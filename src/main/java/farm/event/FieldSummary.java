package farm.event;

public record FieldSummary(String fieldId, int samples, double averageMoisture) implements Event {
    @Override
    public EventType type() {
        return EventType.FIELD_SUMMARY;
    }
}
