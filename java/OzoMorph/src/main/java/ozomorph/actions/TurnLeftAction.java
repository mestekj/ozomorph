package ozomorph.actions;

/**
 * Action of agent that turns it 90 degrees to left.
 */
public class TurnLeftAction extends TurnActionBase {

    public TurnLeftAction(double duration) {
        super(duration);
    }

    @Override
    double getRotation() {
        return -0.25;
    }
}
