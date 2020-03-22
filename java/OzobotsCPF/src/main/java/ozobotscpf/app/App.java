package ozobotscpf.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("mainView"));
        stage.setScene(scene);
        stage.setTitle("Ozobots GroupMAPF");
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    static{
        //setting current.date system property to use it as log-file name
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        System.setProperty("current.date", dateFormat.format(new Date()));
    }

    public static void main(String[] args) {
        launch();
    }

}