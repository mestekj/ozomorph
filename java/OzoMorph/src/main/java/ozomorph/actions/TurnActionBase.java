package ozomorph.actions;

import ozomorph.nodes.AgentMapNode;

public abstract class TurnActionBase extends ActionBase{

    public TurnActionBase(double duration) {
        super(duration);
    }

    abstract double getRotation();

    @Override
    public void apply(AgentMapNode agent, double deltaT) {
        agent.setOrientation(agent.getOrientation() + getRotation()*effectFraction(deltaT));
    }
}
