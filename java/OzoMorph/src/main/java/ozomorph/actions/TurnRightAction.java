package ozomorph.actions;

/**
 * Action of agent that turns it 90 degrees to right.
 */
public class TurnRightAction extends TurnActionBase{
    private static final long serialVersionUID = 32200001L;

    public TurnRightAction(double duration) {
        super(duration);
    }

    @Override
    double getRotation() {
        return 0.25;
    }
}
