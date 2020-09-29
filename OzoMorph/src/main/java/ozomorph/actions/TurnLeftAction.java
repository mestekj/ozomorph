package ozomorph.actions;

/**
 * Action of agent that turns it 90 degrees to left.
 */
public class TurnLeftAction extends TurnActionBase {
    private static final long serialVersionUID = 32100001L;

    public TurnLeftAction(double duration) {
        super(duration);
    }

    @Override
    double getRotation() {
        return -0.25;
    }
}
