package ozomorph.app;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ozomorph.nodes.AgentMapNode;
import ozomorph.ozocodegenerator.MissingDeclarationException;
import ozomorph.ozocodegenerator.OzocodeGenerator;
import ozomorph.ozocodegenerator.PythonGenerator;
import ozomorph.pathfinder.ProblemInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Controller for Simulation window.
 */
public class SimulationController {
    private static Logger logger = LoggerFactory.getLogger(SimulationMapController.class);

    public ToggleButton tbRun;
    public Pane pMap;
    public FunctionalSlider slScale;
    public FunctionalSlider slSpeed;
    public ChoiceBox<Mode> cbMode;


    SimulationMapController simulationMapController;
    private SimulationData simulationData;
    private List<AgentMapNode> agents;
    private MapSettings settings;
    private int width;
    private int height; //size of grid
    private double simulationSpeed = 1.0; //modified via slider
    private final double simulationStep = 1.0 / 60;

    private double scale = 1.0; //modified via slider
    private boolean positionsReseted = true;

    private Timeline timeline;
    private Mode mode = Mode.SIMULATION; //modified via choice box


    /**
     * Initialization, to allow parameter-less constructor needed for FXML.
     * @param data Data for simulation.
     * @param settings Properties of real map.
     */
    public void init(SimulationData data, MapSettings settings) {
        this.agents = data.agents;
        this.width = data.width;
        this.height = data.height;
        this.simulationData = data;
        this.settings = settings;
        this.setScale(getScaleToFit());
        initMap();
    }

    /**
     * Redraw map using current simulation speed and scale.
     */
    private void initMap() {
        simulationMapController = new SimulationMapController(width, height, pMap, agents, getGridTickPx(), getAgentRadiusPx(), getGridLineWidthPx(), mode == Mode.ONSCREEN);
        logger.info("Simulation map controller inited.");
    }

    /**
     * Initialization and setting of GUI elements that cannot be done in FXML.
     */
    @FXML
    public void initialize() {
        slScale.functionValueProperty().addListener((observableValue, number, t1) -> {
            setScale(slScale.getFunctionValue());
            initMap();
        });

        slSpeed.functionValueProperty().addListener((observableValue, number, t1) -> {
            setSimulationSpeed(slSpeed.getFunctionValue());
            //re-run simulation (now with new speed value)
            if (timeline != null)
                timeline.stop();
            handleRunToggle(new ActionEvent());
        });

        cbMode.getItems().setAll(Mode.values());
        cbMode.setValue(Mode.SIMULATION);
        cbMode.getSelectionModel().selectedItemProperty().addListener(  (observableValue, oldValue, newValue) -> handleModeTogle(newValue));
        logger.info("Simulation window opened.");
    }

    /**
     * Computes grid lines spacing according to current scale.
     * @return Grid lines spacing in pixels.
     */
    private double getGridTickPx() { return settings.getGridTickCm() * getDPcm() * getScale(); }

    /**
     * Computes radius of virtual agents according to current scale.
     * @return Radius of virtual agents in pixels.
     */
    private double getAgentRadiusPx() {
        return settings.getAgentRadiusCm() * getDPcm() * getScale();
    }

    /**
     * Computes width of grid lines according to current scale.
     * @return Width of grid lines in pixels.
     */
    private double getGridLineWidthPx() {
        return settings.getGridLineWidthCm() * getDPcm() * getScale();
    }

    /**
     * Computes scale value so that whole map fits into window.
     * @return Scale value so that whole map fits into window.
     */
    private double getScaleToFit() {
        return Math.min(getScaleToFit1D(width, pMap.getWidth()), getScaleToFit1D(height, pMap.getHeight()));
    }

    /**
     * Computes scale so that given number of map nodes fits into given number of pixels (one dimensional).
     * @param nodes Number of nodes.
     * @param pixels Required length in pixels.
     * @return Scale.
     */
    private double getScaleToFit1D(int nodes, double pixels) {
        double pixelsUnscaled = (nodes + 1) * settings.getGridTickCm() * getDPcm();
        return pixels / pixelsUnscaled;
    }

    /**
     * Gets number of pixels per centimetre of the display where this Simulation window is.
     * It is DPI just converted for centimetre instead of inch.
     * @return Number of pixels per centimetre.
     */
    private double getDPcm() {
        Window w = pMap.getScene().getWindow();
        Rectangle2D windowCentre = new Rectangle2D((w.getX() + w.getWidth()) / 2, (w.getY() + w.getHeight()) / 2, 1, 1);
        Screen screen = Screen.getScreensForRectangle(windowCentre).get(0);
        double dpi = screen.getDpi();

        return dpi / 2.54;
    }

    /**
     * Starts/stops simulation.
     * Handler for Run button.
     * @param actionEvent
     */
    public void handleRunToggle(ActionEvent actionEvent) {
        if (tbRun.isSelected()) {
            if (positionsReseted && mode != Mode.SIMULATION) {
                //play Ozobots' activation sequence
                timeline = getActivationSequence();
                timeline.setOnFinished(e -> {
                    //start animation (movement of agents according to their plans)
                    timeline = getExecutionSequence();
                    timeline.play();
                });
            } else {
                timeline = getExecutionSequence();
            }
            positionsReseted = false;
            timeline.play();
        } else {
            if (timeline != null)
                timeline.stop();
        }
    }

    /**
     * Gets activation sequence (animation played before agents starts moving) for current mode.
     * @return Activation sequence.
     */
    private Timeline getActivationSequence() {
        return mode.getActivationSequence(pMap,this);
    }

    /**
     * Creates execution sequence (animation of agents moving according to their plans).
     * @return Execution sequence.
     */
    private Timeline getExecutionSequence() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(simulationStep), ae -> timerTick()));
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }

    /**
     * Resets agents to their starting locations. Stops simulation if running.
     * Handler for Reset button.
     * @param actionEvent
     */
    public void handleReset(ActionEvent actionEvent) {
        if (tbRun.isSelected())
            tbRun.fire(); //toggle off
        for (AgentMapNode agent : agents) {
            agent.resetPosition();
        }
        simulationMapController.updateGuiNodesPositions();
        positionsReseted = true;
    }

    /**
     * Does one step of execution sequence animation.
     */
    private void timerTick() {
        for (AgentMapNode agent : agents)
            agent.move(getSimulationSpeed() * simulationStep);
        simulationMapController.updateGuiNodesPositions();
    }

    /**
     * Generates Ozocodes (programs for Ozobots Evo) into files.
     * Handler for Generate button.
     * @param actionEvent
     */
    public void handleGenerateOzocodes(ActionEvent actionEvent) {
        try {
            //select Ozocode template file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Ozocode template");
            // Open chooser in ozocode_templates directory if available
            File initialDir = new File("../ozocode_templates");
            if (initialDir.exists() && initialDir.isDirectory()) {
                fileChooser.setInitialDirectory(initialDir);
            }
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Ozocode", "*.ozocode")
            );
            File templateFile = fileChooser.showOpenDialog(pMap.getScene().getWindow());
            if (templateFile != null) {
                logger.info("Generating Ozocodes using template: "+ templateFile.getCanonicalPath());
                //generate Ozocodes
                OzocodeGenerator generator = new OzocodeGenerator();
                generator.generateOzocodes(agents, templateFile);
            }
        }catch (MissingDeclarationException e){
            logger.error("Ozocodes not generated", e);
            showError("Selected template does not contain required procedure "+e.getMissingProcedureName() + ".");
        } catch (JDOMException e) {
            logger.error("Ozocodes not generated.", e);
            showError("Error while generating ozocodes.");
        } catch (IOException e) {
            logger.error("Ozocodes not generated.", e);
            showError("Error while generating ozocodes.");
        }
    }


    /**
     * Generates Python code (mutli-bot program for Ozobots Evo) for Ozobot Editor.
     * Handler for GeneratePy button.
     * @param actionEvent
     */
    public void handleGeneratePython(ActionEvent actionEvent) {
        try {
            //select python template file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Python code template");
            // Open chooser in ozocode_templates directory if available
            File initialDir = new File("../ozocode_templates");
            if (initialDir.exists() && initialDir.isDirectory()) {
                fileChooser.setInitialDirectory(initialDir);
            }
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Python script", "*.py")
            );
            File templateFile = fileChooser.showOpenDialog(pMap.getScene().getWindow());
            if (templateFile != null) {
                logger.info("Generating Python code using template: "+ templateFile.getCanonicalPath());
                //generate Python code
                PythonGenerator generator = new PythonGenerator();
                String pythonProgram = generator.generateOzocodes(agents, templateFile);
                
                // Copy content to clipboard
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.putString(pythonProgram);
                Clipboard.getSystemClipboard().setContent(clipboardContent);
                logger.info("Python code copied to clipboard");
            }
        }catch (MissingDeclarationException e){ // TODO update
            logger.error("Ozo Python not generated", e);
            showError("Selected template does not contain required procedure "+e.getMissingProcedureName() + ".");
        } catch (IOException e) {
            logger.error("Ozo Python not generated.", e);
            showError("Error while generating Ozobots' code.");
        }
    }

    /**
     * Switches current mode. Updates map scale if necessary.
     * @param newMode New mode of Simulation.
     */
    private void handleModeTogle(Mode newMode) {
        switch (newMode){
            case ONBOARD:
                setScale(getScaleToFit());
                break;
            case ONSCREEN:
                setScale(1);
                break;
            default:break;
        }
        mode = newMode;
        initMap();
        logger.info("Mode changed to "+ mode.toString());
    }

    /**
     * Informs user about error.
     * @param message Error message to show.
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gets current simulation speed factor.
     * @return Simulation speed factor.
     */
    protected double getSimulationSpeed() {
        return simulationSpeed;
    }

    /**
     * Sets current simulation speed factor (by code).
     * @param simulationSpeed New simulation speed factor.
     */
    protected void setSimulationSpeed(double simulationSpeed) {
        this.simulationSpeed = simulationSpeed;
        slSpeed.setFuctionalValue(simulationSpeed);
    }

    /**
     * Gets current scale.
     * @return Scale of map.
     */
    protected double getScale() {
        return scale;
    }

    /**
     * Sets map scale (by code).
     * @param scale New map scale.
     */
    protected void setScale(double scale) {
        this.scale = scale;
        slScale.setFuctionalValue(scale);
    }

    /**
     * Gets list of agents.
     * @return List of agents.
     */
    List<AgentMapNode> getAgents() {
        return agents;
    }


    protected void savePlans(){
        FileChooser fileChooser = FileChooserFactory.createPlansFileChooser();

        //Show save file dialog
        File file = fileChooser.showSaveDialog(pMap.getScene().getWindow());
        if(file != null){
            //save problemInstance
            try (FileOutputStream stream = new FileOutputStream(file)) {
                try (ObjectOutputStream out = new ObjectOutputStream(stream)) {
                    out.writeObject(simulationData);
                    logger.info("Plans saved to " + file.getCanonicalPath());
                }
            }
            catch (IOException e){
                logger.error("Error while saving plans.", e);
                showError("Error while saving the plans.");
            }
        }
    }


    public void handleSavePlans(ActionEvent actionEvent) {
        savePlans();
    }
}
