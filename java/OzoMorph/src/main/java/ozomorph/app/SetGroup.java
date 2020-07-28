package ozomorph.app;

import ozomorph.nodes.Group;

/**
 * Delegate to setGroup method.
 */
@FunctionalInterface
public interface SetGroup {
    void setGroup(Group g);
}
