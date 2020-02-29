package org.example;

import java.util.List;

public class AgentMapNode implements MapNode {
    private int id;
    private List<Action> plan;
    private Group group;
    private double orientation,originalOrientation;
    private double x,y;
    private int originalX,originalY;

    public AgentMapNode(PositionMapNode positionMapNode, int id, List<Action> plan){
        this.id = id;
        this.plan = plan;
        this.group = positionMapNode.getGroup();
        this.originalOrientation = 0;
        this.originalX = positionMapNode.getGridX();
        this.originalY = positionMapNode.getGridY();
        resetPosition();
    }

    private int indexCurrentAction;
    private double currentActionRemainingTime;

    public double getOrientation() {
        return orientation;
    }

    public void setOrientation(double orientation) {
        this.orientation = orientation % 1.0;
    }

    public int getId() {
        return id;
    }

    public List<Action> getPlan() {
        return plan;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @Override
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void resetPosition(){
        setX(originalX);
        setY(originalY);
        setOrientation(originalOrientation);
        indexCurrentAction = -1;
        currentActionRemainingTime = 0;
    }

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
