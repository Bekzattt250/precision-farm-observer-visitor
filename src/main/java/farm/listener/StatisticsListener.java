package farm.listener;

import farm.core.Observer;
import farm.event.Event;
import farm.event.EventType;
import farm.event.SoilMoistureReading;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/** Listener #4: counts every event per type and the average soil moisture. */
public final class StatisticsListener implements Observer<Event> {
    // filled once in the constructor and never changed afterwards -> safe to read from many threads
    private final Map<EventType, LongAdder> counts = new EnumMap<>(EventType.class);
    private final DoubleAdder moistureSum = new DoubleAdder();

    public StatisticsListener() {
        for (EventType type : EventType.values()) {
            counts.put(type, new LongAdder());
        }
    }

    @Override
    public void onEvent(Event event) {
        counts.get(event.type()).increment();
        if (event instanceof SoilMoistureReading reading) {
            moistureSum.add(reading.moisturePercent());
        }
    }

    public long count(EventType type) {
        return counts.get(type).sum();
    }

    public long total() {
        return counts.values().stream().mapToLong(LongAdder::sum).sum();
    }

    /** Read it after the producers have finished; during a run it is only approximate. */
    public double averageMoisture() {
        long readings = count(EventType.SOIL_MOISTURE);
        return readings == 0 ? 0.0 : moistureSum.sum() / readings;
    }
}
