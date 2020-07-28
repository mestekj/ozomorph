package ozomorph.actions;

import ozomorph.nodes.AgentMapNode;

/**
 * Action of agent that moves it to next map node in its direction.
 */
public class MoveAction extends ActionBase {

    final double[] dX = {0,1,0,-1};
    final double[] dY = {-1,0,1,0,};

    public MoveAction(double duration) {
        super(duration);
    }

    @Override
    public void apply(AgentMapNode agent, double deltaT) {
        int di = Math.floorMod ((int) Math.round(agent.getOrientation() * 4), 4);
        double distanceFraction = effectFraction(deltaT);
        agent.setX(agent.getX() + distanceFraction * dX[di]);
        agent.setY(agent.getY() + distanceFraction * dY[di]);
    }
}
