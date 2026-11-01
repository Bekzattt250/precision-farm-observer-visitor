package farm.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Lookup id -> node, so an event can find "its" subtree. Built once, read-only afterwards. */
public final class FarmIndex {
    private final Map<String, FarmElement> byId;

    public FarmIndex(FarmElement root) {
        Map<String, FarmElement> collected = new HashMap<>();
        register(root, collected);
        this.byId = Map.copyOf(collected);
    }

    public Optional<FarmElement> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    private static void register(FarmElement element, Map<String, FarmElement> target) {
        if (target.put(element.id(), element) != null) {
            throw new IllegalArgumentException("Duplicate id in farm tree: " + element.id());
        }
        if (element instanceof CompositeElement<?> composite) {
            for (FarmElement child : composite.children()) {
                register(child, target);
            }
        }
    }
}
