package farm.core;

import farm.event.Event;
import farm.event.EventType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe subject. No lock around the whole subject:
 * - the map is a ConcurrentHashMap (one entry per event type),
 * - each entry is a CopyOnWriteArrayList, so publish() iterates a snapshot.
 * That is why an observer may subscribe/unsubscribe (even itself) from inside its own handler
 * while other threads are publishing. A change takes effect from the next publish().
 */
public class BasicSubject<E extends Event> implements Subject<E> {

    private final Map<EventType, CopyOnWriteArrayList<Observer<? super E>>> observers = new ConcurrentHashMap<>();

    @Override
    public void subscribe(EventType type, Observer<? super E> observer) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(observer, "observer");
        observers.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>()).addIfAbsent(observer);
    }

    @Override
    public void unsubscribe(EventType type, Observer<? super E> observer) {
        List<Observer<? super E>> subscribed = observers.get(type);
        if (subscribed != null) {
            subscribed.remove(observer);
        }
    }

    protected void publish(E event) {
        List<Observer<? super E>> subscribed = observers.get(event.type());
        if (subscribed == null) {
            return;
        }
        for (Observer<? super E> observer : subscribed) {
            observer.onEvent(event);
        }
    }
}
