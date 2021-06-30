package ozomorph.pathfinder;

import ozomorph.actions.ActionSettings;
import ozomorph.nodes.AgentMapNode;

import java.io.IOException;
import java.util.List;

public interface PathFinder {
    /**
     * Returns solution to given problemInstance in form of list agents having the found plans.
     *
     * @param problemInstance Problem (initial and target configuration) to solve.
     * @return Agents with already set plans.
     * @throws IOException            IO error.
     * @throws InterruptedException   Solver runtime interupted.
     * @throws NoPlansFoundException  Given problem was not solved (maybe solver terminated?).
     * @throws PicatNotFoundException Executable of Picat runtime not found.
     */
    List<AgentMapNode> findPaths(ProblemInstance problemInstance, ActionSettings actionSettings) throws IOException, InterruptedException, NoPlansFoundException;

    /**
     * Terminates solver (if running).
     */
    void stop();

    /**
     * Returns name of this PathFinder to be displayed to user.
     */
    String getName();
}
