package farm.listener;

import farm.core.BasicSubject;
import farm.core.Observer;
import farm.event.Event;
import farm.event.FrostWarning;
import farm.event.InspectionReport;
import farm.event.MaintenanceTicket;
import farm.model.FarmElement;
import farm.model.FarmIndex;
import farm.visitor.NeedsAttentionVisitor;

import java.util.List;

/**
 * INTEGRATION: Observer event -> Visitor on the related subtree only -> new events.
 * FrostWarning(field) -> NeedsAttentionVisitor runs on that field only -> InspectionReport (+ MaintenanceTicket).
 */
public final class InspectionService extends BasicSubject<Event> implements Observer<Event> {
    private final FarmIndex index;
    private final NeedsAttentionVisitor visitor;

    public InspectionService(FarmIndex index, NeedsAttentionVisitor visitor) {
        this.index = index;
        this.visitor = visitor;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof FrostWarning warning) {
            FarmElement subtree = index.find(warning.fieldId())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown field: " + warning.fieldId()));
            inspect(subtree);
        }
    }

    private void inspect(FarmElement subtree) {
        List<String> issues = subtree.accept(visitor).stream()
                .map(element -> element.getClass().getSimpleName() + " " + element.id())
                .toList();
        publish(new InspectionReport(subtree.id(), issues));
        if (!issues.isEmpty()) {
            publish(new MaintenanceTicket(subtree.id(), String.join("; ", issues)));
        }
    }
}
