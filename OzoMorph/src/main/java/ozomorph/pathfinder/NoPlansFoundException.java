package ozomorph.pathfinder;

/**
 * Exception indicating that plans were not found for given configurations.
 */
public class NoPlansFoundException extends Exception {
    public NoPlansFoundException() {
        super("No plan exists for given input.");
    }
}
