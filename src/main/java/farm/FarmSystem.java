package farm;

import farm.event.EventType;
import farm.listener.FarmerApp;
import farm.listener.FieldAggregator;
import farm.listener.FrostAlertListener;
import farm.listener.InspectionService;
import farm.listener.IrrigationController;
import farm.listener.StatisticsListener;
import farm.model.Farm;
import farm.model.FarmIndex;
import farm.sim.ProducerPool;
import farm.sim.Simulation;
import farm.sim.SimulationConfig;
import farm.source.SoilSensorSource;
import farm.source.WeatherStationSource;
import farm.visitor.CostVisitor;
import farm.visitor.JsonExportVisitor;
import farm.visitor.NeedsAttentionVisitor;
import farm.visitor.WaterUsageVisitor;

/**
 * THE composition root: the only place that uses "new" for the application parts and wires them
 * together. Everything else receives its dependencies through the constructor.
 *
 * Wiring (observer chains):
 *   SoilSensorSource    -> IrrigationController, FieldAggregator -> FarmerApp, StatisticsListener
 *   WeatherStationSource-> FrostAlertListener -> InspectionService -> FarmerApp
 *                                           \-> FarmerApp, StatisticsListener
 */
public final class FarmSystem {
    static final double DRY_THRESHOLD_PERCENT = 30.0;
    static final double FROST_BELOW_CELSIUS = 0.0;
    static final int SUMMARY_WINDOW = 10;
    static final int MIN_BATTERY_PERCENT = 20;

    private final Farm farm;
    private final SoilSensorSource soilSource;
    private final WeatherStationSource weatherSource;
    private final IrrigationController irrigation;
    private final StatisticsListener statistics;
    private final FarmerApp farmerApp;
    private final Simulation simulation;

    public FarmSystem(SimulationConfig config) {
        this.farm = SampleFarm.build();
        FarmIndex index = new FarmIndex(farm);

        this.soilSource = new SoilSensorSource();
        this.weatherSource = new WeatherStationSource();
        this.irrigation = new IrrigationController(DRY_THRESHOLD_PERCENT);
        this.statistics = new StatisticsListener();
        this.farmerApp = new FarmerApp();
        FrostAlertListener frostAlert = new FrostAlertListener(FROST_BELOW_CELSIUS);
        FieldAggregator aggregator = new FieldAggregator(SUMMARY_WINDOW);
        InspectionService inspection = new InspectionService(index, new NeedsAttentionVisitor(MIN_BATTERY_PERCENT));

        soilSource.subscribe(EventType.SOIL_MOISTURE, irrigation);
        soilSource.subscribe(EventType.SOIL_MOISTURE, aggregator);
        soilSource.subscribe(EventType.SOIL_MOISTURE, statistics);

        weatherSource.subscribe(EventType.TEMPERATURE, frostAlert);
        weatherSource.subscribe(EventType.TEMPERATURE, statistics);

        frostAlert.subscribe(EventType.FROST_WARNING, inspection);
        frostAlert.subscribe(EventType.FROST_WARNING, farmerApp);
        frostAlert.subscribe(EventType.FROST_WARNING, statistics);

        aggregator.subscribe(EventType.FIELD_SUMMARY, farmerApp);
        aggregator.subscribe(EventType.FIELD_SUMMARY, statistics);

        inspection.subscribe(EventType.INSPECTION_REPORT, farmerApp);
        inspection.subscribe(EventType.INSPECTION_REPORT, statistics);
        inspection.subscribe(EventType.MAINTENANCE_TICKET, farmerApp);
        inspection.subscribe(EventType.MAINTENANCE_TICKET, statistics);

        this.simulation = new Simulation(config, new ProducerPool(config.producerThreads()), soilSource, weatherSource);
    }

    /** Runs all producers and returns only when every event has been delivered. Call once. */
    public void run() throws InterruptedException {
        simulation.run();
    }

    public Farm farm() {
        return farm;
    }

    public IrrigationController irrigation() {
        return irrigation;
    }

    public StatisticsListener statistics() {
        return statistics;
    }

    public FarmerApp farmerApp() {
        return farmerApp;
    }

    public double dailyWaterUsageLiters() {
        return farm.accept(new WaterUsageVisitor());
    }

    public double totalCost() {
        return farm.accept(new CostVisitor());
    }

    public String farmAsJson() {
        return farm.accept(new JsonExportVisitor());
    }
}
