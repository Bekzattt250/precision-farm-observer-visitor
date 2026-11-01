package farm.listener;

import farm.SampleFarm;
import farm.event.Event;
import farm.event.EventType;
import farm.event.FieldSummary;
import farm.event.FrostWarning;
import farm.event.InspectionReport;
import farm.event.MaintenanceTicket;
import farm.event.SoilMoistureReading;
import farm.event.TemperatureReading;
import farm.model.FarmIndex;
import farm.visitor.NeedsAttentionVisitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListenerTest {
    private static final double DELTA = 1e-9;

    @Test
    void irrigationStartsOnlyBelowThreshold() {
        IrrigationController irrigation = new IrrigationController(30.0);

        irrigation.onEvent(new SoilMoistureReading("f", "p1", 29.9));
        irrigation.onEvent(new SoilMoistureReading("f", "p1", 30.0));   // exactly at the limit: not dry
        irrigation.onEvent(new SoilMoistureReading("f", "p2", 10.0));
        irrigation.onEvent(new TemperatureReading("f", -5.0));          // other event types are ignored

        assertEquals(1, irrigation.commandsFor("p1"));
        assertEquals(1, irrigation.commandsFor("p2"));
        assertEquals(0, irrigation.commandsFor("unknown"));
        assertEquals(2, irrigation.totalCommands());
    }

    @Test
    void frostAlertRepublishesOnlyColdReadings() {
        FrostAlertListener frostAlert = new FrostAlertListener(0.0);
        List<Event> warnings = new ArrayList<>();
        frostAlert.subscribe(EventType.FROST_WARNING, warnings::add);

        frostAlert.onEvent(new TemperatureReading("f", 3.0));
        frostAlert.onEvent(new TemperatureReading("f", 0.0));
        frostAlert.onEvent(new TemperatureReading("f", -2.5));

        assertEquals(List.of(new FrostWarning("f", -2.5)), warnings);
    }

    @Test
    void aggregatorPublishesAverageAfterFullWindowOnly() {
        FieldAggregator aggregator = new FieldAggregator(3);
        List<Event> summaries = new ArrayList<>();
        aggregator.subscribe(EventType.FIELD_SUMMARY, summaries::add);

        aggregator.onEvent(new SoilMoistureReading("f", "p", 10.0));
        aggregator.onEvent(new SoilMoistureReading("f", "p", 20.0));
        assertTrue(summaries.isEmpty());

        aggregator.onEvent(new SoilMoistureReading("f", "p", 30.0));

        assertEquals(1, summaries.size());
        FieldSummary summary = (FieldSummary) summaries.get(0);
        assertEquals("f", summary.fieldId());
        assertEquals(3, summary.samples());
        assertEquals(20.0, summary.averageMoisture(), DELTA);
    }

    @Test
    void aggregatorKeepsSeparateWindowsPerField() {
        FieldAggregator aggregator = new FieldAggregator(2);
        List<Event> summaries = new ArrayList<>();
        aggregator.subscribe(EventType.FIELD_SUMMARY, summaries::add);

        aggregator.onEvent(new SoilMoistureReading("a", "p", 10.0));
        aggregator.onEvent(new SoilMoistureReading("b", "p", 50.0));
        assertTrue(summaries.isEmpty());

        aggregator.onEvent(new SoilMoistureReading("a", "p", 30.0));

        assertEquals(List.of(new FieldSummary("a", 2, 20.0)), summaries);
    }

    @Test
    void aggregatorRejectsWindowSmallerThanOne() {
        assertThrows(IllegalArgumentException.class, () -> new FieldAggregator(0));
    }

    @Test
    void statisticsCountsPerTypeAndAveragesMoisture() {
        StatisticsListener statistics = new StatisticsListener();

        statistics.onEvent(new SoilMoistureReading("f", "p", 10.0));
        statistics.onEvent(new SoilMoistureReading("f", "p", 20.0));
        statistics.onEvent(new SoilMoistureReading("f", "p", 30.0));
        statistics.onEvent(new TemperatureReading("f", 1.0));

        assertEquals(3, statistics.count(EventType.SOIL_MOISTURE));
        assertEquals(1, statistics.count(EventType.TEMPERATURE));
        assertEquals(0, statistics.count(EventType.FROST_WARNING));
        assertEquals(4, statistics.total());
        assertEquals(20.0, statistics.averageMoisture(), DELTA);
    }

    // ---- integration: event -> visitor on the related subtree -> new events ----------------

    @Test
    void frostInNorthFieldInspectsNorthSubtreeAndCreatesTicket() {
        InspectionService inspection = newInspectionService();
        List<Event> out = capture(inspection);

        inspection.onEvent(new FrostWarning(SampleFarm.NORTH_FIELD, -3.0));

        assertEquals(2, out.size());
        InspectionReport report = (InspectionReport) out.get(0);
        assertEquals(SampleFarm.NORTH_FIELD, report.subtreeId());
        assertEquals(List.of("Pump pump-n2", "Sensor sensor-n2", "Valve valve-n2"), report.issues());
        MaintenanceTicket ticket = (MaintenanceTicket) out.get(1);
        assertEquals(SampleFarm.NORTH_FIELD, ticket.subtreeId());
        assertEquals("Pump pump-n2; Sensor sensor-n2; Valve valve-n2", ticket.description());
    }

    @Test
    void frostInSouthFieldGivesCleanReportAndNoTicket() {
        InspectionService inspection = newInspectionService();
        List<Event> out = capture(inspection);

        inspection.onEvent(new FrostWarning(SampleFarm.SOUTH_FIELD, -1.0));

        assertEquals(1, out.size());
        assertTrue(((InspectionReport) out.get(0)).issues().isEmpty());
    }

    @Test
    void inspectionIgnoresOtherEventTypes() {
        InspectionService inspection = newInspectionService();
        List<Event> out = capture(inspection);

        inspection.onEvent(new TemperatureReading(SampleFarm.NORTH_FIELD, -10.0));

        assertTrue(out.isEmpty());
    }

    @Test
    void inspectionOfUnknownFieldFailsLoudly() {
        InspectionService inspection = newInspectionService();

        assertThrows(IllegalArgumentException.class, () -> inspection.onEvent(new FrostWarning("no-such-field", -1.0)));
    }

    @Test
    void farmerAppKeepsEventsByType() {
        FarmerApp app = new FarmerApp();

        app.onEvent(new FrostWarning("f", -1.0));
        app.onEvent(new MaintenanceTicket("f", "fix"));
        app.onEvent(new FrostWarning("f", -2.0));

        assertEquals(3, app.received().size());
        assertEquals(2, app.receivedOf(EventType.FROST_WARNING).size());
        assertEquals(1, app.receivedOf(EventType.MAINTENANCE_TICKET).size());
    }

    private static InspectionService newInspectionService() {
        return new InspectionService(new FarmIndex(SampleFarm.build()), new NeedsAttentionVisitor(20));
    }

    private static List<Event> capture(InspectionService inspection) {
        List<Event> out = new ArrayList<>();
        inspection.subscribe(EventType.INSPECTION_REPORT, out::add);
        inspection.subscribe(EventType.MAINTENANCE_TICKET, out::add);
        return out;
    }
}
