package ozomorph.actions;

public class TurnRightAction extends TurnActionBase{

    public TurnRightAction(double duration) {
        super(duration);
    }

    @Override
    double getRotation() {
        return 0.25;
    }
}
