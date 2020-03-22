package ozobotscpf.actions;

public abstract class ActionBase implements Action {
    private double duration;

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
