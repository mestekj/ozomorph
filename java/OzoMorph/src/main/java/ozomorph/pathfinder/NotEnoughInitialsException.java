package ozomorph.pathfinder;

import ozomorph.nodes.Group;

import java.util.Map;

public class NotEnoughInitialsException extends IllegalArgumentException {
    public Map<Group, Integer> getNumberOfMissings() {
        return numberOfMissings;
    }

    public NotEnoughInitialsException(Map<Group, Integer> numberOfMissings) {
        super("Number of agents difers.");
        this.numberOfMissings = numberOfMissings;
    }

    private Map<Group,Integer> numberOfMissings;
}
