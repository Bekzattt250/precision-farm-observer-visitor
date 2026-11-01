package farm.core;

import farm.event.Event;
import farm.event.EventType;
import farm.event.FrostWarning;
import farm.event.TemperatureReading;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicSubjectTest {

    private static final Event TEMPERATURE = new TemperatureReading("field-x", 1.0);

    @Test
    void deliversOnlyToObserversSubscribedToThatEventType() {
        BasicSubject<Event> subject = new BasicSubject<>();
        List<Event> temperatures = new ArrayList<>();
        List<Event> frosts = new ArrayList<>();
        subject.subscribe(EventType.TEMPERATURE, temperatures::add);
        subject.subscribe(EventType.FROST_WARNING, frosts::add);

        subject.publish(TEMPERATURE);

        assertEquals(List.of(TEMPERATURE), temperatures);
        assertEquals(List.of(), frosts);
    }

    @Test
    void unsubscribedObserverReceivesNothingMore() {
        BasicSubject<Event> subject = new BasicSubject<>();
        List<Event> received = new ArrayList<>();
        Observer<Event> observer = received::add;
        subject.subscribe(EventType.TEMPERATURE, observer);
        subject.publish(TEMPERATURE);

        subject.unsubscribe(EventType.TEMPERATURE, observer);
        subject.publish(TEMPERATURE);

        assertEquals(1, received.size());
    }

    @Test
    void observerCanUnsubscribeItselfInsideItsOwnHandler() {
        BasicSubject<Event> subject = new BasicSubject<>();
        OneShotObserver oneShot = new OneShotObserver(subject);
        List<Event> other = new ArrayList<>();
        subject.subscribe(EventType.TEMPERATURE, oneShot);
        subject.subscribe(EventType.TEMPERATURE, other::add);

        subject.publish(TEMPERATURE);
        subject.publish(TEMPERATURE);

        assertEquals(1, oneShot.calls);   // left after the first event
        assertEquals(2, other.size());    // the others were not disturbed
    }

    @Test
    void observerCanSubscribeAnotherObserverInsideItsHandler() {
        BasicSubject<Event> subject = new BasicSubject<>();
        List<Event> late = new ArrayList<>();
        subject.subscribe(EventType.TEMPERATURE, new Observer<Event>() {
            @Override
            public void onEvent(Event event) {
                subject.subscribe(EventType.TEMPERATURE, late::add);
            }
        });

        subject.publish(TEMPERATURE);   // late observer joins during this event, gets nothing yet
        subject.publish(TEMPERATURE);   // and receives from the next event on

        assertEquals(1, late.size());
    }

    @Test
    void sameObserverSubscribedTwiceIsCalledOnce() {
        BasicSubject<Event> subject = new BasicSubject<>();
        List<Event> received = new ArrayList<>();
        Observer<Event> observer = received::add;
        subject.subscribe(EventType.TEMPERATURE, observer);
        subject.subscribe(EventType.TEMPERATURE, observer);

        subject.publish(TEMPERATURE);

        assertEquals(1, received.size());
    }

    @Test
    void publishWithoutSubscribersDoesNothing() {
        BasicSubject<Event> subject = new BasicSubject<>();
        subject.publish(new FrostWarning("field-x", -2.0));   // must not throw
    }

    private static final class OneShotObserver implements Observer<Event> {
        private final BasicSubject<Event> subject;
        private int calls;

        OneShotObserver(BasicSubject<Event> subject) {
            this.subject = subject;
        }

        @Override
        public void onEvent(Event event) {
            calls++;
            subject.unsubscribe(EventType.TEMPERATURE, this);
        }
    }
}
