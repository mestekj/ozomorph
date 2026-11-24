package ozomorph.pathfinder;

import javafx.scene.paint.Color;
import org.junit.Test;
import ozomorph.actions.*;
import ozomorph.nodes.AgentMapNode;
import ozomorph.nodes.Group;
import ozomorph.nodes.PositionMapNode;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class DepotPathFinderTest {
    private final ActionSettings settings = new ActionSettings();

    @Test
    public void preservesIdentityAndAlignsStagesAcrossReorderedResults() throws Exception {
        Group red = new Group(Color.RED);
        Group blue = new Group(Color.BLUE);
        Map<Group, Set<PositionMapNode>> starts = new LinkedHashMap<>();
        Map<Group, Set<PositionMapNode>> goals = new LinkedHashMap<>();
        starts.put(red, nodes(red, 0, 0, 1, 0));
        starts.put(blue, nodes(blue, 2, 0));
        goals.put(red, nodes(red, 0, 1, 1, 1));
        goals.put(blue, nodes(blue, 2, 1));
        ProblemInstance problem = new ProblemInstance(3, 3, starts, goals);
        FakeSolver solver = new FakeSolver();
        List<AgentMapNode> agents = new DepotPathFinder(solver).findPaths(problem, settings);

        assertEquals(3, solver.calls);
        int startIndex = -1;
        int goalIndex = -1;
        for (AgentMapNode agent : agents) {
            int id = agent.getId();
            assertEquals(id, (int) agent.getX());
            assertEquals(2, (int) agent.getY());
            assertSame(solver.middleGroups.get(id), agent.getGroup());
            List<Action> plan = agent.getPlan();
            int actualStart = indexOf(plan, StartAction.class);
            int actualGoal = indexOf(plan, AtGoalAction.class);
            if (startIndex < 0) {
                startIndex = actualStart;
                goalIndex = actualGoal;
            }
            assertEquals(startIndex, actualStart);
            assertEquals(goalIndex, actualGoal);
            assertTrue(actualStart < actualGoal);
            for (int i = 0; i < plan.size(); i++) {
                if (i == actualStart) {
                    assertPosition(agent, solver.middleStarts.get(id));
                    assertEquals(0.0, agent.getOrientation(), 0.00001);
                }
                if (i == actualGoal) {
                    assertPosition(agent, solver.middleGoals.get(id));
                    assertEquals(0.0, agent.getOrientation(), 0.00001);
                }
                agent.move(plan.get(i).getDuration());
            }
            assertEquals(id, agent.getX(), 0.00001);
            assertEquals(2, agent.getY(), 0.00001);
            assertEquals(0.0, agent.getOrientation(), 0.00001);
            agent.resetPosition();
            assertEquals(id, agent.getX(), 0.00001);
            assertEquals(2, agent.getY(), 0.00001);
        }
    }

    @Test
    public void fillsDepotRowsFromBottomUp() throws Exception {
        Group group = new Group(Color.RED);
        Map<Group, Set<PositionMapNode>> positions = new LinkedHashMap<>();
        positions.put(group, nodes(group, 0, 0, 1, 0, 0, 1));
        ProblemInstance problem = new ProblemInstance(2, 2, positions, new LinkedHashMap<>(positions));
        List<AgentMapNode> agents = new DepotPathFinder(new FakeSolver()).findPaths(problem, settings);
        assertEquals(0, agents.get(2).getX(), 0.0);
        assertEquals(0, agents.get(2).getY(), 0.0);
    }

    @Test
    public void rejectsOverCapacityBeforeCallingSolver() throws Exception {
        Group group = new Group(Color.RED);
        Map<Group, Set<PositionMapNode>> positions = new LinkedHashMap<>();
        positions.put(group, nodes(group, 0, 0, 0, 0));
        FakeSolver solver = new FakeSolver();
        try {
            new DepotPathFinder(solver).findPaths(new ProblemInstance(1, 1, positions, new LinkedHashMap<>(positions)), settings);
            fail("Expected capacity error");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("more agents"));
        }
        assertEquals(0, solver.calls);
    }

    @Test
    public void cancellationPreventsNextLeg() throws Exception {
        Group group = new Group(Color.RED);
        Map<Group, Set<PositionMapNode>> positions = new LinkedHashMap<>();
        positions.put(group, nodes(group, 0, 0));
        FakeSolver solver = new FakeSolver();
        DepotPathFinder finder = new DepotPathFinder(solver);
        solver.onSolved = finder::stop;
        try {
            finder.findPaths(new ProblemInstance(1, 1, positions, new LinkedHashMap<>(positions)), settings);
            fail("Expected interruption");
        } catch (InterruptedException expected) {
            assertEquals(1, solver.calls);
            assertTrue(solver.stopped);
        }
    }

    @Test
    public void solverFailureDoesNotReturnPartialPlans() throws Exception {
        Group group = new Group(Color.RED);
        Map<Group, Set<PositionMapNode>> positions = new LinkedHashMap<>();
        positions.put(group, nodes(group, 0, 0));
        FakeSolver solver = new FakeSolver() {
            @Override
            public List<AgentMapNode> findPaths(ProblemInstance problem, ActionSettings settings) throws NoPlansFoundException {
                if (calls == 1) throw new NoPlansFoundException();
                return super.findPaths(problem, settings);
            }
        };
        try {
            new DepotPathFinder(solver).findPaths(
                    new ProblemInstance(1, 1, positions, new LinkedHashMap<>(positions)), settings);
            fail("Expected solver failure");
        } catch (NoPlansFoundException expected) {
            assertEquals(1, solver.calls);
        }
    }

    private static int indexOf(List<Action> plan, Class<? extends Action> type) {
        int index = -1;
        for (int i = 0; i < plan.size(); i++) {
            if (type.isInstance(plan.get(i))) {
                assertEquals(-1, index);
                index = i;
            }
        }
        assertTrue(index >= 0);
        return index;
    }

    private static void assertPosition(AgentMapNode agent, PositionMapNode position) {
        assertEquals(position.getGridX(), agent.getX(), 0.00001);
        assertEquals(position.getGridY(), agent.getY(), 0.00001);
    }

    private static Set<PositionMapNode> nodes(Group group, int... coordinates) {
        Set<PositionMapNode> nodes = new LinkedHashSet<>();
        for (int i = 0; i < coordinates.length; i += 2) {
            PositionMapNode node = new PositionMapNode(coordinates[i], coordinates[i + 1]);
            node.setGroup(group);
            nodes.add(node);
        }
        return nodes;
    }

    private static class FakeSolver implements PathFinder {
        int calls;
        boolean stopped;
        Runnable onSolved;
        final Map<Integer, PositionMapNode> middleStarts = new HashMap<>();
        final Map<Integer, PositionMapNode> middleGoals = new HashMap<>();
        final Map<Integer, Group> middleGroups = new HashMap<>();

        @Override
        public List<AgentMapNode> findPaths(ProblemInstance problem, ActionSettings settings) throws NoPlansFoundException {
            calls++;
            List<AgentMapNode> result = new ArrayList<>();
            for (Map.Entry<Group, Set<PositionMapNode>> entry : problem.getInitialPositions().entrySet()) {
                List<PositionMapNode> targets = new ArrayList<>(problem.getTargetPositions().get(entry.getKey()));
                Collections.reverse(targets);
                int targetIndex = 0;
                for (PositionMapNode start : entry.getValue()) {
                    PositionMapNode goal = targets.get(targetIndex++);
                    int id = result.size();
                    if (calls == 1) {
                        middleStarts.put(id, start);
                        middleGoals.put(id, goal);
                        middleGroups.put(id, entry.getKey());
                    }
                    result.add(new AgentMapNode(start, id, path(start, goal)));
                }
            }
            Collections.reverse(result);
            if (onSolved != null) onSolved.run();
            return result;
        }

        private List<Action> path(PositionMapNode start, PositionMapNode goal) {
            List<Action> result = new ArrayList<>();
            int direction = 0;
            int x = start.getGridX(), y = start.getGridY();
            while (x != goal.getGridX() || y != goal.getGridY()) {
                int nextDirection = x < goal.getGridX() ? 1 : x > goal.getGridX() ? 3 : y < goal.getGridY() ? 2 : 0;
                while (direction != nextDirection) {
                    result.add(new TurnRightAction(1));
                    direction = (direction + 1) % 4;
                }
                result.add(new MoveAction(1));
                if (direction == 1) x++;
                if (direction == 3) x--;
                if (direction == 2) y++;
                if (direction == 0) y--;
            }
            return result;
        }

        @Override public void stop() { stopped = true; }
        @Override public String getName() { return "fake"; }
    }
}
