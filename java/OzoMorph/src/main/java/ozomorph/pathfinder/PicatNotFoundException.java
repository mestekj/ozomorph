package ozomorph.pathfinder;

import java.io.IOException;

/**
 * Exception indicating that path to Picat runtime executable was not found.
 */
public class PicatNotFoundException extends IOException {
    public PicatNotFoundException(IOException e) {
        super("Picat executable not found.",e);
    }
}
