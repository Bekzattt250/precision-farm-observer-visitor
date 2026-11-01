package farm.visitor;

import farm.model.CompositeElement;
import farm.model.Farm;
import farm.model.FarmElement;
import farm.model.Field;
import farm.model.Plot;

/**
 * The VISITOR does the traversal (elements hold data only). Containers are folded here once,
 * so every concrete visitor only says what a leaf is worth and how two results combine.
 */
public abstract class FoldingVisitor<R> implements Visitor<R> {

    protected abstract R neutral();

    protected abstract R combine(R left, R right);

    @Override
    public R visitFarm(Farm farm) {
        return fold(farm);
    }

    @Override
    public R visitField(Field field) {
        return fold(field);
    }

    @Override
    public R visitPlot(Plot plot) {
        return fold(plot);
    }

    private R fold(CompositeElement<?> node) {
        R result = neutral();
        for (FarmElement child : node.children()) {
            result = combine(result, child.accept(this));
        }
        return result;
    }
}
