package ozobotscpf.app;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import ozobotscpf.nodes.Group;
import ozobotscpf.nodes.PositionMapNode;

import java.util.*;

public class MapController extends MapControllerBase {
    PositionMapNode[][] nodes;

    public Map<Group, Set<PositionMapNode>> getGroups() {
        return groups;
    }

    Map<Group, Set<PositionMapNode>> groups;

    public void setSelectedGroup(Group selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    Group selectedGroup;

    Paint noGroupColor;

    public MapController(int width, int height, Pane pane) {
        super(width, height, pane, computeGridTick(width,height, pane.getWidth(), pane.getHeight()));

        //initialize computed/hard-coded fields
        noGroupColor = Paint.valueOf("gray");
        selectedGroup=null;
        groups = new HashMap<>();

        //allow OnMouseDragEvent on targets
        Scene scene = pane.getScene();
        scene.addEventFilter(MouseEvent.DRAG_DETECTED , new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                scene.startFullDrag();
            }
        });

        //draw nodes
        generateNodes();
    }



    private static double computeGridTick(int gridWidth, int gridHeight, double paneWidth, double paneHeight){
        double xRad = paneWidth / (gridWidth);
        double yRad = paneHeight / (gridHeight);
        return Math.min(xRad,yRad);
    }

    private void generateNodes(){
        nodes = new PositionMapNode[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                nodes[x][y] = new PositionMapNode(x,y);
                Node guiNode = getGuiNode(nodes[x][y]);
                pane.getChildren().add(guiNode);
            }
        }
    }

    private Node getGuiNode(PositionMapNode positionMapNode){
        Shape guiNode = new Circle((positionMapNode.getGridX() +0.5) * gridTick, (positionMapNode.getGridY()+0.5) * gridTick, gridTick/3, noGroupColor);

        EventHandler<MouseEvent> handler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.isPrimaryButtonDown()){
                    //update groups
                    Group old = positionMapNode.getGroup();
                    if(old != null)
                        groups.get(old).remove(positionMapNode);
                    positionMapNode.setGroup(selectedGroup);
                    if(selectedGroup != null)
                        groups.computeIfAbsent(selectedGroup,s -> new HashSet<>()).add(positionMapNode);

                    //update view
                    if(selectedGroup == null)
                        guiNode.setFill(noGroupColor);
                    else
                        guiNode.setFill(selectedGroup.getColor());
                }
            }
        };

        guiNode.setOnMouseDragEntered(handler);
        guiNode.setOnMousePressed(handler);
        return guiNode;
    }
}
