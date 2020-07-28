package ozomorph.actions;

/**
 * Durations of actions.
 */
public class ActionSettings {
    private double forwardDuration;
    private double turnDuration;
    private double waitDuration;

    /**
     * Creates new ActionSettings using given durations.
     * @param forwardDuration Duration of "move forward" action, in seconds.
     * @param turnDuration Duration of "turn left/right" action, in seconds.
     * @param waitDuration Duration of "wait" action, in seconds.
     */
    public ActionSettings(double forwardDuration, double turnDuration, double waitDuration) {
        this.forwardDuration = forwardDuration;
        this.turnDuration = turnDuration;
        this.waitDuration = waitDuration;
    }

    /**
     * Creates new ActionSettings using default durations (1 second each action).
     */
    public ActionSettings(){
        this(1,1,1);
    }

    /**
     * Gets duration of "move forward" action, in seconds.
     * @return Duration of "move forward" action, in seconds.
     */
    public double getForwardDuration() {
        return forwardDuration;
    }

    /**
     * Gets duration of "turn left/right" action, in seconds.
     * @return Duration of "move forward" action, in seconds.
     */
    public double getTurnDuration() {
        return turnDuration;
    }

    /**
     * Gets duration of "wait" action, in seconds.
     * @return Duration of "move forward" action, in seconds.
     */
    public double getWaitDuration() {
        return waitDuration;
    }
}
