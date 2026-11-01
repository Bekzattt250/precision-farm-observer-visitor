package farm.visitor;

import farm.model.Crop;
import farm.model.Pump;
import farm.model.Sensor;
import farm.model.Valve;

/** Numeric visitor: liters of water per day pumped by operational pumps. */
public final class WaterUsageVisitor extends FoldingVisitor<Double> {

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
        return 0.0;
    }

    @Override
    public Double visitPump(Pump pump) {
        return pump.operational() ? pump.flowLitersPerHour() * pump.hoursPerDay() : 0.0;
    }

    @Override
    public Double visitSensor(Sensor sensor) {
        return 0.0;
    }

    @Override
    public Double visitValve(Valve valve) {
        return 0.0;
    }
}
