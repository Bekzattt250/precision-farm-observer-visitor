package farm.sim;

import farm.core.Observer;
import farm.event.Event;
import farm.event.EventType;
import farm.listener.StatisticsListener;
import farm.source.SoilSensorSource;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StressTest {
    private static final int THREADS = 4;
    private static final int EVENTS_PER_THREAD = 1_000;

    @Test
    void fourThreadsTimesThousandEventsAreAllDelivered() throws InterruptedException {
        SoilSensorSource source = new SoilSensorSource();
        StatisticsListener statistics = new StatisticsListener();
        source.subscribe(EventType.SOIL_MOISTURE, statistics);

        publishConcurrently(source);

        assertEquals(THREADS * EVENTS_PER_THREAD, statistics.count(EventType.SOIL_MOISTURE));
    }

    @Test
    void subscribingAndUnsubscribingWhilePublishingLosesNothingForStableObservers() throws InterruptedException {
        SoilSensorSource source = new SoilSensorSource();
        StatisticsListener stable = new StatisticsListener();
        source.subscribe(EventType.SOIL_MOISTURE, stable);

        Observer<Event> transientObserver = event -> { };
        AtomicBoolean done = new AtomicBoolean(false);
        Thread toggler = new Thread(() -> {
            while (!done.get()) {
                source.subscribe(EventType.SOIL_MOISTURE, transientObserver);
                source.unsubscribe(EventType.SOIL_MOISTURE, transientObserver);
                Thread.onSpinWait();
            }
        });
        toggler.start();

        publishConcurrently(source);
        done.set(true);
        toggler.join();

        assertEquals(THREADS * EVENTS_PER_THREAD, stable.count(EventType.SOIL_MOISTURE));
    }

    private static void publishConcurrently(SoilSensorSource source) throws InterruptedException {
        ProducerPool pool = new ProducerPool(THREADS);
        for (int t = 0; t < THREADS; t++) {
            pool.submit(() -> {
                for (int i = 0; i < EVENTS_PER_THREAD; i++) {
                    source.emit("field-x", "plot-x", 40.0);
                }
            });
        }
        pool.shutdownAndAwait(Duration.ofSeconds(10));
    }
}
