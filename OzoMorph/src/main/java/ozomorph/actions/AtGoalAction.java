package ozomorph.actions;

import ozomorph.nodes.AgentMapNode;

public class AtGoalAction extends ActionBase {
    private static final long serialVersionUID = 35000001L;

    public AtGoalAction() {
        super(1.0);
    }

    @Override
    public void apply(AgentMapNode agent, double deltaT) {
    }
}
