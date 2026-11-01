package farm.model;

import java.util.List;
import java.util.Objects;

/** Shared data of the container nodes: an id and an immutable list of children. */
public abstract class CompositeElement<C extends FarmElement> implements FarmElement {
    private final String id;
    private final List<C> children;

    protected CompositeElement(String id, List<C> children) {
        this.id = Objects.requireNonNull(id, "id");
        this.children = List.copyOf(children);
    }

    @Override
    public String id() {
        return id;
    }

    public List<C> children() {
        return children;
    }
}
