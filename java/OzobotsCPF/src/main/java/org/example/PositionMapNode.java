package org.example;

import javafx.scene.Node;

public class PositionMapNode implements MapNode {
    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }


    public PositionMapNode(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
    }

    private int gridX, gridY;

    public Group getGroup() {
        return group;
    }

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
