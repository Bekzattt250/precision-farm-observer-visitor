package farm.source;

import farm.core.BasicSubject;
import farm.event.Event;
import farm.event.TemperatureReading;

/** Subject #2: weather stations. */
public final class WeatherStationSource extends BasicSubject<Event> {
    public void emit(String fieldId, double celsius) {
        publish(new TemperatureReading(fieldId, celsius));
    }
}
