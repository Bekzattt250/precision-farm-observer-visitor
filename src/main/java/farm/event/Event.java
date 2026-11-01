package farm.event;

/** Every event is immutable and carries its own type; listeners subscribe per type. */
public interface Event {
    EventType type();
}
