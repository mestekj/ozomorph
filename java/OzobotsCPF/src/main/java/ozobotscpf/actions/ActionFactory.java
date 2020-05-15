package ozobotscpf.actions;

public class ActionFactory {
    private ActionSettings settings;

    public ActionFactory(ActionSettings settings) {
        this.settings = settings;
    }

    public Action createAction(String picatAction){
        switch (picatAction){
            case "goAhead":
                return new MoveAction(settings.getForwardDuration());
            case "turnLeft":
                return new TurnLeftAction(settings.getTurnDuration());
            case "turnRight":
                return new TurnRightAction(settings.getTurnDuration());
            case "wait":
                return new WaitAction(settings.getWaitDuration());
            default:
                throw new IllegalArgumentException(String.format("Unknown picat action: %s.", picatAction));
        }
    }
}
