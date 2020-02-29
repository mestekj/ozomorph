package org.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProblemInstance {
    private Map<Group, Set<PositionMapNode>> initialPositions, targetPositions;
    private int width,height;

    public ProblemInstance(int width, int height, Map<Group, Set<PositionMapNode>> initialPositions, Map<Group, Set<PositionMapNode>> targetPositions) {

        removeEmptyGroups(initialPositions);
        removeEmptyGroups(targetPositions);

        var missing = validateInput(initialPositions,targetPositions);
        if(!missing.isEmpty())
            throw new NotEnoughInitialsException(missing);

        this.initialPositions = initialPositions;
        this.targetPositions = targetPositions;
        this.height = height;
        this.width = width;
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
        for (Map.Entry<Group, Set<PositionMapNode>> entry : targetPositions.entrySet()) {
            var group = entry.getKey();
            var targetsNumber = entry.getValue().size();
            var initialsNumber = initialPositions.getOrDefault(group, new HashSet<>()).size();
            if(initialsNumber < targetsNumber){
                missingNumber.put(group,targetsNumber-initialsNumber);
            }
        }
        return missingNumber;
    }

    private void removeEmptyGroups(Map<Group, Set<PositionMapNode>> positions){
        positions.entrySet().removeIf(e -> e.getValue().isEmpty());
    }


}
