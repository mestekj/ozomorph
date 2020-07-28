package ozomorph.pathfinder;

/**
 * Delegate to get file path.
 */
@FunctionalInterface
public interface GetPathCallback {
    String getPath();
}
