package ozomorph.app;

/**
 * Default properties of real map according to physical characteristics of Ozobots.
 */
public class MapSettings {
    private static MapSettings instance = new MapSettings();

    /**
     * Spacing between grid lines (length of an edge) in centimetres.
     */
    private double gridTickCm = 5;
    /**
     * Radius of agent (Ozobot) in centimetres.
     */
    private double agentRadiusCm = 1.5;
    /**
     * Width of grid line in centimetres.
     */
    private double gridLineWidthCm = 0.5;

    public double getGridTickCm() {
        return gridTickCm;
    }

    public double getAgentRadiusCm() {
        return agentRadiusCm;
    }

    public double getGridLineWidthCm() {
        return gridLineWidthCm;
    }

    public static MapSettings getSettings(){
        return instance;
    }
}
