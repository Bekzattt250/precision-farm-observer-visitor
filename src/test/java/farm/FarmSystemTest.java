package farm;

import farm.event.EventType;
import farm.event.InspectionReport;
import farm.sim.SimulationConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Whole system, 4 producer tasks x 1000 events, with counts worked out by hand (see Simulation). */
class FarmSystemTest {

    @Test
    void everyEventOfTheWholeChainIsDeliveredExactlyOnce() throws InterruptedException {
        FarmSystem system = new FarmSystem(new SimulationConfig(4, 1_000));

        system.run();

        // 2 soil producers x 1000 and 2 weather producers x 1000
        assertEquals(2_000, system.statistics().count(EventType.SOIL_MOISTURE));
        assertEquals(2_000, system.statistics().count(EventType.TEMPERATURE));
        // frost: 4 of every 10 readings are below 0 C -> 400 per weather producer
        assertEquals(800, system.statistics().count(EventType.FROST_WARNING));
        // one summary per 10 soil readings of a field -> 100 per field
        assertEquals(200, system.statistics().count(EventType.FIELD_SUMMARY));
        // every frost warning is inspected; only the north field has faulty elements
        assertEquals(800, system.statistics().count(EventType.INSPECTION_REPORT));
        assertEquals(400, system.statistics().count(EventType.MAINTENANCE_TICKET));
        assertEquals(6_200, system.statistics().total());
        // dry soil: 10 of every 40 readings -> 250 per soil producer
        assertEquals(500, system.irrigation().totalCommands());
        // the farmer app got the alerts, summaries, reports and tickets (not raw readings)
        assertEquals(800 + 200 + 800 + 400, system.farmerApp().received().size());
    }

    @Test
    void resultDoesNotDependOnThePoolSize() throws InterruptedException {
        FarmSystem system = new FarmSystem(new SimulationConfig(2, 500));

        system.run();

        assertEquals(1_000, system.statistics().count(EventType.SOIL_MOISTURE));
        assertEquals(400, system.statistics().count(EventType.FROST_WARNING));
        assertEquals(200, system.statistics().count(EventType.MAINTENANCE_TICKET));
        assertTrue(system.farmerApp().receivedOf(EventType.INSPECTION_REPORT).stream()
                .allMatch(event -> event instanceof InspectionReport));
    }

    @Test
    void visitorResultsOfTheSampleFarm() {
        FarmSystem system = new FarmSystem(new SimulationConfig(1, 1));

        assertEquals(4_400.0, system.dailyWaterUsageLiters(), 1e-9);
        assertEquals(2_000.0, system.totalCost(), 1e-9);
        assertTrue(system.farmAsJson().startsWith("{\n  \"type\": \"farm\""));
    }
}
