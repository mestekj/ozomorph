package ozobotscpf.pathfinder;

public class NoPlansFoundException extends IllegalArgumentException {
    public NoPlansFoundException() {
        super("No plan exists for given input.");
    }
}
