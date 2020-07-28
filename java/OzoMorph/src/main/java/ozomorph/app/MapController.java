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

/**
 * Controller for element allowing user to enter configuration of agents.
 */
public class MapController extends MapControllerBase implements Observable {
    private List<InvalidationListener> listeners = new ArrayList<>();

    /**
     * Grid of nodes each of which represents possible agent position.
     */
    PositionMapNode[][] nodes;

    /**
     * Current configuration (groups and positions assigned to them).
     * @return Curent configuration of agents.
     */
    public Map<Group, Set<PositionMapNode>> getGroups() {
        return groups;
    }

    Map<Group, Set<PositionMapNode>> groups;

    /**
     * Set the group that a position will be assigned to when clicked.
     * @param selectedGroup Edited group.
     */
    public void setSelectedGroup(Group selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    Group selectedGroup;

    Paint noGroupColor;

    /**
     * GUI elements representing given map nodes.
     */
    Map<PositionMapNode,Shape> guiNodes;

    /**
     * Initializes new blank MapController (none of map nodes is assigned to any group)
     * @param width Width of map (number of nodes).
     * @param height Height of map (number of nodes).
     * @param pane Pane to draw map on.
     */
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

    /**
     * Computes spacing between grid lines so that whole map fits to given Pane.
     * @param gridWidth Width of map (number of nodes).
     * @param gridHeight Height of map (number of nodes).
     * @param paneWidth Width of pane (pixels).
     * @param paneHeight Height of pane (pixels).
     * @return Space between grid lines in pixels.
     */
    private static double computeGridTick(int gridWidth, int gridHeight, double paneWidth, double paneHeight){
        double xRad = paneWidth / (gridWidth);
        double yRad = paneHeight / (gridHeight);
        return Math.min(xRad,yRad);
    }

    /**
     * Initializes nodes according to specified size of map.
     */
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

    /**
     * Creates GUI element that will represent given map node.
     * @param positionMapNode Map node.
     * @return GUI element.
     */
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

    /**
     * Assigns given map node to given group.
     * @param positionMapNode Map node.
     * @param guiNode GUI element that represents the map node.
     * @param newGroup Group to assign the map node to.
     */
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

    /**
     * Notify listeners that group of an position has changed.
     */
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

    /**
     * Loads configuration.
     * @param initialPositions Given configuration.
     */
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
