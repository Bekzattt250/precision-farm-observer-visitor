package farm.event;

public record MaintenanceTicket(String subtreeId, String description) implements Event {
    @Override
    public EventType type() {
        return EventType.MAINTENANCE_TICKET;
    }
}
