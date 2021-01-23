package ozomorph.app;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import ozomorph.nodes.AgentMapNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for GUI element that shows map with agents.
 */
public class SimulationMapController extends MapControllerBase {
    private Map<AgentMapNode, Node> agentsGuiNodes;
    private double agentRadius;
    private final double onScreenOpacityFactor = 0.2;

    /**
     * Creates new SimulationMapController to simulate given agents on map of given size.
     * @param width Width of map (number of nodes).
     * @param height Height of map (number of nodes).
     * @param pane Pane to draw map to.
     * @param agents List of agents to be simulated.
     * @param gridTick Spacing between grid lines in pixels.
     * @param agentRadius Radius of agents in pixels.
     * @param gridLineWidth Width of grid lines in pixels.
     * @param onScreenMode If is window in OnScreen mode.
     */
    public SimulationMapController(int width, int height, Pane pane, List<AgentMapNode> agents, double gridTick, double agentRadius, double gridLineWidth, boolean onScreenMode, Color gridLineColor) {
        super(width, height, pane, gridTick, gridLineWidth, gridLineColor);
        this.agentRadius = agentRadius;

        generateAgentsGuiNodes(agents, onScreenMode);
        updateGuiNodesPositions();
    }

    /**
     * Creates new SimulationMapController to simulate given agents on map of given size in other than OnScreen mode.
     * @param width Width of map (number of nodes).
     * @param height Height of map (number of nodes).
     * @param pane Pane to draw map to.
     * @param agents List of agents to be simulated.
     * @param gridTick Spacing between grid lines in pixels.
     * @param agentRadius Radius of agents in pixels.
     * @param gridLineWidth Width of grid lines in pixels.
     */
    public SimulationMapController(int width, int height, Pane pane, List<AgentMapNode> agents, double gridTick, double agentRadius, double gridLineWidth) {
        this(width,height,pane,agents,gridTick,agentRadius,gridLineWidth,false);
    }

    /**
     * Creates new SimulationMapController to simulate given agents on map of given size in other than OnScreen mode.
     * @param width Width of map (number of nodes).
     * @param height Height of map (number of nodes).
     * @param pane Pane to draw map to.
     * @param agents List of agents to be simulated.
     * @param gridTick Spacing between grid lines in pixels.
     * @param agentRadius Radius of agents in pixels.
     * @param gridLineWidth Width of grid lines in pixels.
     * @param onScreenMode If is window in OnScreen mode.
     */
    public SimulationMapController(int width, int height, Pane pane, List<AgentMapNode> agents, double gridTick, double agentRadius, double gridLineWidth, boolean onScreenMode) {
        this(width,height,pane,agents,gridTick,agentRadius,gridLineWidth, onScreenMode, Color.BLACK);
    }

    /**
     * Initializes GUI elements representing agents.
     * @param agents Agents to draw on map.
     * @param onScreenMode If is in OnScreen mode.
     */
    protected void generateAgentsGuiNodes(List<AgentMapNode> agents, boolean onScreenMode){
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

    /**
     * Updates position of GUI elements representing agents according to position of each agent.
     */
    public void updateGuiNodesPositions(){
        for(var entry : agentsGuiNodes.entrySet()){
            AgentMapNode agent = entry.getKey();
            Node guiNode = entry.getValue();
            guiNode.getTransforms().clear();
            guiNode.getTransforms().addAll(new Translate((agent.getX() +0.5) * gridTick, (agent.getY()+0.5) * gridTick),new Rotate(agent.getOrientation() * 360,0,0));
            
        }
    }

    /**
     * Gets position (in window) of GUI node representing given agent.
     * @param agent Agent whose position is returned.
     * @return Position of GUI node (as transform from (0,0) to actual position).
     */
    Transform getGuiNodePosition(AgentMapNode agent){
        return agentsGuiNodes.get(agent).getTransforms().get(0);
    }
}
