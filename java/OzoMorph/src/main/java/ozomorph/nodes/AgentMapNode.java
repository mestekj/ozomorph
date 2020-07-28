package ozomorph.nodes;

import ozomorph.actions.Action;

import java.util.List;

/**
 * Representation of agent with already found plan.
 */
public class AgentMapNode implements MapNode {
    private int id;
    private List<Action> plan;
    private Group group;
    private double orientation,originalOrientation;
    /**
     * Current position.
     */
    private double x,y;
    /**
     * Starting position.
     */
    private int originalX,originalY;

    /**
     * Creates new instance of AgentMapNode.
     * @param positionMapNode Starting position of this agent.
     * @param id ID of this agent.
     * @param plan Plan of this agent.
     */
    public AgentMapNode(PositionMapNode positionMapNode, int id, List<Action> plan){
        this.id = id;
        this.plan = plan;
        this.group = positionMapNode.getGroup();
        this.originalOrientation = 0;
        this.originalX = positionMapNode.getGridX();
        this.originalY = positionMapNode.getGridY();
        resetPosition();
    }

    /**
     * Index of action in plan which is agent currently (while simulation) performing.
     */
    private int indexCurrentAction;
    /**
     * Time (seconds) how long the agent will yet perform the currentAction.
     * Remainting time from duration of the currentAction.
     */
    private double currentActionRemainingTime;

    /**
     * Gets current orientation of this agent.
     * Orientation is in [0,1), 0 means UP, increasing clockwise.
     * @return Current orientation of this agent.
     */
    public double getOrientation() {
        return orientation;
    }

    /**
     * Sets orientation of this agent.
     * Orientation is in [0,1), 0 means UP, increasing clockwise.
     * @param orientation New orientation of agent.
     */
    public void setOrientation(double orientation) {
        this.orientation = orientation % 1.0;
    }

    /**
     * Gets ID of this agent.
     * @return ID of this agent.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets plan of this agent.
     * @return Plan.
     */
    public List<Action> getPlan() {
        return plan;
    }

    /**
     * Gets group to which is this agent assigned.
     * @return Group to which is this agent assigned.
     */
    @Override
    public Group getGroup() {
        return group;
    }

    /**
     * Gets X coordinate of current position of this agent.
     * Number from 0 to (width of map - 1), increases from left to right.
     * Integer values means that agen is on a grid line.
     * @return X coordinate of current position.
     */
    @Override
    public double getX() {
        return x;
    }

    /**
     * Sets X coordinate of current position of this agent.
     * Number from 0 to (width of map - 1), increases from left to right.
     * Integer values means that agen is on a grid line.
     * @param x X coordinate of current position.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Gets Y coordinate of current position of this agent.
     * Number from 0 to (height of map - 1), increases from top to bottom.
     * Integer values means that agen is on a grid line.
     * @return Y coordinate of current position.
     */
    @Override
    public double getY() {
        return y;
    }

    /**
     * Sets Y coordinate of current position of this agent.
     * Number from 0 to (height of map - 1), increases from top to bottom.
     * Integer values means that agen is on a grid line.
     * @param y Y coordinate of current position.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Sets current position ({@code X}, {@code Y}, {@code Orientation}) to values of starting location.
     */
    public void resetPosition(){
        setX(originalX);
        setY(originalY);
        setOrientation(originalOrientation);
        indexCurrentAction = -1;
        currentActionRemainingTime = 0;
    }

    /**
     * Moves according to plan by given amount of time.
     * @param deltaTime Time to move by, in seconds.
     */
    public void move(double deltaTime){
        while(deltaTime > 0){
            if(currentActionRemainingTime <= 0){
                if(indexCurrentAction >= plan.size() -1)
                    //plan finished
                    return;
                indexCurrentAction++;
                currentActionRemainingTime = plan.get(indexCurrentAction).getDuration();
            }

            double timeCurrentAction = Math.min(deltaTime,currentActionRemainingTime);
            plan.get(indexCurrentAction).apply(this,timeCurrentAction);
            deltaTime -= timeCurrentAction;
            currentActionRemainingTime -= timeCurrentAction;
        }
    }
}
