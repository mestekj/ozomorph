package ozomorph.actions;

/**
 * Implementation of {@link Action} using own duration for each instance.
 */
public abstract class ActionBase implements Action {
    private double duration;

    /**
     * New instance
     * @param duration Duration of action in seconds.
     */
    public ActionBase(double duration) {
        this.duration = duration;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    protected double effectFraction(double deltaT){
        return deltaT / getDuration();
    }
}
