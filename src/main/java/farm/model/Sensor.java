package farm.model;

import farm.visitor.Visitor;

public record Sensor(String id, SensorKind kind, int batteryPercent, double unitCost) implements FarmElement {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitSensor(this);
    }
}
