package ozomorph.app;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import ozomorph.nodes.AgentMapNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowMeMapController extends SimulationMapController {
    private Map<AgentMapNode, Path> paths;
    private int pathLength = 60*2;

    public FollowMeMapController(int width, int height, Pane pane, List<AgentMapNode> agents, double gridTick, double agentRadius, double gridLineWidth) {
        super(width, height, pane, agents, gridTick, agentRadius, gridLineWidth,true, Color.LIGHTGRAY);
    }

    protected void generateAgentsGuiNodes(List<AgentMapNode> agents, boolean onScreenMode){
        super.generateAgentsGuiNodes(agents,onScreenMode);
        generatePaths(agents);
    }

    private void generatePaths(List<AgentMapNode> agents) {
        paths = new HashMap<>();
        for (var agent : agents){
            var path = new Path();
            paths.put(agent,path);
            path.setStroke(Color.BLUE);
            path.setStrokeWidth(this.gridLineWidth*1.1);
            pane.getChildren().add(path);

            MoveTo moveTo = new MoveTo();
            moveTo.setX(agent.getX());
            moveTo.setY(agent.getY());

            path.getElements().add(moveTo);
        }
    }

    @Override
    public void updateGuiNodesPositions() {
        super.updateGuiNodesPositions();
        for (var entry : paths.entrySet()){
            var path = entry.getValue();
            var agent = entry.getKey();

            LineTo lineTo = new LineTo();
            lineTo.setX(computeGuiCoordinate(agent.getX()));
            lineTo.setY(computeGuiCoordinate(agent.getY()));

            path.getElements().add(lineTo);
            if(path.getElements().size() > pathLength+1){
                int newfirst = path.getElements().size() - pathLength;
                var lastremoved = (LineTo) path.getElements().get(newfirst-1);


                MoveTo moveTo = new MoveTo();
                moveTo.setX(lastremoved.getX());
                moveTo.setY(lastremoved.getY());

                path.getElements().add(0,moveTo);
                path.getElements().remove(1,newfirst+1);

            }
        }
    }
}
