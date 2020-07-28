package ozomorph.pathfinder;

import ozomorph.nodes.Group;
import ozomorph.nodes.PositionMapNode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Representation of group Multi-agent Path Finding problem for grid map.
 */
public class ProblemInstance implements Serializable {
    private static final long serialVersionUID = 10000001L;

    private Map<Group, Set<PositionMapNode>> initialPositions, targetPositions;
    private int width,height;

    /**
     * Creates new ProblemInstance.
     * @param width Width of grid map (number of nodes).
     * @param height Height of grid map (number of nodes).
     * @param initialPositions Initial configuration of agents.
     * @param targetPositions Target configuration of agents.
     */
    public ProblemInstance(int width, int height, Map<Group, Set<PositionMapNode>> initialPositions, Map<Group, Set<PositionMapNode>> targetPositions){

        removeEmptyGroups(initialPositions);
        removeEmptyGroups(targetPositions);

        this.initialPositions = initialPositions;
        this.targetPositions = targetPositions;
        this.height = height;
        this.width = width;


    }

    /**
     * Gets initial configuration of agents.
     * @return Initial configuration of agents.
     */
    public Map<Group, Set<PositionMapNode>> getInitialPositions() {
        return initialPositions;
    }

    /**
     * Gets target configuration of agents.
     * @return target configuration of agents.
     */
    public Map<Group, Set<PositionMapNode>> getTargetPositions() {
        return targetPositions;
    }

    /**
     * Gets number of agents in this problem instance.
     * @return Number of agents.
     */
    public int getAgentsCount(){
        return initialPositions.values().stream().mapToInt(Set::size).sum();
    }

    /**
     * Gets width of grid map.
     * @return Width of grid map (number of nodes).
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets width of grid map.
     * @return Width of grid map (number of nodes).
     */
    public int getHeight() {
        return height;
    }

    /**
     * Validate this problem instance.
     * @throws NotEnoughInitialsException Number of agents in a group is different in initial and target configuration.
     * @throws NoInitialsException No agents in this problem instance.
     */
    public void validate() throws NotEnoughInitialsException,NoInitialsException{
        var missing = findDifferencies(initialPositions,targetPositions);
        if(!missing.isEmpty())
            throw new NotEnoughInitialsException(missing);

        if(getAgentsCount() == 0)
            throw new NoInitialsException();
    }

    /**
     * Finds groups that have different number of agents in initial and target configuration.
     * @param initialPositions Initial configuration.
     * @param targetPositions Target configuration.
     * @return Groups with different numbers and the difference (target-initial).
     */
    private Map<Group,Integer> findDifferencies(Map<Group, Set<PositionMapNode>> initialPositions, Map<Group, Set<PositionMapNode>> targetPositions){
        Map<Group,Integer> missingNumber = new HashMap<>();
        Set<Group> usedGroups = new HashSet<>(initialPositions.keySet());
        usedGroups.addAll(targetPositions.keySet());

        for (Group group : usedGroups) {
            var targetsNumber = targetPositions.getOrDefault(group, new HashSet<>()).size();
            var initialsNumber = initialPositions.getOrDefault(group, new HashSet<>()).size();
            if(initialsNumber != targetsNumber){
                missingNumber.put(group,targetsNumber-initialsNumber);
            }
        }
        return missingNumber;
    }

    /**
     * Deletes from given configuration such groups that no agents are assigned to them.
     * @param positions Configuration to clean.
     */
    private void removeEmptyGroups(Map<Group, Set<PositionMapNode>> positions){
        positions.entrySet().removeIf(e -> e.getValue().isEmpty());
    }


}
