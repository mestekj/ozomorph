package ozobotscpf.app;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

public class MapControllerBase {
    int width;
    int height;
    double gridTick;
    double gridLineWidth;
    Pane pane;

    public MapControllerBase(int width, int height, Pane pane, double gridTick) {
        this(width,height,pane,gridTick,1);
    }

    public MapControllerBase(int width, int height, Pane pane, double gridTick, double gridLineWidth) {
        this.width = width;
        this.height = height;
        this.pane = pane;
        this.gridTick = gridTick;
        this.gridLineWidth = gridLineWidth;

        pane.getChildren().clear();
        drawGridLines();
    }

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

    protected void handleError(String userMessage, Exception e){
       e.printStackTrace();
    }
}
