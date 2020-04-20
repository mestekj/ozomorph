package ozobotscpf.app;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;
import org.jdom2.JDOMException;
import ozobotscpf.nodes.AgentMapNode;
import ozobotscpf.ozocodegenerator.OzocodeGenerator;

import java.io.IOException;
import java.util.List;

public class SimulationController {
    public ToggleButton tbRun;
    public Pane pMap;
    public FunctionalSlider slScale;
    public FunctionalSlider slSpeed;
    public ToggleButton tbOnScreen;


    private SimulationMapController simulationMapController;
    private List<AgentMapNode> agents;
    private int width;
    private int height; //size of grid
    private double simulationSpeed = 1.0; //modified via slider
    private final double simulationStep = 1.0 / 60;

    private final double gridTickCm = 5;
    private final double agentRadiusCm = 1.5;
    private final double gridLineWidthCm = 0.5;
    private double scale = 1.0; //modified via slider
    private boolean onScreenMode = false; //modified via togle button
    private boolean positionsReseted = true;

    private Timeline timeline;


    public void init(int width, int height, List<AgentMapNode> agents) {
        this.agents = agents;
        this.width = width;
        this.height = height;
        this.setScale(getScaleToFit());
        initMap();
    }

    private void initMap() {
        simulationMapController = new SimulationMapController(width, height, pMap, agents, getGridTickPx(), getAgentRadiusPx(), getGridLineWidthPx(), onScreenMode);
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
    }

    private double getGridTickPx() {
        return gridTickCm * getDPcm() * getScale();
    }

    private double getAgentRadiusPx() {
        return agentRadiusCm * getDPcm() * getScale();
    }

    private double getGridLineWidthPx() {
        return gridLineWidthCm * getDPcm() * getScale();
    }

    private double getScaleToFit() {
        return Math.min(getScaleToFit1D(width, pMap.getWidth()), getScaleToFit1D(height, pMap.getHeight()));
    }

    private double getScaleToFit1D(int nodes, double pixels) {
        double pixelsUnscaled = (nodes + 1) * gridTickCm * getDPcm();
        return pixels / pixelsUnscaled;
    }

    ///pixels per centimetre
    private double getDPcm() {
        Window w = pMap.getScene().getWindow();
        Rectangle2D windowCentre = new Rectangle2D((w.getX() + w.getWidth()) / 2, (w.getY() + w.getHeight()) / 2, 1, 1);
        Screen screen = Screen.getScreensForRectangle(windowCentre).get(0);
        double dpi = screen.getDpi(); //TODO scaling?

        return dpi / 2.54;
    }

    public void handleRunToggle(ActionEvent actionEvent) {
        if (tbRun.isSelected()) {
            if (positionsReseted && onScreenMode) {
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
        //red for 300 ms

        Rectangle cover = new Rectangle(pMap.getWidth(), pMap.getHeight(), Color.RED);
        Timeline activationSequence = new Timeline(
                new KeyFrame(Duration.millis(0), ae -> pMap.getChildren().add(cover)),
                new KeyFrame(Duration.millis(300), ae -> pMap.getChildren().remove(cover))
        );
        activationSequence.setCycleCount(1);
        return activationSequence;
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
            OzocodeGenerator generator = new OzocodeGenerator();
            generator.generateOzocodes(agents);
        } catch (JDOMException e) {
            showError("Error while generating ozocodes.");
        } catch (IOException e) {
            showError("Error while generating ozocodes.");
        }

    }

    public void handleOnScreenToggle(ActionEvent actionEvent) {
        if (tbOnScreen.isSelected()) {
            setScale(1);
            onScreenMode = true;
            initMap();
        } else {
            onScreenMode = false;
            initMap();
        }
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
}
