package ozomorph.app;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

/**
 * Base class for MapControlers. Ensures drawing a grid.
 */
public class MapControllerBase {
    int width;
    int height;
    double gridTick;
    double gridLineWidth;
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
        this.width = width;
        this.height = height;
        this.pane = pane;
        this.gridTick = gridTick;
        this.gridLineWidth = gridLineWidth;

        pane.getChildren().clear();
        drawGridLines();
    }

    /**
     * Draw grid lines on pane.
     */
    protected void drawGridLines() {
        for (int x = 0; x < width; x++) {
            Line gridLine = new Line((0.5+x)* gridTick, 0, (0.5+x)* gridTick, height*gridTick);
            gridLine.setStrokeWidth(gridLineWidth);
            pane.getChildren().add(gridLine);
        }
        for (int y = 0; y < height; y++) {
            Line gridLine = new Line(0, (0.5+y)* gridTick, width * gridTick, (0.5+y)* gridTick);
            gridLine.setStrokeWidth(gridLineWidth);
            pane.getChildren().add(gridLine);
        }
    }
}
