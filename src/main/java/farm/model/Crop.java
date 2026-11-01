package farm.model;

import farm.visitor.Visitor;

public record Crop(String id, String name, double areaHectares,
                   double seedCostPerHectare, double expectedYieldTonsPerHectare) implements FarmElement {
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitCrop(this);
    }
}
