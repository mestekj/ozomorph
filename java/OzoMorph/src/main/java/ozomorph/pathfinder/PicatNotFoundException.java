package ozomorph.pathfinder;

import java.io.IOException;

public class PicatNotFoundException extends IOException {
    public PicatNotFoundException(IOException e) {
        super("Picat executable not found.",e);
    }
}
