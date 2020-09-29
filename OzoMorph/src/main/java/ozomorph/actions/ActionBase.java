package ozomorph.actions;

import java.io.Serializable;

/**
 * Implementation of {@link Action} using own duration for each instance.
 */
public abstract class ActionBase implements Action, Serializable {
    private static final long serialVersionUID = 30000001L;
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
