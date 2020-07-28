package ozomorph.pathfinder;

/**
 * Exception indicating that there the initial/target configurations are empty.
 */
public class NoInitialsException extends IllegalArgumentException {

    public NoInitialsException() {
        super("No agent in intials.");
    }
}
