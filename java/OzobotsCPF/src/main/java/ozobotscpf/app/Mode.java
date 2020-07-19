package ozobotscpf.app;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import ozobotscpf.nodes.AgentMapNode;

import java.util.ArrayList;

public enum Mode {
    SIMULATION ("Simulation") {
        @Override
        public Timeline getActivationSequence(Pane pane, SimulationController sc) {
            return new Timeline();
        }
    },
    ONSCREEN ("OnScreen"){
        @Override
        public Timeline getActivationSequence(Pane pane, SimulationController sc) {
            // red for 300 ms
            Rectangle cover = new Rectangle(pane.getWidth(), pane.getHeight(), Color.RED);
            Timeline activationSequence = new Timeline(
                    new KeyFrame(Duration.millis(0), ae -> pane.getChildren().add(cover)),
                    new KeyFrame(Duration.millis(300), ae -> pane.getChildren().remove(cover))
            );
            activationSequence.setCycleCount(1);
            return activationSequence;
        }
    },
    ONBOARD ("OnBoard"){
        @Override
        public Timeline getActivationSequence(Pane pane, SimulationController sc) {
            // To press countdown for each agent
            Timeline as = new Timeline();
            var kfs = as.getKeyFrames();

            Label lId = new Label();
            lId.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY,null,null)));
            ProgressBar pbRemaining = new ProgressBar();

            pane.getChildren().addAll(lId,pbRemaining);

            Double t = Double.valueOf(0);

            for (AgentMapNode agent : sc.getAgents()) {
                int agentId = agent.getId();
                t = addCountDown(t,kfs,
                        lId, "Start agent " + String.valueOf(agentId) + " in ",
                        pbRemaining, sc.simulationMapController.getGuiNodePosition(agent));
            }


            // time to get hands back
            t = addCountDown(t,kfs,lId, "Starting in ",pbRemaining, new Translate(lId.getLayoutX() + 80, lId.getLayoutY()));

            kfs.add(new KeyFrame(Duration.millis(t), ae-> pane.getChildren().removeAll(lId,pbRemaining)));

            as.setCycleCount(1);
            return as;
        }

        private double addCountDown(Double t, ObservableList<KeyFrame> kfs, Label lId, String text, ProgressBar pbRemaining, Transform progressBarPossition){
            kfs.add(new KeyFrame(Duration.millis(t), ae -> {
                lId.setText(text);
                lId.autosize();
                pbRemaining.getTransforms().setAll(progressBarPossition);
            }));

            //countdown to press
            for (int i = 100; i > 0; i--) {
                final int finalI = i;
                t +=50;
                kfs.add(new KeyFrame(Duration.millis(t), ae ->
                        pbRemaining.setProgress(finalI / 100.0)
                ));
            }
            return t;
        }
    };

    private String label;
    Mode(String label){
        this.label = label;
    }
    public abstract Timeline getActivationSequence(Pane pane, SimulationController sc);

    @Override
    public String toString() {
        return label;
    }
}
