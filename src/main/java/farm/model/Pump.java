package farm.model;

import farm.visitor.Visitor;

public record Pump(String id, double flowLitersPerHour, double hoursPerDay,
                   double maintenanceCost, boolean operational) implements FarmElement {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitPump(this);
    }
}
