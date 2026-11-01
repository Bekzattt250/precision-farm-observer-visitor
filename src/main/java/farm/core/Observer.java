package farm.core;

import farm.event.Event;

@FunctionalInterface
public interface Observer<E extends Event> {
    void onEvent(E event);
}
