package org.example;

public class ActionFactory {
    public Action createAction(String picatAction){
        switch (picatAction){
            case "goAhead":
                return new MoveAction(2);
            case "turnLeft":
                return new TurnLeftAction(2);
            case "turnRight":
                return new TurnRightAction(2);
            case "wait":
                return new WaitAction(2);
            default:
                throw new IllegalArgumentException(String.format("Unknown picat action: %s.", picatAction));
        }
    }
}
