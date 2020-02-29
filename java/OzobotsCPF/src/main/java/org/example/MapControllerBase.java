package org.example;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

public class MapControllerBase {
    int width;
    int height;
    double gridTick;
    Pane pane;

    public MapControllerBase(int width, int height, Pane pane, double gridTick) {
        this.width = width;
        this.height = height;
        this.pane = pane;
        this.gridTick = gridTick;

        pane.getChildren().clear();
        drawGridLines();
    }

    protected void drawGridLines() {
        for (int x = 0; x < width; x++) {
            Shape gridLine = new Line((0.5+x)* gridTick, 0, (0.5+x)* gridTick, height*gridTick);
            pane.getChildren().add(gridLine);
        }
        for (int y = 0; y < height; y++) {
            Shape gridLine = new Line(0, (0.5+y)* gridTick, width * gridTick, (0.5+y)* gridTick);
            pane.getChildren().add(gridLine);
        }
    }

    protected void handleError(String userMessage, Exception e){
       e.printStackTrace();
    }
}
