package ozobotscpf.app;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ozobotscpf.actions.ActionSettings;
import ozobotscpf.nodes.AgentMapNode;
import ozobotscpf.nodes.Group;
import ozobotscpf.pathfinder.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.UnaryOperator;

public class MainView {


    Logger logger = LoggerFactory.getLogger(MainView.class);

    @FXML
    ColorPicker cpGroupColor;
    @FXML
    Pane pInitials, pTargets;
    @FXML
    TextField tfHeight,tfWidth, tfForwardDuration, tfTurnDuration, tfWaitDuration;
    @FXML
    GridPane pDifferencies;
    @FXML
    Button btMorph, btSave, btPrint;

    MapController initialsMapController;
    MapController targetsMapController;
    DifferenciesTableController differenciesTableController;
    Map<Color, Group> groupsColors;

    int width,height;

    BooleanProperty isMapNotLoaded;

    public MainView() {
        groupsColors = new HashMap<>();
        isMapNotLoaded = new SimpleBooleanProperty(true);
    }

    @FXML
    public void initialize(){
        //text fields allows only numbers
        UnaryOperator<TextFormatter.Change> positiveIntegerFilter = change -> {
            String newText = change.getControlNewText();
            if(newText.isEmpty())
                return change;
            if (newText.matches("[1-9][0-9]*")) {
                return change;
            }
            return null;
        };

        tfHeight.setTextFormatter(new TextFormatter<>(positiveIntegerFilter));
        tfWidth.setTextFormatter(new TextFormatter<>(positiveIntegerFilter));

        UnaryOperator<TextFormatter.Change> positiveFloatFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty())
                return change;
            if (newText.matches("([1-9][0-9]*|0)(\\.[0-9]*)?")) {
                return change;
            }
            return null;
        };

        tfForwardDuration.setTextFormatter(new TextFormatter<>(positiveFloatFilter));
        //tfWaitDuration.setTextFormatter(new TextFormatter<>(positiveFloatFilter));
        tfTurnDuration.setTextFormatter(new TextFormatter<>(positiveFloatFilter));

        //set initial durations
        ActionSettings defaultDurations = new ActionSettings();
        tfForwardDuration.setText(String.valueOf(defaultDurations.getForwardDuration()));
        tfWaitDuration.setText(String.valueOf(defaultDurations.getWaitDuration()));
        tfTurnDuration.setText(String.valueOf(defaultDurations.getTurnDuration()));

        //other things
        cpGroupColor.setValue(Color.RED);
        btMorph.setDisable(true);

        btSave.disableProperty().bindBidirectional(isMapNotLoaded);
        btPrint.disableProperty().bindBidirectional(isMapNotLoaded);

        logger.info("MainView inited.");
    }

    public void createMap(ActionEvent actionEvent) {
        if(!isMapNotLoaded.getValue() && !confirmMapReload())
            return;
        //map not loaded or reload confirmed
        try {
            height = Integer.parseInt(tfHeight.getCharacters().toString());
            width = Integer.parseInt(tfWidth.getCharacters().toString());
            createMap(width,height);
            isMapNotLoaded.setValue(false);
        }
        catch (NumberFormatException e){
            logger.error("Width or height is not valid integer (probably empty string).");
            showError("Enter valid width and height (should be positive integers).");
        }
    }

    private void createMap(int width, int height){
        initialsMapController = new MapController(width, height, pInitials);
        targetsMapController = new MapController(width, height, pTargets);
        differenciesTableController = new DifferenciesTableController(pDifferencies,initialsMapController,targetsMapController, (group)-> {
            {
                cpGroupColor.setValue(group.getColor());
                groupColorChanged(null);
            }
        });
        btMorph.disableProperty().bindBidirectional(differenciesTableController.areDifferenciesProperty);
        groupColorChanged(null);
        logger.info("New blank map loaded.");
    }

    private void setSelectedGroup(Group group){
        if(initialsMapController != null)
            initialsMapController.setSelectedGroup(group);
        if(targetsMapController != null)
            targetsMapController.setSelectedGroup(group);
    }

    public void groupColorChanged(ActionEvent actionEvent)
    {
        Color color = cpGroupColor.getValue();
        Group selectedGroup =  groupsColors.computeIfAbsent(color, c-> new Group(color));
        setSelectedGroup(selectedGroup);
    }

    public void  startSimulation(){
        try {
            logger.info("Simulation window opening...");
            ActionSettings actionDurations = new ActionSettings(
                    Double.parseDouble(tfForwardDuration.getCharacters().toString()),
                    Double.parseDouble(tfTurnDuration.getCharacters().toString()),
                    Double.parseDouble(tfWaitDuration.getCharacters().toString())
            );
            PathFinder pathFinder = new PathFinder(actionDurations, ()->askForPicatExec());
            ProblemInstance problemInstance = new ProblemInstance(width, height, initialsMapController.getGroups(), targetsMapController.getGroups());
            problemInstance.validate();

            Alert picatRuning = new Alert(Alert.AlertType.INFORMATION,"Finding plans... \nClick Cancel to abort.", ButtonType.CANCEL);

            Thread t = new Thread(()->{
                try {
                    List<AgentMapNode> agents = pathFinder.findPaths(problemInstance);
                    Platform.runLater(() -> {
                        //picatRuning.close();
                        try {
                            openSimulationWindow(agents);
                        } catch (IOException e) {
                            logger.error("Showing of simulation window failed.", e);
                            showError("Simulation window cannot be shown due to an error, check logs for details.");
                        }
                    });
                }catch(NoPlansFoundException e){
                    logger.error("No plans found.");
                    showError("No plans found.");
                }catch (PicatNotFoundException e) {
                    logger.error("Picat executable not found.",e);
                    showError("Picat executable has not been found. Try adding the directory containing picat executable to the system variable PATH, or running the app from terminal.");
                }catch (InterruptedException e) {
                    logger.error("Picat thread interrupted.", e);
                    showError("No plans found because solver thread was interrupted.");
                } catch (IOException e) {
                    logger.error("Plan finding failed.",e);
                    showError("No plans found due to an error, check logs for details.");
                } finally {
                    Platform.runLater(()-> picatRuning.close());
                }
            });
            t.start();
            var res = picatRuning.showAndWait();
            if(res.isPresent()){
                logger.info("Aborting pathFinder.");
                pathFinder.stop();
            }

        } catch (NotEnoughInitialsException e){
            logger.error("Number of agents mismatch.", e);
            showDifferentAgentNumbersError(e.getNumberOfMissings());
        } catch (NoInitialsException e) {
            logger.error("No initial agents.", e);
            showError("No agents.");
        } catch (NumberFormatException e){
            logger.error("Duration of an action is not double format (probably empty string).");
            showError("Enter valid durations of actions.");
        }
    }

    private String askForPicatExecFromUIThread(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText("Picat executable not found, please find it manually.");
        alert.showAndWait();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Picat executable");
        File picatExec = fileChooser.showOpenDialog(pInitials.getScene().getWindow());
        String picatExecPath = "";
        if (picatExec != null) {
            try {
                picatExecPath =  picatExec.getCanonicalPath();
            }
            catch(IOException e){}
        }
        return picatExecPath;
    }

    private String askForPicatExec(){
        final FutureTask<String> query = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return askForPicatExecFromUIThread();
            }
        });
        Platform.runLater(query);
        try {
            return query.get();
        } catch (Exception e) {
            return null;
        }
    }

    private void showErrorUIThread(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message){
        if(Platform.isFxApplicationThread())
            showErrorUIThread(message);
        else
            Platform.runLater(()->{showErrorUIThread(message);});
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
        controller.init(width, height,agents, MapSettings.getSettings());

        return stage;
    }

    public void printMap(ActionEvent actionEvent) {
        PrintController pc = new PrintController(pInitials.getScene().getWindow(), MapSettings.getSettings(), width,height);
        pc.print();
    }

    public void loadMap(ActionEvent actionEvent) {
        if(!isMapNotLoaded.getValue() && !confirmMapReload())
            return;
        //map not loaded or reload confirmed
        //Show open file dialog
        FileChooser fileChooser = createMapFileChooser();
        File file = fileChooser.showOpenDialog(pInitials.getScene().getWindow());

        if(file != null){
            //load problemInstance
            ProblemInstance problemInstance = null;
            try (FileInputStream stream = new FileInputStream(file)) {
                try (ObjectInputStream in = new ObjectInputStream(stream)) {
                    problemInstance = (ProblemInstance) in.readObject();

                    //load maps
                    createMap(problemInstance.getWidth(),problemInstance.getHeight());
                    loadSavedMap(problemInstance);
                    isMapNotLoaded.setValue(false);
                }
            }
            catch (Exception e){
                logger.error("Error while loading map.", e);
                showError("Error while loading selected map.");
            }
        }
    }

    private void loadSavedMap(ProblemInstance problemInstance) {
        for (Group group : problemInstance.getInitialPositions().keySet()) {
            groupsColors.put(group.getColor(),group);
        }
        for (Group group : problemInstance.getTargetPositions().keySet()) {
            groupsColors.putIfAbsent(group.getColor(),group);
        }

        initialsMapController.load(problemInstance.getInitialPositions());
        targetsMapController.load(problemInstance.getTargetPositions());
        //set selected color
        groupColorChanged(null);
    }

    public void saveMap(ActionEvent actionEvent) {
        saveMap();
    }

    private boolean saveMap(){
        ProblemInstance problemInstance = new ProblemInstance(width, height, initialsMapController.getGroups(), targetsMapController.getGroups());
        FileChooser fileChooser = createMapFileChooser();

        //Show save file dialog
        File file = fileChooser.showSaveDialog(pInitials.getScene().getWindow());
        if(file != null){
            //save problemInstance
            try (FileOutputStream stream = new FileOutputStream(file)) {
                try (ObjectOutputStream out = new ObjectOutputStream(stream)) {
                    out.writeObject(problemInstance);
                    logger.info("Map saved to " + file.getCanonicalPath());
                    return true;
                }
            }
            catch (IOException e){
                logger.error("Error while saving map.", e);
                showError("Error while saving the map.");
            }
        }
        return false;
    }

    private FileChooser createMapFileChooser(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("OzoMorph map (*.ommap)", "*.ommap");
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser;
    }

    private boolean confirmMapReload(){
        ButtonType saveBtn = new ButtonType("Save");
        Alert alert = new Alert(Alert.AlertType.WARNING,"Current map will be lost.",ButtonType.OK,saveBtn,ButtonType.CANCEL);
        var result = alert.showAndWait();

        if(result.isPresent()){
            var buttonType = result.get();
            if(buttonType == ButtonType.OK)
                return true;
            if(buttonType == saveBtn){
                return saveMap();
            }
        }
        return false;
    }
}
