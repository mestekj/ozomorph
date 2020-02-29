package org.example;

public interface Action {
    double getDuration();
    void apply(AgentMapNode agent, double deltaT);
}
