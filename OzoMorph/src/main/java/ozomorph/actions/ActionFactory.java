package ozomorph.actions;

/**
 * Creates {@link Action} instance corresponding to name of the action as is used in solver.
 */
public class ActionFactory {
    private ActionSettings settings;

    /**
     * Creates new ActionFactory.
     * @param settings Duration of actions.
     */
    public ActionFactory(ActionSettings settings) {
        this.settings = settings;
    }

    /**
     * Creates new {@link Action} corresponding to Picat name of an action
     * @param picatAction Name of the action as is used in solver.
     * @return Action of the name.
     */
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
