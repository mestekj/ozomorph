package ozomorph.pathfinder;

import ozomorph.actions.Action;
import ozomorph.actions.ActionSettings;
import ozomorph.actions.TurnLeftAction;
import ozomorph.actions.TurnRightAction;
import ozomorph.actions.WaitAction;
import ozomorph.nodes.AgentMapNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Decorator for PathFinder that creates circular plans.
 * Agents go from initial configuration to target configuration and then back to initial.
 * Uses decorator pattern to extend any PathFinder implementation.
 */
public class CircularPathFinder implements PathFinder {
    private final PathFinder delegate;
    private final ActionSettings actionSettings;
    
    /**
     * Creates a new CircularPathFinder that wraps an existing PathFinder.
     * 
     * @param delegate The underlying PathFinder to use for finding the initial path
     * @param actionSettings Action settings needed to create reverse actions
     */
    public CircularPathFinder(PathFinder delegate, ActionSettings actionSettings) {
        this.delegate = delegate;
        this.actionSettings = actionSettings;
    }

    /**
     * Finds paths and makes them circular by appending reversed actions.
     * 
     * @param problemInstance Problem (initial and target configuration) to solve.
     * @param actionSettings Action settings for plan execution
     * @return Agents with circular plans (initial -> target -> initial)
     */
    @Override
    public List<AgentMapNode> findPaths(ProblemInstance problemInstance, ActionSettings actionSettings) 
            throws IOException, InterruptedException, NoPlansFoundException {
        // Get the original paths from the delegate
        List<AgentMapNode> agents = delegate.findPaths(problemInstance, actionSettings);
        
        // Make each agent's plan circular
        for (AgentMapNode agent : agents) {
            List<Action> originalPlan = agent.getPlan();
            List<Action> circularPlan = createCircularPlan(originalPlan);
            agent.setPlan(circularPlan);
        }
        
        return agents;
    }
    
    /**
     * Creates a circular plan by appending the reversed original plan.
     * Adds U-turns before and after the reversed sequence since Move always goes forward.
     * 
     * @param originalPlan The original plan from initial to target
     * @return Circular plan (original + wait + U-turn + reversed + wait + U-turn)
     */
    private List<Action> createCircularPlan(List<Action> originalPlan) {
        List<Action> circularPlan = new ArrayList<>(originalPlan);
        
        // Wait at target before turning around
        circularPlan.add(new WaitAction(actionSettings.getWaitDuration()));
        circularPlan.add(new WaitAction(actionSettings.getWaitDuration()));
        
        // Add U-turn (180 degrees = two 90-degree turns)
        circularPlan.add(new TurnRightAction(actionSettings.getTurnDuration()));
        circularPlan.add(new TurnRightAction(actionSettings.getTurnDuration()));
        
        // Add reversed actions in reverse order
        for (int i = originalPlan.size() - 1; i >= 0; i--) {
            Action reversedAction = reverseAction(originalPlan.get(i));
            circularPlan.add(reversedAction);
        }
        
        // Add another U-turn to face original direction
        circularPlan.add(new TurnRightAction(actionSettings.getTurnDuration()));
        circularPlan.add(new TurnRightAction(actionSettings.getTurnDuration()));
        
        return circularPlan;
    }
    
    /**
     * Creates the reverse of an action.
     * - TurnLeft becomes TurnRight
     * - TurnRight becomes TurnLeft
     * - Move remains Move (going back)
     * - Wait remains Wait
     * 
     * @param action The action to reverse
     * @return The reversed action
     */
    private Action reverseAction(Action action) {
        // Turn left reverses to turn right
        if (action instanceof TurnLeftAction) {
            return new TurnRightAction(action.getDuration());
        }
        // Turn right reverses to turn left
        else if (action instanceof TurnRightAction) {
            return new TurnLeftAction(action.getDuration());
        }
        // Move and Wait are their own reverses
        // (Move backwards is the same action in the reverse direction)
        else {
            // For Move and Wait, just return the same type of action
            // This works because after reversing turns, the agent faces the opposite direction
            // so Move will go back to the previous position
            return action;
        }
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public String getName() {
        return delegate.getName() + " (Circular)";
    }
}
