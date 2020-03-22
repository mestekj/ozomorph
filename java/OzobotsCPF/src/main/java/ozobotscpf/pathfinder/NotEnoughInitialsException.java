package ozobotscpf.pathfinder;

import ozobotscpf.nodes.Group;

import java.util.Map;

public class NotEnoughInitialsException extends IllegalArgumentException {
    public Map<Group, Integer> getNumberOfMissings() {
        return numberOfMissings;
    }

    public NotEnoughInitialsException(Map<Group, Integer> numberOfMissings) {
        this.numberOfMissings = numberOfMissings;
    }

    private Map<Group,Integer> numberOfMissings;
}
