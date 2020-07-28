package ozomorph.actions;

import ozomorph.nodes.AgentMapNode;

/**
 * Action of agent that turns it on place.
 */
public abstract class TurnActionBase extends ActionBase{

    public TurnActionBase(double duration) {
        super(duration);
    }

    /**
     * Number of rotations of the agent when this action is applied once.
     * Positive value means rotating clockwise, negative counterclockwise.
     * E.g. rotation = 1 means rotate clockwise by 360 degrees.
     * @return Number of rotations of the agent.
     */
    abstract double getRotation();

    @Override
    public void apply(AgentMapNode agent, double deltaT) {
        agent.setOrientation(agent.getOrientation() + getRotation()*effectFraction(deltaT));
    }
}
