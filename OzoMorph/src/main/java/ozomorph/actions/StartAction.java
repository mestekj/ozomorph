package ozomorph.actions;

import ozomorph.nodes.AgentMapNode;

public class StartAction extends ActionBase {
    private static final long serialVersionUID = 34000001L;

    public StartAction() {
        super(1.0);
    }

    @Override
    public void apply(AgentMapNode agent, double deltaT) {
    }
}
