package farm.event;

public record TemperatureReading(String fieldId, double celsius) implements Event {
    @Override
    public EventType type() {
        return EventType.TEMPERATURE;
    }
}
