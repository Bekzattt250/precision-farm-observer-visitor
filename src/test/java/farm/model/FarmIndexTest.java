package farm.model;

import farm.SampleFarm;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FarmIndexTest {

    @Test
    void findsNodesOnEveryLevel() {
        FarmIndex index = new FarmIndex(SampleFarm.build());

        assertEquals(SampleFarm.NORTH_FIELD, index.find(SampleFarm.NORTH_FIELD).orElseThrow().id());
        assertEquals(SampleFarm.SOUTH_PLOT_2, index.find(SampleFarm.SOUTH_PLOT_2).orElseThrow().id());
        assertEquals("pump-n2", index.find("pump-n2").orElseThrow().id());
    }

    @Test
    void unknownIdIsEmpty() {
        assertTrue(new FarmIndex(SampleFarm.build()).find("nope").isEmpty());
    }

    @Test
    void duplicateIdsAreRejected() {
        Valve first = new Valve("same", false, 1.0);
        Valve second = new Valve("same", false, 1.0);
        Plot plot = new Plot("p", List.<FarmElement>of(first, second));

        assertThrows(IllegalArgumentException.class, () -> new FarmIndex(plot));
    }
}
