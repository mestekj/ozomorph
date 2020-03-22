module ozobotscpf {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;

    //only runtime dependency, but javafx-maven-plugin does not allow to manually add it to module-path
    requires ch.qos.logback.classic;

    opens ozobotscpf.app to javafx.fxml;
    exports ozobotscpf.app;
}