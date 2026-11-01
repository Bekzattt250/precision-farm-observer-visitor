package farm.visitor;

import farm.model.Crop;
import farm.model.FarmElement;
import farm.model.Pump;
import farm.model.Sensor;
import farm.model.Valve;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter visitor: returns the elements that need maintenance
 * (pump not operational, sensor battery too low, valve stuck). Stateless, safe to share between threads.
 */
public final class NeedsAttentionVisitor extends FoldingVisitor<List<FarmElement>> {
    private final int minBatteryPercent;

    public NeedsAttentionVisitor(int minBatteryPercent) {
        this.minBatteryPercent = minBatteryPercent;
    }

    @Override
    protected List<FarmElement> neutral() {
        return List.of();
    }

    @Override
    protected List<FarmElement> combine(List<FarmElement> left, List<FarmElement> right) {
        List<FarmElement> merged = new ArrayList<>(left);
        merged.addAll(right);
        return List.copyOf(merged);
    }

    @Override
    public List<FarmElement> visitCrop(Crop crop) {
        return List.of();
    }

    @Override
    public List<FarmElement> visitPump(Pump pump) {
        return flag(!pump.operational(), pump);
    }

    @Override
    public List<FarmElement> visitSensor(Sensor sensor) {
        return flag(sensor.batteryPercent() < minBatteryPercent, sensor);
    }

    @Override
    public List<FarmElement> visitValve(Valve valve) {
        return flag(valve.stuck(), valve);
    }

    private static List<FarmElement> flag(boolean needsAttention, FarmElement element) {
        return needsAttention ? List.of(element) : List.of();
    }
}
