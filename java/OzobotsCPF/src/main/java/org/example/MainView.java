package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public class MainView {

    @FXML
    ColorPicker cpGroupColor;
    @FXML
    ToggleGroup groupsTools;
    @FXML
    ToggleButton btPaint;
    @FXML
    Pane pInitials, pTargets;
    @FXML
    TextField tfHeight,tfWidth;

    MapController initialsMapController;
    MapController targetsMapController;
    Map<Color,Group> groupsColors;

    int width,height;

    public MainView() {
        groupsColors = new HashMap<>();
    }

    @FXML
    public void initialize(){
        //text fields allows only numbers
        UnaryOperator<TextFormatter.Change> positiveIntegerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[1-9][0-9]*")) {
                return change;
            }
            return null;
        };

        tfHeight.setTextFormatter(new TextFormatter<>(positiveIntegerFilter));
        tfWidth.setTextFormatter(new TextFormatter<>(positiveIntegerFilter));


    }

    public void createMap(ActionEvent actionEvent) {
        height = Integer.parseInt(tfHeight.getCharacters().toString());
        width = Integer.parseInt(tfWidth.getCharacters().toString());
        initialsMapController = new MapController( width,height, pInitials);
        targetsMapController = new MapController( width,height, pTargets);
    }


    public void setNoGroup(ActionEvent actionEvent) {
        setSelectedGroup(null);
    }

    public void setGroup(ActionEvent actionEvent) {
        Color color = cpGroupColor.getValue();
        Group selectedGroup =  groupsColors.computeIfAbsent(color, c-> new Group(color));
        setSelectedGroup(selectedGroup);
    }

    private void setSelectedGroup(Group group){
        initialsMapController.setSelectedGroup(group);
        targetsMapController.setSelectedGroup(group);
    }

    public void groupColorChanged(ActionEvent actionEvent) {
        btPaint.fire();
    }



    public void  startSimulation(){
        try {
            PathFinder pathFinder = new PathFinder();
            ProblemInstance problemInstance = new ProblemInstance(width,height, initialsMapController.getGroups(), targetsMapController.getGroups());
            List<AgentMapNode> agents = pathFinder.findPaths(problemInstance);
            openSimulationWindow(agents);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private Stage openSimulationWindow(List<AgentMapNode> agents) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("simulationView.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene((Pane) loader.load()));
        SimulationController controller = loader.getController();

        controller.init(width, height,agents);

        stage.show();
        return stage;
    }
}
