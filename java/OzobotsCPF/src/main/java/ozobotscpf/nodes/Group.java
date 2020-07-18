package ozobotscpf.nodes;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.Serializable;

public class Group implements Serializable {
    private static final long serialVersionUID = 20000001L;

    public Group(Color color) {
        this.color = color;
    }

    public Color getColor() { return color; }

    public void setColor(Color color) {
        this.color = color;
    }

    private transient Color color;

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeDouble(color.getRed());
        stream.writeDouble(color.getGreen());
        stream.writeDouble(color.getBlue());
        stream.writeDouble(color.getOpacity());
    }

    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        double r = stream.readDouble();
        double g = stream.readDouble();
        double b = stream.readDouble();
        double o = stream.readDouble();
        color = new Color(r,g,b,o);
    }
}
