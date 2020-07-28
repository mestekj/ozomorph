package ozomorph.actions;

/**
 * Action of agent that turns it 90 degrees to right.
 */
public class TurnRightAction extends TurnActionBase{

    public TurnRightAction(double duration) {
        super(duration);
    }

    @Override
    double getRotation() {
        return 0.25;
    }
}
