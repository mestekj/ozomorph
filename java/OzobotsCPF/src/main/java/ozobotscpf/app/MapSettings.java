package ozobotscpf.app;

public class MapSettings {
    private static MapSettings instance = new MapSettings();

    private double gridTickCm = 5;
    private double agentRadiusCm = 1.5;
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
