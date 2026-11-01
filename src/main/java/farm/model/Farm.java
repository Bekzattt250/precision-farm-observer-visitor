package farm.model;

import farm.visitor.Visitor;

import java.util.List;

public final class Farm extends CompositeElement<Field> {
    public Farm(String id, List<Field> fields) {
        super(id, fields);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitFarm(this);
    }
}
