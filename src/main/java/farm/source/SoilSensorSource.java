package farm.source;

import farm.core.BasicSubject;
import farm.event.Event;
import farm.event.SoilMoistureReading;

/** Subject #1: soil moisture sensors. */
public final class SoilSensorSource extends BasicSubject<Event> {
    public void emit(String fieldId, String plotId, double moisturePercent) {
        publish(new SoilMoistureReading(fieldId, plotId, moisturePercent));
    }
}
