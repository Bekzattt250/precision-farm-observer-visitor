package farm.sim;

import farm.SampleFarm;
import farm.source.SoilSensorSource;
import farm.source.WeatherStationSource;

import java.time.Duration;
import java.util.function.IntConsumer;

/** 4 producer tasks (2 soil, 2 weather) run on the pool; each emits eventsPerProducer readings. */
public final class Simulation {
    private final SimulationConfig config;
    private final ProducerPool pool;
    private final SoilSensorSource soil;
    private final WeatherStationSource weather;

    public Simulation(SimulationConfig config, ProducerPool pool,
                      SoilSensorSource soil, WeatherStationSource weather) {
        this.config = config;
        this.pool = pool;
        this.soil = soil;
        this.weather = weather;
    }

    /** Can be called only once: the pool is shut down at the end. */
    public void run() throws InterruptedException {
        int n = config.eventsPerProducer();
        // soil: 20..59 %, cycle of 40  -> 10 of every 40 readings are below a 30 % threshold
        pool.submit(() -> repeat(n, i -> soil.emit(SampleFarm.NORTH_FIELD, SampleFarm.NORTH_PLOT_1, 20.0 + i % 40)));
        pool.submit(() -> repeat(n, i -> soil.emit(SampleFarm.SOUTH_FIELD, SampleFarm.SOUTH_PLOT_1, 20.0 + i % 40)));
        // temperature: 5..-4 C, cycle of 10 -> 4 of every 10 readings are below 0 C
        pool.submit(() -> repeat(n, i -> weather.emit(SampleFarm.NORTH_FIELD, 5.0 - i % 10)));
        pool.submit(() -> repeat(n, i -> weather.emit(SampleFarm.SOUTH_FIELD, 5.0 - i % 10)));
        pool.shutdownAndAwait(Duration.ofSeconds(30));
    }

    private static void repeat(int times, IntConsumer action) {
        for (int i = 0; i < times; i++) {
            action.accept(i);
        }
    }
}
