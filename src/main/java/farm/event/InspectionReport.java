package farm.event;

import java.util.List;

public record InspectionReport(String subtreeId, List<String> issues) implements Event {
    public InspectionReport {
        issues = List.copyOf(issues);
    }

    @Override
    public EventType type() {
        return EventType.INSPECTION_REPORT;
    }
}
