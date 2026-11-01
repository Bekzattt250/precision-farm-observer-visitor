package farm.model;

import farm.visitor.Visitor;

public interface FarmElement {
    String id();

    /** Double dispatch entry point: the element chooses which visit method is called. */
    <R> R accept(Visitor<R> visitor);
}
