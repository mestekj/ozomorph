package ozomorph.nodes;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.Serializable;

/**
 * Representation of group in group MAPF problem. Group is represented by its color.
 */
public class Group implements Serializable {
    private static final long serialVersionUID = 20000001L;

    /**
     * Creates new group.
     * @param color Color of this group.
     */
    public Group(Color color) {
        this.color = color;
    }

    /**
     * Gets color which represents this group.
     * @return Color of this group.
     */
    public Color getColor() { return color; }

    private transient Color color;

    /**
     * Custom serialization (because javafx.scene.Color is not serializable). Group is exactly specified by the color.
     * @param stream Stream where to print the object.
     * @throws IOException IO error while writing serialized input.
     */
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeDouble(color.getRed());
        stream.writeDouble(color.getGreen());
        stream.writeDouble(color.getBlue());
        stream.writeDouble(color.getOpacity());
    }

    /**
     * Custom deserialization (because javafx.scene.Color is not serializable). Group is exactly specified by the color.
     * @param stream Stream where to print the object.
     * @throws IOException IO error while reading serialized input.
     */
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        double r = stream.readDouble();
        double g = stream.readDouble();
        double b = stream.readDouble();
        double o = stream.readDouble();
        color = new Color(r,g,b,o);
    }
}
