package farm.listener;

import farm.core.BasicSubject;
import farm.core.Observer;
import farm.event.Event;
import farm.event.FieldSummary;
import farm.event.SoilMoistureReading;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener #3 AND subject: collects soil readings per field and republishes
 * one FieldSummary for every full window of readings (observer chain).
 * Only the small per-field window is locked, never the whole subject.
 */
public final class FieldAggregator extends BasicSubject<Event> implements Observer<Event> {
    private final int windowSize;
    private final Map<String, Window> windowsByField = new ConcurrentHashMap<>();

    public FieldAggregator(int windowSize) {
        if (windowSize < 1) {
            throw new IllegalArgumentException("windowSize must be >= 1");
        }
        this.windowSize = windowSize;
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof SoilMoistureReading reading)) {
            return;
        }
        Window window = windowsByField.computeIfAbsent(reading.fieldId(), field -> new Window());
        OptionalDouble average = window.add(reading.moisturePercent(), windowSize);
        // publish outside the synchronized block: listeners must never run while we hold a lock
        average.ifPresent(value -> publish(new FieldSummary(reading.fieldId(), windowSize, value)));
    }

    private static final class Window {
        private int count;
        private double sum;

        synchronized OptionalDouble add(double value, int windowSize) {
            sum += value;
            count++;
            if (count < windowSize) {
                return OptionalDouble.empty();
            }
            double average = sum / count;
            sum = 0;
            count = 0;
            return OptionalDouble.of(average);
        }
    }
}
