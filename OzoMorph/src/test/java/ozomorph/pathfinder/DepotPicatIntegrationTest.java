package ozomorph.pathfinder;

import javafx.scene.paint.Color;
import org.junit.Test;
import ozomorph.actions.Action;
import ozomorph.actions.ActionSettings;
import ozomorph.actions.AtGoalAction;
import ozomorph.actions.StartAction;
import ozomorph.nodes.AgentMapNode;
import ozomorph.nodes.Group;
import ozomorph.nodes.PositionMapNode;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class DepotPicatIntegrationTest {
    @Test
    public void solvesAllThreeLegsWithPicat() throws Exception {
        String executable = System.getenv("OZOMORPH_TEST_PICAT");
        String workingDirectory = System.getenv("OZOMORPH_TEST_WORKDIR");
        assumeTrue(executable != null && new File(executable).isFile() && workingDirectory != null
                && new File(".").getCanonicalFile().equals(new File(workingDirectory).getCanonicalFile()));
        Group red = new Group(Color.RED);
        Group blue = new Group(Color.BLUE);
        Map<Group, Set<PositionMapNode>> starts = new LinkedHashMap<>();
        Map<Group, Set<PositionMapNode>> goals = new LinkedHashMap<>();
        starts.put(red, Set.of(node(0, 1, red)));
        starts.put(blue, Set.of(node(2, 1, blue)));
        goals.put(red, Set.of(node(0, 0, red)));
        goals.put(blue, Set.of(node(2, 0, blue)));
        ProblemInstance problem = new ProblemInstance(3, 3, starts, goals);
        PathFinder solver = new PicatPathFinder(executable, new File("../picat/model_split.pi"), () -> executable);
        List<AgentMapNode> agents = new DepotPathFinder(solver).findPaths(problem, new ActionSettings());
        assertEquals(2, agents.size());
        int maxSteps = agents.stream().mapToInt(agent -> agent.getPlan().size()).max().orElse(0);
        for (int step = 0; step < maxSteps; step++) {
            Set<String> occupied = new HashSet<>();
            for (AgentMapNode agent : agents) {
                if (step >= agent.getPlan().size()) {
                    assertTrue(occupied.add(agent.getX() + "," + agent.getY()));
                    continue;
                }
                Action action = agent.getPlan().get(step);
                if (action instanceof StartAction) {
                    assertEquals(agent.getGroup() == red ? 0 : 2, agent.getX(), 0.00001);
                    assertEquals(1, agent.getY(), 0.00001);
                } else if (action instanceof AtGoalAction) {
                    assertEquals(agent.getGroup() == red ? 0 : 2, agent.getX(), 0.00001);
                    assertEquals(0, agent.getY(), 0.00001);
                }
                agent.move(action.getDuration());
                assertTrue("Agents collide at step " + step,
                        occupied.add(agent.getX() + "," + agent.getY()));
            }
        }
        for (AgentMapNode agent : agents) {
            assertEquals(agent.getId(), agent.getX(), 0.00001);
            assertEquals(2, agent.getY(), 0.00001);
            assertEquals(0.0, agent.getOrientation(), 0.00001);
        }
    }

    private PositionMapNode node(int x, int y, Group group) {
        PositionMapNode result = new PositionMapNode(x, y);
        result.setGroup(group);
        return result;
    }
}
