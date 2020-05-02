package ozobotscpf.pathfinder;

import ozobotscpf.nodes.Group;
import ozobotscpf.nodes.PositionMapNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProblemInstance {
    private Map<Group, Set<PositionMapNode>> initialPositions, targetPositions;
    private int width,height;

    public ProblemInstance(int width, int height, Map<Group, Set<PositionMapNode>> initialPositions, Map<Group, Set<PositionMapNode>> targetPositions) throws NotEnoughInitialsException {

        removeEmptyGroups(initialPositions);
        removeEmptyGroups(targetPositions);

        var missing = validateInput(initialPositions,targetPositions);
        if(!missing.isEmpty())
            throw new NotEnoughInitialsException(missing);

        this.initialPositions = initialPositions;
        this.targetPositions = targetPositions;
        this.height = height;
        this.width = width;

        if(getAgentsCount() == 0)
            throw new NoInitialsException();
    }

    public Map<Group, Set<PositionMapNode>> getInitialPositions() {
        return initialPositions;
    }

    public Map<Group, Set<PositionMapNode>> getTargetPositions() {
        return targetPositions;
    }

    public int getAgentsCount(){
        return initialPositions.values().stream().mapToInt(Set::size).sum();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private Map<Group,Integer> validateInput(Map<Group, Set<PositionMapNode>> initialPositions, Map<Group, Set<PositionMapNode>> targetPositions){
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

    private void removeEmptyGroups(Map<Group, Set<PositionMapNode>> positions){
        positions.entrySet().removeIf(e -> e.getValue().isEmpty());
    }


}
