package ozomorph.actions;

import ozomorph.nodes.AgentMapNode;

/**
 * Action of agent that makes it do nothing.
 */
public class WaitAction extends ActionBase {
    public WaitAction(double duration) {
        super(duration);
    }

    @Override
    public void apply(AgentMapNode agent, double deltaT) {
        //do nothing
    }
}
