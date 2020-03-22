package ozobotscpf.app;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import ozobotscpf.nodes.AgentMapNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationMapController extends MapControllerBase {
    private Map<AgentMapNode, Node> agentsGuiNodes;
    private double agentRadius;

    public SimulationMapController(int width, int height, Pane pane, List<AgentMapNode> agents, double gridTick, double agentRadius) {
        super(width, height, pane, gridTick);
        this.agentRadius = agentRadius;

        generateAgentsGuiNodes(agents);
        updateGuiNodesPositions();
    }

    private void generateAgentsGuiNodes(List<AgentMapNode> agents){
        agentsGuiNodes = new HashMap<>();
        for (var agent : agents) {
            Arc guiNode = new Arc(0,0,agentRadius, agentRadius, 105, 330); //arc should point towards negative Y
            guiNode.setType(ArcType.ROUND);
            guiNode.setFill(agent.getGroup().getColor());
            agentsGuiNodes.put(agent,guiNode);
            pane.getChildren().add(guiNode);
        }
    }

    public void updateGuiNodesPositions(){
        for(var entry : agentsGuiNodes.entrySet()){
            AgentMapNode agent = entry.getKey();
            Node guiNode = entry.getValue();
            guiNode.getTransforms().clear();
            guiNode.getTransforms().addAll(new Translate((agent.getX() +0.5) * gridTick, (agent.getY()+0.5) * gridTick),new Rotate(agent.getOrientation() * 360,0,0));
            
        }
    }
}
