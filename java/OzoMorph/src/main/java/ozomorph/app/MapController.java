package ozomorph.app;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import ozomorph.nodes.Group;
import ozomorph.nodes.PositionMapNode;

import java.util.*;

public class MapController extends MapControllerBase implements Observable {
    private List<InvalidationListener> listeners = new ArrayList<>();

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

    Map<PositionMapNode,Shape> guiNodes;

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
        guiNodes = new HashMap<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                nodes[x][y] = new PositionMapNode(x,y);
                Shape guiNode = getGuiNode(nodes[x][y]);
                guiNodes.put(nodes[x][y],guiNode);
                pane.getChildren().add(guiNode);
            }
        }
    }

    private Shape getGuiNode(PositionMapNode positionMapNode){
        Shape guiNode = new Circle((positionMapNode.getGridX() +0.5) * gridTick, (positionMapNode.getGridY()+0.5) * gridTick, gridTick/3, noGroupColor);

        EventHandler<MouseEvent> handler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.isPrimaryButtonDown()){
                    updateGroup(positionMapNode,guiNode, selectedGroup);
                    notifyGroupChanged();
                }
            }
        };

        guiNode.setOnMouseDragEntered(handler);
        guiNode.setOnMousePressed(handler);
        return guiNode;
    }

    private void updateGroup(PositionMapNode positionMapNode, Shape guiNode, Group newGroup){
        Group old = positionMapNode.getGroup();
        if(old != null){
            //erase
            groups.get(old).remove(positionMapNode);
            positionMapNode.setGroup(null);
            guiNode.setFill(noGroupColor);
        }
        else{
            positionMapNode.setGroup(newGroup);
            groups.computeIfAbsent(newGroup,s -> new HashSet<>()).add(positionMapNode);
            guiNode.setFill(newGroup.getColor());
        }
    }


    private void notifyGroupChanged(){
        for (InvalidationListener listener : listeners) {
            listener.invalidated(this);
        }
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        listeners.add(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        listeners.remove(invalidationListener);
    }

    public void load(Map<Group, Set<PositionMapNode>> initialPositions) {
        for (Map.Entry<Group, Set<PositionMapNode>> entry : initialPositions.entrySet()) {
            Group group = entry.getKey();
            for (PositionMapNode savedNode : entry.getValue()) {
                PositionMapNode corespondingNode = nodes[savedNode.getGridX()][savedNode.getGridY()];
                updateGroup(corespondingNode, guiNodes.get(corespondingNode),group);
            }
        }
        notifyGroupChanged();
    }
}
