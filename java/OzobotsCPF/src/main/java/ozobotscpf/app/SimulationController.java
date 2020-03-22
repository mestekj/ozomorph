package ozobotscpf.app;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import ozobotscpf.nodes.AgentMapNode;

import java.util.List;

public class SimulationController {
    public ToggleButton tbRun;
    public Pane pMap;

    private SimulationMapController simulationMapController;
    private List<AgentMapNode> agents;
    private double simulationSpeed = 1.0;
    private double simulationStep = 1.0/60;

    private double gridTick = 50;
    private double agentRadius = 20;

    private Timeline timeline;


    public void init(int width, int height, List<AgentMapNode> agents){
        this.agents = agents;
        simulationMapController = new SimulationMapController(width,height, pMap, agents, gridTick, agentRadius);
    }

    @FXML
    public void initialize(){
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
