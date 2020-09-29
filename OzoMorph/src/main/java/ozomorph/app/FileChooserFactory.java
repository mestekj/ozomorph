package ozomorph.app;

import javafx.stage.FileChooser;

public class FileChooserFactory {
    public static FileChooser createPlansFileChooser(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("OzoMorph plans (*.omplans)", "*.omplans");
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser;
    }

    /**
     * Creates FileChooser instantiated for OzoMorph map file type.
     * @return
     */
    public static FileChooser createMapFileChooser(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("OzoMorph map (*.ommap)", "*.ommap");
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser;
    }
}
