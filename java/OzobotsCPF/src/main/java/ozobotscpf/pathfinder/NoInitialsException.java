package ozobotscpf.pathfinder;

public class NoInitialsException extends IllegalArgumentException {

    public NoInitialsException() {
        super("No agent in intials.");
    }
}
