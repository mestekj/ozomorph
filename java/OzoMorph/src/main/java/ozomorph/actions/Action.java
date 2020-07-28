package ozomorph.actions;

import ozomorph.nodes.AgentMapNode;

/**
 * Action that agent can perform.
 */
public interface Action {
    /**
     * Duration of the action in seconds.
     * @return Duration of the action in seconds.
     */
    double getDuration();

    /**
     * Applies {@code deltaT/duration} of the action to given agent.
     * @param agent Agent to apply the action on.
     * @param deltaT Portion of action to apply.
     */
    void apply(AgentMapNode agent, double deltaT);
}
