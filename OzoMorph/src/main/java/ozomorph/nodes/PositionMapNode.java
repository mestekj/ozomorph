package ozomorph.nodes;

import java.io.Serializable;

/**
 * Position on map where an agent can be placed.
 */
public class PositionMapNode implements MapNode, Serializable {
    private static final long serialVersionUID = 30000001L;

    /**
     * X coordinate of position on grid map, in 0 to (width of map - 1).
     * @return X coordinate of position.
     */
    public int getGridX() {
        return gridX;
    }

    /**
     * Y coordinate of position on grid map, in 0 to (height of map - 1).
     * @return Y coordinate of position.
     */
    public int getGridY() {
        return gridY;
    }


    /**
     * Creates new PositionMapNode.
     * @param gridX X coordinate in grid map.
     * @param gridY Y coordinate in grid map.
     */
    public PositionMapNode(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
    }

    private int gridX, gridY;

    /**
     * Gets group to which is this position assigned.
     * Null means that this position is free (not assigned to any group).
     * @return Group to which is this position assigned.
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Assigns this position to the group.
     * Null means that this position is free (not assigned to any group).
     * @param group Group to which is this position assigned.
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    private Group group;

    @Override
    public double getX() {
        return gridX;
    }

    @Override
    public double getY() {
        return gridY;
    }
}
