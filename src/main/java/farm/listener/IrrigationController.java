package farm.listener;

import farm.core.Observer;
import farm.event.Event;
import farm.event.SoilMoistureReading;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/** Listener #1: counts "start irrigation" commands per plot when the soil is too dry. */
public final class IrrigationController implements Observer<Event> {
    private final double dryThresholdPercent;
    private final Map<String, LongAdder> commandsByPlot = new ConcurrentHashMap<>();

    public IrrigationController(double dryThresholdPercent) {
        this.dryThresholdPercent = dryThresholdPercent;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof SoilMoistureReading reading && reading.moisturePercent() < dryThresholdPercent) {
            commandsByPlot.computeIfAbsent(reading.plotId(), plot -> new LongAdder()).increment();
        }
    }

    public long commandsFor(String plotId) {
        LongAdder commands = commandsByPlot.get(plotId);
        return commands == null ? 0 : commands.sum();
    }

    public long totalCommands() {
        return commandsByPlot.values().stream().mapToLong(LongAdder::sum).sum();
    }
}
