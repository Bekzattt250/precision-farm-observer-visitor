package farm;

import farm.model.Crop;
import farm.model.Farm;
import farm.model.FarmElement;
import farm.model.Field;
import farm.model.Plot;
import farm.model.Pump;
import farm.model.Sensor;
import farm.model.SensorKind;
import farm.model.Valve;

import java.util.List;

/** Fixed demo data. Numbers are chosen so the tests can use hand-calculated values. */
public final class SampleFarm {
    public static final String FARM = "farm-green-valley";
    public static final String NORTH_FIELD = "field-north";
    public static final String SOUTH_FIELD = "field-south";
    public static final String NORTH_PLOT_1 = "plot-n1";
    public static final String NORTH_PLOT_2 = "plot-n2";
    public static final String SOUTH_PLOT_1 = "plot-s1";
    public static final String SOUTH_PLOT_2 = "plot-s2";

    private SampleFarm() {
    }

    public static Farm build() {
        Plot n1 = new Plot(NORTH_PLOT_1, List.<FarmElement>of(
                new Crop("crop-wheat", "Wheat", 10.0, 50.0, 3.0),
                new Pump("pump-n1", 500.0, 4.0, 100.0, true),
                new Sensor("sensor-n1", SensorKind.SOIL, 80, 30.0),
                new Valve("valve-n1", false, 20.0)));
        Plot n2 = new Plot(NORTH_PLOT_2, List.<FarmElement>of(
                new Crop("crop-corn", "Corn", 5.0, 80.0, 8.0),
                new Pump("pump-n2", 300.0, 2.0, 100.0, false),   // broken
                new Sensor("sensor-n2", SensorKind.SOIL, 10, 30.0), // low battery
                new Valve("valve-n2", true, 20.0)));              // stuck
        Plot s1 = new Plot(SOUTH_PLOT_1, List.<FarmElement>of(
                new Crop("crop-barley", "Barley", 8.0, 40.0, 4.0),
                new Pump("pump-s1", 400.0, 3.0, 100.0, true),
                new Sensor("sensor-s1", SensorKind.SOIL, 90, 30.0),
                new Valve("valve-s1", false, 20.0)));
        Plot s2 = new Plot(SOUTH_PLOT_2, List.<FarmElement>of(
                new Crop("crop-sunflower", "Sunflower", 2.0, 100.0, 2.0),
                new Pump("pump-s2", 200.0, 6.0, 100.0, true),
                new Sensor("sensor-s2", SensorKind.WEATHER, 55, 30.0)));
        return new Farm(FARM, List.of(
                new Field(NORTH_FIELD, List.of(n1, n2)),
                new Field(SOUTH_FIELD, List.of(s1, s2))));
    }
}
