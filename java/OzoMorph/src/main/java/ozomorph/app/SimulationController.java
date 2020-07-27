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
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ozomorph.nodes.AgentMapNode;
import ozomorph.ozocodegenerator.OzocodeGenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SimulationController {
    private static Logger logger = LoggerFactory.getLogger(SimulationMapController.class);

    public ToggleButton tbRun;
    public Pane pMap;
    public FunctionalSlider slScale;
    public FunctionalSlider slSpeed;
    public ChoiceBox<Mode> cbMode;


    SimulationMapController simulationMapController;
    private List<AgentMapNode> agents;
    private MapSettings settings;
    private int width;
    private int height; //size of grid
    private double simulationSpeed = 1.0; //modified via slider
    private final double simulationStep = 1.0 / 60;

    private double scale = 1.0; //modified via slider
    private boolean positionsReseted = true;

    private Timeline timeline;
    private Mode mode = Mode.SIMULATION; //modified via toggle button


    public void init(int width, int height, List<AgentMapNode> agents, MapSettings settings) {
        this.agents = agents;
        this.width = width;
        this.height = height;
        this.settings = settings;
        this.setScale(getScaleToFit());
        initMap();
    }

    private void initMap() {
        simulationMapController = new SimulationMapController(width, height, pMap, agents, getGridTickPx(), getAgentRadiusPx(), getGridLineWidthPx(), mode == Mode.ONSCREEN);
        logger.info("Simulation map controller inited.");
    }

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

    private double getGridTickPx() { return settings.getGridTickCm() * getDPcm() * getScale(); }

    private double getAgentRadiusPx() {
        return settings.getAgentRadiusCm() * getDPcm() * getScale();
    }

    private double getGridLineWidthPx() {
        return settings.getGridLineWidthCm() * getDPcm() * getScale();
    }

    private double getScaleToFit() {
        return Math.min(getScaleToFit1D(width, pMap.getWidth()), getScaleToFit1D(height, pMap.getHeight()));
    }

    private double getScaleToFit1D(int nodes, double pixels) {
        double pixelsUnscaled = (nodes + 1) * settings.getGridTickCm() * getDPcm();
        return pixels / pixelsUnscaled;
    }

    ///pixels per centimetre
    private double getDPcm() {
        Window w = pMap.getScene().getWindow();
        Rectangle2D windowCentre = new Rectangle2D((w.getX() + w.getWidth()) / 2, (w.getY() + w.getHeight()) / 2, 1, 1);
        Screen screen = Screen.getScreensForRectangle(windowCentre).get(0);
        double dpi = screen.getDpi();

        return dpi / 2.54;
    }

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

    private Timeline getActivationSequence() {
        return mode.getActivationSequence(pMap,this);
    }

    private Timeline getExecutionSequence() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(simulationStep), ae -> timerTick()));
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }

    public void handleReset(ActionEvent actionEvent) {
        if (tbRun.isSelected())
            tbRun.fire(); //toggle off
        for (AgentMapNode agent : agents) {
            agent.resetPosition();
        }
        simulationMapController.updateGuiNodesPositions();
        positionsReseted = true;
    }

    private void timerTick() {
        for (AgentMapNode agent : agents)
            agent.move(getSimulationSpeed() * simulationStep);
        simulationMapController.updateGuiNodesPositions();
    }

    public void handleGenerateOzocodes(ActionEvent actionEvent) {
        try {
            //select Ozocode template file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Ozocode template");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Ozocode", "*.ozocode")
            );
            File templateFile = fileChooser.showOpenDialog(pMap.getScene().getWindow());
            if (templateFile != null) {

                //generate Ozocodes
                OzocodeGenerator generator = new OzocodeGenerator();
                generator.generateOzocodes(agents,templateFile);
            }


        } catch (JDOMException e) {
            logger.error("Ozocodes not generated.", e);
            showError("Error while generating ozocodes.");
        } catch (IOException e) {
            logger.error("Ozocodes not generated.", e);
            showError("Error while generating ozocodes.");
        }
    }

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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected double getSimulationSpeed() {
        return simulationSpeed;
    }

    protected void setSimulationSpeed(double simulationSpeed) {
        this.simulationSpeed = simulationSpeed;
        slSpeed.setFuctionalValue(simulationSpeed);
    }

    protected double getScale() {
        return scale;
    }

    protected void setScale(double scale) {
        this.scale = scale;
        slScale.setFuctionalValue(scale);
    }

    List<AgentMapNode> getAgents() {
        return agents;
    }
}
