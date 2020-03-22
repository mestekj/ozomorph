package ozobotscpf.nodes;

import javafx.scene.paint.Color;

public class Group {

    public Group(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    private Color color;
}
