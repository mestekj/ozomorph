package ozobotscpf.actions;

public class ActionSettings {
    private double forwardDuration;
    private double turnDuration;
    private double waitDuration;

    public ActionSettings(double forwardDuration, double turnDuration, double waitDuration) {
        this.forwardDuration = forwardDuration;
        this.turnDuration = turnDuration;
        this.waitDuration = waitDuration;
    }

    public ActionSettings(){
        this(1,1,1);
    }

    public double getForwardDuration() {
        return forwardDuration;
    }

    public double getTurnDuration() {
        return turnDuration;
    }

    public double getWaitDuration() {
        return waitDuration;
    }
}
