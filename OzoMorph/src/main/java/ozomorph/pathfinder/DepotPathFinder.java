package ozomorph.pathfinder;

import ozomorph.actions.*;
import ozomorph.nodes.AgentMapNode;
import ozomorph.nodes.Group;
import ozomorph.nodes.PositionMapNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Solves depot-to-start, start-to-goal, and goal-to-depot paths.
 */
public class DepotPathFinder implements PathFinder {
    private final PathFinder delegate;
    private volatile boolean stopped;

    public DepotPathFinder(PathFinder delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<AgentMapNode> findPaths(ProblemInstance problem, ActionSettings settings)
            throws IOException, InterruptedException, NoPlansFoundException {
        if (problem.getAgentsCount() > (long) problem.getWidth() * problem.getHeight()) {
            throw new IOException("There are more agents than depot positions.");
        }

        List<AgentMapNode> middle = solve(problem, settings);
        int count = problem.getAgentsCount();
        if (middle.size() != count) {
            throw new IOException("Solver returned an unexpected number of agents.");
        }

        AgentMapNode[] byId = new AgentMapNode[count];
        PositionMapNode[] starts = new PositionMapNode[count];
        PositionMapNode[] goals = new PositionMapNode[count];
        PositionMapNode[] depots = new PositionMapNode[count];
        Set<Long> seenStarts = new HashSet<>();
        Set<Long> seenGoals = new HashSet<>();
        for (AgentMapNode agent : middle) {
            int id = agent.getId();
            if (id < 0 || id >= count || byId[id] != null) {
                throw new IOException("Solver returned invalid agent IDs.");
            }
            byId[id] = agent;
            starts[id] = position((int) agent.getX(), (int) agent.getY(), agent.getGroup());
            State goal = endpoint(starts[id], agent.getPlan(), problem);
            goals[id] = position(goal.x, goal.y, agent.getGroup());
            if (!containsPosition(problem.getInitialPositions().get(agent.getGroup()), starts[id])
                    || !containsPosition(problem.getTargetPositions().get(agent.getGroup()), goals[id])
                    || !seenStarts.add(key(starts[id], problem))
                    || !seenGoals.add(key(goals[id], problem))) {
                throw new IOException("Solver returned an invalid start or goal position.");
            }
            depots[id] = position(id % problem.getWidth(),
                    problem.getHeight() - 1 - id / problem.getWidth(), agent.getGroup());
        }

        List<List<Action>> outbound = solveIndividualLeg(problem, depots, starts, settings);
        List<List<Action>> inbound = solveIndividualLeg(problem, goals, depots, settings);
        List<List<Action>> first = new ArrayList<>();
        List<List<Action>> second = new ArrayList<>();
        List<List<Action>> third = new ArrayList<>();
        for (int id = 0; id < count; id++) {
            List<Action> firstLeg = new ArrayList<>(outbound.get(id));
            normalizeOrientation(firstLeg, depots[id], problem, settings);
            first.add(firstLeg);
            List<Action> secondLeg = new ArrayList<>(byId[id].getPlan());
            normalizeOrientation(secondLeg, starts[id], problem, settings);
            second.add(secondLeg);
            List<Action> thirdLeg = new ArrayList<>(inbound.get(id));
            normalizeOrientation(thirdLeg, goals[id], problem, settings);
            third.add(thirdLeg);
        }
        pad(first, settings);
        pad(second, settings);
        pad(third, settings);

        List<AgentMapNode> result = new ArrayList<>();
        for (int id = 0; id < count; id++) {
            List<Action> plan = first.get(id);
            plan.add(new StartAction());
            plan.addAll(second.get(id));
            plan.add(new AtGoalAction());
            plan.addAll(third.get(id));
            result.add(new AgentMapNode(depots[id], id, plan));
        }
        return result;
    }

    private List<AgentMapNode> solve(ProblemInstance problem, ActionSettings settings)
            throws IOException, InterruptedException, NoPlansFoundException {
        if (stopped) {
            throw new InterruptedException("Depot planning cancelled.");
        }
        List<AgentMapNode> result = delegate.findPaths(problem, settings);
        if (stopped) {
            throw new InterruptedException("Depot planning cancelled.");
        }
        return result;
    }

    private List<List<Action>> solveIndividualLeg(ProblemInstance problem, PositionMapNode[] from,
                                               PositionMapNode[] to, ActionSettings settings)
            throws IOException, InterruptedException, NoPlansFoundException {
        Map<Group, Set<PositionMapNode>> initials = new LinkedHashMap<>();
        Map<Group, Set<PositionMapNode>> targets = new LinkedHashMap<>();
        Map<Long, Integer> idsByPosition = new HashMap<>();
        for (int id = 0; id < from.length; id++) {
            Group group = new Group(from[id].getGroup().getColor());
            PositionMapNode initial = position(from[id].getGridX(), from[id].getGridY(), group);
            PositionMapNode target = position(to[id].getGridX(), to[id].getGridY(), group);
            initials.put(group, Collections.singleton(initial));
            targets.put(group, Collections.singleton(target));
            if (idsByPosition.put(key(initial, problem), id) != null) {
                throw new IOException("Multiple agents occupy the same position.");
            }
        }
        List<List<Action>> plans = new ArrayList<>(Collections.nCopies(from.length, null));
        ProblemInstance leg = new ProblemInstance(problem.getWidth(), problem.getHeight(), initials, targets);
        List<AgentMapNode> agents = solve(leg, settings);
        if (agents.size() != from.length) {
            throw new IOException("Solver returned an unexpected number of agents.");
        }
        for (AgentMapNode agent : agents) {
            PositionMapNode initial = position((int) agent.getX(), (int) agent.getY(), agent.getGroup());
            Integer id = idsByPosition.get(key(initial, problem));
            if (id == null || plans.get(id) != null) {
                throw new IOException("Solver returned an unexpected starting position.");
            }
            State end = endpoint(initial, agent.getPlan(), problem);
            if (end.x != to[id].getGridX() || end.y != to[id].getGridY()) {
                throw new IOException("Solver returned a plan to the wrong destination.");
            }
            plans.set(id, agent.getPlan());
        }
        return plans;
    }

    private void normalizeOrientation(List<Action> plan, PositionMapNode start, ProblemInstance problem,
                                      ActionSettings settings) throws IOException {
        int direction = endpoint(start, plan, problem).direction;
        if (direction == 1) {
            plan.add(new TurnLeftAction(settings.getTurnDuration()));
        } else if (direction == 2) {
            plan.add(new TurnRightAction(settings.getTurnDuration()));
            plan.add(new TurnRightAction(settings.getTurnDuration()));
        } else if (direction == 3) {
            plan.add(new TurnRightAction(settings.getTurnDuration()));
        }
    }

    private void pad(List<List<Action>> plans, ActionSettings settings) {
        int length = plans.stream().mapToInt(List::size).max().orElse(0);
        for (List<Action> plan : plans) {
            while (plan.size() < length) {
                plan.add(new WaitAction(settings.getWaitDuration()));
            }
        }
    }

    private State endpoint(PositionMapNode start, List<Action> plan, ProblemInstance problem) throws IOException {
        State state = new State(start.getGridX(), start.getGridY());
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};
        for (Action action : plan) {
            if (action instanceof TurnLeftAction) {
                state.direction = (state.direction + 3) % 4;
            } else if (action instanceof TurnRightAction) {
                state.direction = (state.direction + 1) % 4;
            } else if (action instanceof MoveAction) {
                state.x += dx[state.direction];
                state.y += dy[state.direction];
                if (state.x < 0 || state.x >= problem.getWidth() || state.y < 0 || state.y >= problem.getHeight()) {
                    throw new IOException("Solver returned a plan that leaves the grid.");
                }
            } else if (!(action instanceof WaitAction)) {
                throw new IOException("Solver returned an unsupported action: " + action.getClass().getName());
            }
        }
        return state;
    }

    private PositionMapNode position(int x, int y, Group group) {
        PositionMapNode node = new PositionMapNode(x, y);
        node.setGroup(group);
        return node;
    }

    private long key(PositionMapNode node, ProblemInstance problem) {
        return (long) node.getGridY() * problem.getWidth() + node.getGridX();
    }

    private boolean containsPosition(Set<PositionMapNode> positions, PositionMapNode expected) {
        return positions != null && positions.stream().anyMatch(node ->
                node.getGridX() == expected.getGridX() && node.getGridY() == expected.getGridY());
    }

    private static class State {
        int x;
        int y;
        int direction;

        State(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public void stop() {
        stopped = true;
        delegate.stop();
    }

    @Override
    public String getName() {
        return delegate.getName() + " (Depot)";
    }
}
