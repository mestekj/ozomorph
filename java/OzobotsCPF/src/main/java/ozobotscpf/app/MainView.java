package ozobotscpf.app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ozobotscpf.nodes.AgentMapNode;
import ozobotscpf.nodes.Group;
import ozobotscpf.pathfinder.NoInitialsException;
import ozobotscpf.pathfinder.NotEnoughInitialsException;
import ozobotscpf.pathfinder.PathFinder;
import ozobotscpf.pathfinder.ProblemInstance;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public class MainView {
    Logger logger = LoggerFactory.getLogger(MainView.class);

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
    Map<Color, Group> groupsColors;

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

        //other things
        cpGroupColor.setValue(Color.RED);

        logger.info("MainView inited.");
    }

    public void createMap(ActionEvent actionEvent) {
        height = Integer.parseInt(tfHeight.getCharacters().toString());
        width = Integer.parseInt(tfWidth.getCharacters().toString());
        initialsMapController = new MapController( width,height, pInitials);
        targetsMapController = new MapController( width,height, pTargets);
        logger.info("Initial and target configurations editors created.");
        btPaint.fire();
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
            logger.info("Simulation window opening...");
            PathFinder pathFinder = new PathFinder();
            ProblemInstance problemInstance = new ProblemInstance(width, height, initialsMapController.getGroups(), targetsMapController.getGroups());
            List<AgentMapNode> agents = pathFinder.findPaths(problemInstance);
            openSimulationWindow(agents);
        }catch (NotEnoughInitialsException e){
            logger.error("Number of agents mismatch.", e);
            showDifferentAgentNumbersError(e.getNumberOfMissings());
        } catch (NoInitialsException e){
            logger.error("No initial agents.", e);
            showError("No agents.");
        } catch (InterruptedException e) {
            logger.error("Picat thread interrupted.", e);
            showError("No plans found because solver thread was interrupted.");
        } catch (IOException e) {
            logger.error("Plan finding failed.",e);
            showError("No plans found due to an error, check logs for details.");
        }
    }

    private void showError(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showDifferentAgentNumbersError(Map<Group, Integer> differences){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Numbers of agents in groups are different.");
        GridPane table = createAgentNumberDifferencesGrid(differences);
        alert.getDialogPane().setContent(table);
        alert.showAndWait();
    }

    private GridPane createAgentNumberDifferencesGrid(Map<Group, Integer> differences){
        GridPane pane = new GridPane();
        pane.add(new Label("Group: "),0,0);
        pane.add(new Label("Number of lacking in initials: "),1,0);
        int row = 0;
        for (Map.Entry<Group, Integer> entry : differences.entrySet()) {
            row++;
            var r = new Rectangle(10,10);
            r.setFill(entry.getKey().getColor());
            pane.add(r,0,row);
            pane.add(new Label(entry.getValue().toString()), 1, row);
        }
        return pane;
    }

    private Stage openSimulationWindow(List<AgentMapNode> agents) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("simulationView.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene((Pane) loader.load()));
        stage.setTitle("Simulation");
        SimulationController controller = loader.getController();

        stage.show();
        controller.init(width, height,agents);

        return stage;
    }

    public void printMap(ActionEvent actionEvent) {
        PrintController pc = new PrintController(pInitials.getScene().getWindow(), MapSettings.getSettings(), width,height);
        pc.print();

        //TODO
    }
}
