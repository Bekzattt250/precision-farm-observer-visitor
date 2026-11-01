package farm.model;

import farm.visitor.Visitor;

import java.util.List;

public final class Plot extends CompositeElement<FarmElement> {
    public Plot(String id, List<FarmElement> items) {
        super(id, items);
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitPlot(this);
    }
}
