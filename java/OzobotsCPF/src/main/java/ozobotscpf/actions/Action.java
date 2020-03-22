package ozobotscpf.actions;

import ozobotscpf.nodes.AgentMapNode;

public interface Action {
    double getDuration();
    void apply(AgentMapNode agent, double deltaT);
}
