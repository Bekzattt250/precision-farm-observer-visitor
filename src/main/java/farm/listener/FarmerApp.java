package farm.listener;

import farm.core.Observer;
import farm.event.Event;
import farm.event.EventType;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Listener #5: the farmer's mobile app, keeps every notification it receives. */
public final class FarmerApp implements Observer<Event> {
    private final Queue<Event> received = new ConcurrentLinkedQueue<>();

    @Override
    public void onEvent(Event event) {
        received.add(event);
    }

    public List<Event> received() {
        return List.copyOf(received);
    }

    public List<Event> receivedOf(EventType type) {
        return received.stream().filter(event -> event.type() == type).toList();
    }
}
