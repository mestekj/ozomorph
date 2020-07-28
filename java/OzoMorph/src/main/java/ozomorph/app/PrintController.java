package ozomorph.app;

import javafx.print.PrinterJob;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

import java.util.Collections;

/**
 * Controller that prints map on printer.
 */
public class PrintController {
    private MapSettings settings;
    private int width;
    private int height;
    private double dpi = 72;

    /**
     * Intializes new PrintController to print given map (complete grid).
     * @param settings Required properties of real map.
     * @param width Width of map (number of nodes).
     * @param height Height of map (number of nodes).
     */
    public PrintController(MapSettings settings, int width, int height) {
        this.settings = settings;
        this.width = width;
        this.height = height;
    }

    /**
     * Draw map on pane.
     * @return Pane with drown map.
     */
    protected Pane getMapToPrint() {
        Pane pane = new Pane();
        pane.setPrefSize(getGridTickPx() * (width + 1), getGridTickPx() * (height + 1));
        SimulationMapController smc = new SimulationMapController(width, height, pane, Collections.emptyList(), getGridTickPx(), 0, getGridLineWidthPx());
        return pane;
    }

    protected double getGridLineWidthPx() {
        return getPx(settings.getGridLineWidthCm());
    }

    protected double getGridTickPx() {
        return getPx(settings.getGridTickCm());
    }

    protected double getPx(double cm) {
        return cm / 2.54 * dpi;
    }

    /**
     * Print map on printer.
     * @param window Window where to show print dialogs.
     */
    public void print(Window window) {
        Pane pane = getMapToPrint();

        PrinterJob job = PrinterJob.createPrinterJob();

        if (job != null && job.showPrintDialog(window) && job.showPageSetupDialog(window)) {

            boolean success = job.printPage(pane);
            if (success)
                job.endJob();
        }
    }
}
