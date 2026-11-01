package farm.event;

public record FrostWarning(String fieldId, double celsius) implements Event {
    @Override
    public EventType type() {
        return EventType.FROST_WARNING;
    }
}
