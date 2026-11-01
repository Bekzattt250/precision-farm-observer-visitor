package farm.model;

import farm.visitor.Visitor;

public record Valve(String id, boolean stuck, double unitCost) implements FarmElement {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitValve(this);
    }
}
