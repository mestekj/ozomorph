package ozobotscpf.app;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;
import ozobotscpf.nodes.AgentMapNode;

import java.util.List;

public class SimulationController {
    public ToggleButton tbRun;
    public Pane pMap;
    public FunctionalSlider slScale;
    public FunctionalSlider slSpeed;


    private SimulationMapController simulationMapController;
    private List<AgentMapNode> agents;
    private int width; private int height; //size of grid
    private double simulationSpeed = 1.0; //modified via slider
    private double simulationStep = 1.0/60;

    private double gridTickCm = 5;
    private double agentRadiusCm = 1.5;
    private double gridLineWidthCm = 0.5;
    private double scale = 1.0; //modified via slider

    private Timeline timeline;


    public void init(int width, int height, List<AgentMapNode> agents){
        this.agents = agents;
        this.width = width;
        this.height = height;
        initMap();
    }

    private void initMap(){
        simulationMapController = new SimulationMapController(width,height, pMap, agents, getGridTickPx(), getAgentRadiusPx(), getGridLineWidthPx());
    }

    @FXML
    public void initialize() {
        slScale.functionValueProperty().addListener((observableValue, number, t1) -> {
            scale = slScale.getFunctionValue();
            initMap();
        });

        slSpeed.functionValueProperty().addListener((observableValue, number, t1) -> {
            simulationSpeed = slSpeed.getFunctionValue();
            //re-run simulation (now with new speed value)
            if(timeline != null)
                timeline.stop();
            handleRunToggle(new ActionEvent());
        });
    }


    private double getGridTickPx(){
        return gridTickCm * getDPcm() * scale;
    }

    private double getAgentRadiusPx(){
        return agentRadiusCm * getDPcm() * scale;
    }

    private double getGridLineWidthPx() {
        return gridLineWidthCm*getDPcm()*scale;
    }

    ///pixels per centimetre
    private double getDPcm(){
        Window w = pMap.getScene().getWindow();
        Rectangle2D windowCentre = new Rectangle2D((w.getX()+w.getWidth())/2, (w.getY()+w.getHeight())/2,1,1);
        Screen screen = Screen.getScreensForRectangle(windowCentre).get(0);
        double dpi = screen.getDpi(); //TODO scaling?

        return dpi / 2.54;
    }

    public void handleRunToggle(ActionEvent actionEvent) {
        if(tbRun.isSelected()){
            timeline = new Timeline(new KeyFrame(Duration.seconds(simulationStep), ae -> timerTick()));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }
        else {
            timeline.stop();
        }
    }

    public void handleReset(ActionEvent actionEvent) {
        if(tbRun.isSelected())
            tbRun.fire();
        for (AgentMapNode agent : agents) {
            agent.resetPosition();
        }
        simulationMapController.updateGuiNodesPositions();
    }

    private void timerTick(){
        for(AgentMapNode agent : agents)
            agent.move(simulationSpeed * simulationStep);
        simulationMapController.updateGuiNodesPositions();
    }
}
