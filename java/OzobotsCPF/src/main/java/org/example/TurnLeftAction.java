package org.example;

public class TurnLeftAction extends TurnActionBase {

    public TurnLeftAction(double duration) {
        super(duration);
    }

    @Override
    double getRotation() {
        return -0.25;
    }
}
