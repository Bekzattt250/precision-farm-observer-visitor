package farm.model;

import farm.visitor.Visitor;

import java.util.List;

public final class Field extends CompositeElement<Plot> {
    public Field(String id, List<Plot> plots) {
        super(id, plots);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitField(this);
    }
}
