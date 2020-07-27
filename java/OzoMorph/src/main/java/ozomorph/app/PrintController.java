package ozomorph.app;

import javafx.print.PrinterJob;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

import java.util.Collections;

public class PrintController {
    private Window window;
    private MapSettings settings;
    private int width;
    private int height;
    private double dpi = 72;

    public PrintController(Window window, MapSettings settings, int width, int height) {
        this.window = window;
        this.settings = settings;
        this.width = width;
        this.height = height;
    }

    public Pane getMapToPrint() {
        Pane pane = new Pane();
        pane.setPrefSize(getGridTickPx() * (width + 1), getGridTickPx() * (height + 1));
        SimulationMapController smc = new SimulationMapController(width, height, pane, Collections.emptyList(), getGridTickPx(), 0, getGridLineWidthPx());
        return pane;
    }

    private double getGridLineWidthPx() {
        return getPx(settings.getGridLineWidthCm());
    }

    private double getGridTickPx() {
        return getPx(settings.getGridTickCm());
    }

    private double getPx(double cm) {
        return cm / 2.54 * dpi;
    }

    public void print() {
        Pane pane = getMapToPrint();

        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null && job.showPrintDialog(window) && job.showPageSetupDialog(window)) {

            boolean success = job.printPage(pane);
            if (success)
                job.endJob();
        }
    }
}
