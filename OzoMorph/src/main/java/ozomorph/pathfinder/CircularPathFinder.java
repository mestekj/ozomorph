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
        
        // Add optimized middle U-turn (combines with trailing turns from original plan)
        addOptimizedUTurn(circularPlan, 2);
        
        // Add reversed actions in reverse order
        for (int i = originalPlan.size() - 1; i >= 0; i--) {
            Action reversedAction = reverseAction(originalPlan.get(i));
            circularPlan.add(reversedAction);
        }
        
        // Add optimized final U-turn (combines with any trailing turns)
        addOptimizedUTurn(circularPlan, 0);
        
        return circularPlan;
    }
    
    /**
     * Adds optimized U-turn to the plan by combining it with any trailing turn actions.
     * If the plan ends with turns that combine with the U-turn to make a simpler rotation,
     * replaces them with waits to preserve synchronization.
     * 
     * @param plan The circular plan to optimize
     * @param skipFromEnd Number of actions to skip from the end when searching for trailing turns
     */
    private void addOptimizedUTurn(List<Action> plan, int skipFromEnd) {
        // Calculate net rotation from trailing turn actions (in quarter turns, where 4 = 360°)
        int trailingTurns = 0;
        int turnCount = 0;
        
        // Count trailing turn actions from the end (skipping specified actions)
        for (int i = plan.size() - 1 - skipFromEnd; i >= 0; i--) {
            Action action = plan.get(i);
            if (action instanceof TurnLeftAction) {
                trailingTurns -= 1; // -90°
                turnCount++;
            } else if (action instanceof TurnRightAction) {
                trailingTurns += 1; // +90°
                turnCount++;
            } else {
                break; // Stop at first non-turn action
            }
        }
        
        // We need to add +2 quarter turns (U-turn) to the net rotation
        int targetRotation = trailingTurns + 2;
        
        // Normalize to [0, 4) range
        targetRotation = ((targetRotation % 4) + 4) % 4;
        
        // Remove trailing turns (accounting for skipped actions)
        for (int i = 0; i < turnCount; i++) {
            plan.remove(plan.size() - 1 - skipFromEnd);
        }
        
        // Add optimized turns to achieve target rotation
        // If target is 3 (270° right = 90° left), use one TurnLeft instead
        int actualTurns;
        if (targetRotation == 3) {
            plan.add(new TurnLeftAction(actionSettings.getTurnDuration()));
            actualTurns = 1;
        } else {
            for (int i = 0; i < targetRotation; i++) {
                plan.add(new TurnRightAction(actionSettings.getTurnDuration()));
            }
            actualTurns = targetRotation;
        }
        
        // Add wait actions to maintain desired plan length (turnCount + 2 total actions)
        int waitsNeeded = turnCount + 2 - actualTurns;
        for (int i = 0; i < waitsNeeded; i++) {
            plan.add(new WaitAction(actionSettings.getTurnDuration()));
        }
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
