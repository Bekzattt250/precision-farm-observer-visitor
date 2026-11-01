package farm.event;

public record SoilMoistureReading(String fieldId, String plotId, double moisturePercent) implements Event {
    @Override
    public EventType type() {
        return EventType.SOIL_MOISTURE;
    }
}
