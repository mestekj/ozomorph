package ozomorph.app;

import ozomorph.nodes.AgentMapNode;

import java.io.Serializable;
import java.util.List;

public class SimulationData implements Serializable {
    private static final long serialVersionUID = 50000001L;

    List<AgentMapNode> agents;
    int width, height;

    /**
     * Data for simulation.
     * @param width Width of map (number of nodes).
     * @param height Width of map (number of nodes).
     * @param agents List of agents to simulate.
     */
    public SimulationData(List<AgentMapNode> agents, int width, int height) {
        this.agents = agents;
        this.width = width;
        this.height = height;
    }

    public List<AgentMapNode> getAgents() {
        return agents;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
