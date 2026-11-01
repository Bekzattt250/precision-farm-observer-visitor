package farm.visitor;

import farm.model.Crop;
import farm.model.Pump;
import farm.model.Sensor;
import farm.model.Valve;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Accumulating visitor: besides the total it collects the cost per element type while walking.
 * It keeps state, so create ONE NEW instance per traversal.
 */
public final class CostVisitor extends FoldingVisitor<Double> {
    private final Map<String, Double> costByType = new TreeMap<>();

    public Map<String, Double> costByType() {
        return Collections.unmodifiableMap(costByType);
    }

    @Override
    protected Double neutral() {
        return 0.0;
    }

    @Override
    protected Double combine(Double left, Double right) {
        return left + right;
    }

    @Override
    public Double visitCrop(Crop crop) {
        return record("Crop", crop.areaHectares() * crop.seedCostPerHectare());
    }

    @Override
    public Double visitPump(Pump pump) {
        return record("Pump", pump.maintenanceCost());
    }

    @Override
    public Double visitSensor(Sensor sensor) {
        return record("Sensor", sensor.unitCost());
    }

    @Override
    public Double visitValve(Valve valve) {
        return record("Valve", valve.unitCost());
    }

    private double record(String type, double cost) {
        costByType.merge(type, cost, Double::sum);
        return cost;
    }
}
