package farm.listener;

import farm.core.BasicSubject;
import farm.core.Observer;
import farm.event.Event;
import farm.event.FrostWarning;
import farm.event.TemperatureReading;

/** Listener #2 AND subject: turns cold temperature readings into FrostWarning events (observer chain). */
public final class FrostAlertListener extends BasicSubject<Event> implements Observer<Event> {
    private final double frostBelowCelsius;

    public FrostAlertListener(double frostBelowCelsius) {
        this.frostBelowCelsius = frostBelowCelsius;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof TemperatureReading reading && reading.celsius() < frostBelowCelsius) {
            publish(new FrostWarning(reading.fieldId(), reading.celsius()));
        }
    }
}
