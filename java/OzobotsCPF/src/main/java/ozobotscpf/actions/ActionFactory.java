package ozobotscpf.actions;

public class ActionFactory {
    public Action createAction(String picatAction){
        switch (picatAction){
            case "goAhead":
                return new MoveAction(1);
            case "turnLeft":
                return new TurnLeftAction(1);
            case "turnRight":
                return new TurnRightAction(1);
            case "wait":
                return new WaitAction(1);
            default:
                throw new IllegalArgumentException(String.format("Unknown picat action: %s.", picatAction));
        }
    }
}
