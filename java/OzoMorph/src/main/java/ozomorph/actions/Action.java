package ozomorph.actions;

import ozomorph.nodes.AgentMapNode;

public interface Action {
    /**
     * Duration of the action in seconds.
     * @return duration of the action in seconds
     */
    double getDuration();
    void apply(AgentMapNode agent, double deltaT);
}
