package ozomorph.app;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;

/**
 * Base class for MapControlers. Ensures drawing a grid.
 */
public class MapControllerBase {
    int width;
    int height;
    double gridTick;
    double gridLineWidth;
    Color gridLineColor;
    Pane pane;

    /**
     * Initializes new MapControllerBase with default width od grid lines (1)
     * @param width Width of map (number of nodes).
     * @param height Height of map (number of nodes).
     * @param pane Pane to draw map to.
     * @param gridTick Spacing between grid lines (pixels).
     */
    public MapControllerBase(int width, int height, Pane pane, double gridTick) {
        this(width,height,pane,gridTick,1);
    }

    /**
     * Initializes new MapControllerBase.
     * @param width Width of map (number of nodes).
     * @param height Height of map (number of nodes).
     * @param pane Pane to draw map to.
     * @param gridTick Spacing between grid lines (pixels).
     * @param gridLineWidth Width of a grid line (pixels).
     */
    public MapControllerBase(int width, int height, Pane pane, double gridTick, double gridLineWidth) {
        this(width, height, pane, gridTick,gridLineWidth, Color.BLACK);
    }

    /**
     * Initializes new MapControllerBase.
     * @param width Width of map (number of nodes).
     * @param height Height of map (number of nodes).
     * @param pane Pane to draw map to.
     * @param gridTick Spacing between grid lines (pixels).
     * @param gridLineWidth Width of a grid line (pixels).
     */
    public MapControllerBase(int width, int height, Pane pane, double gridTick, double gridLineWidth, Color gridLineColor) {
        this.width = width;
        this.height = height;
        this.pane = pane;
        this.gridTick = gridTick;
        this.gridLineWidth = gridLineWidth;
        this.gridLineColor = gridLineColor;

        pane.getChildren().clear();
        drawGridLines();
    }



    /**
     * Draw grid lines on pane.
     */
    protected void drawGridLines() {
        for (int x = 0; x < width; x++) {
            Line gridLine = new Line(computeGuiCoordinate(x), 0, computeGuiCoordinate(x), height*gridTick);
            gridLine.setStrokeWidth(gridLineWidth);
            gridLine.setStroke(gridLineColor);
            pane.getChildren().add(gridLine);
        }
        for (int y = 0; y < height; y++) {
            Line gridLine = new Line(0, computeGuiCoordinate(y), width * gridTick, computeGuiCoordinate(y));
            gridLine.setStrokeWidth(gridLineWidth);
            gridLine.setStroke(gridLineColor);
            pane.getChildren().add(gridLine);
        }
    }

    protected double computeGuiCoordinate(double gridCoordinate){
        return (0.5 + gridCoordinate) * gridTick;
    }

    protected double computeGridCoordinate(double guiCoordinate){
        return guiCoordinate / gridTick - 0.5;
    }
}
