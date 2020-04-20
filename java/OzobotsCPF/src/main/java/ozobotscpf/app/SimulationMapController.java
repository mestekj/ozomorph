package ozobotscpf.app;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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
    private final double onScreenOpacityFactor = 0.2;

    public SimulationMapController(int width, int height, Pane pane, List<AgentMapNode> agents, double gridTick, double agentRadius, double gridLineWidth, boolean onScreenMode) {
        super(width, height, pane, gridTick, gridLineWidth);
        this.agentRadius = agentRadius;

        generateAgentsGuiNodes(agents, onScreenMode);
        updateGuiNodesPositions();
    }

    public SimulationMapController(int width, int height, Pane pane, List<AgentMapNode> agents, double gridTick, double agentRadius, double gridLineWidth) {
        this(width,height,pane,agents,gridTick,agentRadius,gridLineWidth,false);
    }

    private void generateAgentsGuiNodes(List<AgentMapNode> agents, boolean onScreenMode){
        agentsGuiNodes = new HashMap<>();
        for (var agent : agents) {
            Arc arc = new Arc(0,0,agentRadius, agentRadius, 105, 330); //arc should point towards negative Y
            arc.setType(ArcType.ROUND);
            Color agentColor = agent.getGroup().getColor();
            if(onScreenMode)
                agentColor = agentColor.deriveColor(0,1,1,onScreenOpacityFactor); //same color but partially transparent
            arc.setFill(agentColor);
            Label agentID = new Label(String.valueOf(agent.getId()));

            var guiNode = new Group(arc,agentID);
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
