package ozobotscpf.actions;

import ozobotscpf.nodes.AgentMapNode;

public class WaitAction extends ActionBase {
    public WaitAction(double duration) {
        super(duration);
    }

    @Override
    public void apply(AgentMapNode agent, double deltaT) {
        //do nothing
    }
}
