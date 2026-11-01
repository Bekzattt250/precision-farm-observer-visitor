package farm.visitor;

import farm.SampleFarm;
import farm.model.Crop;
import farm.model.Farm;
import farm.model.FarmElement;
import farm.model.FarmIndex;
import farm.model.Field;
import farm.model.Plot;
import farm.model.Pump;
import farm.model.Sensor;
import farm.model.Valve;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisitorTest {
    private static final double DELTA = 1e-9;

    private final Farm farm = SampleFarm.build();
    private final FarmIndex index = new FarmIndex(farm);

    private FarmElement node(String id) {
        return index.find(id).orElseThrow();
    }

    // ---- double dispatch -------------------------------------------------------------------

    @Test
    void doubleDispatchCallsTheVisitMethodOfTheRealElementType() {
        Visitor<String> namer = new Visitor<>() {
            public String visitFarm(Farm f) { return "farm"; }
            public String visitField(Field f) { return "field"; }
            public String visitPlot(Plot p) { return "plot"; }
            public String visitCrop(Crop c) { return "crop"; }
            public String visitPump(Pump p) { return "pump"; }
            public String visitSensor(Sensor s) { return "sensor"; }
            public String visitValve(Valve v) { return "valve"; }
        };

        FarmElement asInterface = node("pump-n1");   // static type is FarmElement, not Pump

        assertEquals("pump", asInterface.accept(namer));
        assertEquals("plot", node(SampleFarm.NORTH_PLOT_1).accept(namer));
        assertEquals("farm", farm.accept(namer));
    }

    // ---- numeric: water use (litres/day, broken pumps count 0) -----------------------------
    // n1: 500*4 = 2000 | n2: broken = 0 | s1: 400*3 = 1200 | s2: 200*6 = 1200

    @Test
    void waterUsageOfWholeFarm() {
        assertEquals(4400.0, farm.accept(new WaterUsageVisitor()), DELTA);
    }

    @Test
    void waterUsageOfSubtrees() {
        assertEquals(2000.0, node(SampleFarm.NORTH_FIELD).accept(new WaterUsageVisitor()), DELTA);
        assertEquals(2400.0, node(SampleFarm.SOUTH_FIELD).accept(new WaterUsageVisitor()), DELTA);
        assertEquals(0.0, node(SampleFarm.NORTH_PLOT_2).accept(new WaterUsageVisitor()), DELTA);
    }

    // ---- accumulating state: cost ----------------------------------------------------------
    // crops 10*50 + 5*80 + 8*40 + 2*100 = 1420 | pumps 4*100 = 400 | sensors 4*30 = 120 | valves 3*20 = 60

    @Test
    void costOfWholeFarmAndPerType() {
        CostVisitor cost = new CostVisitor();

        double total = farm.accept(cost);

        assertEquals(2000.0, total, DELTA);
        assertEquals(Map.of("Crop", 1420.0, "Pump", 400.0, "Sensor", 120.0, "Valve", 60.0), cost.costByType());
    }

    @Test
    void costOfNorthFieldOnly() {
        // crops 500+400 | pumps 200 | sensors 60 | valves 40
        assertEquals(1200.0, node(SampleFarm.NORTH_FIELD).accept(new CostVisitor()), DELTA);
    }

    // ---- filter ----------------------------------------------------------------------------

    @Test
    void needsAttentionFindsBrokenPumpLowBatterySensorAndStuckValve() {
        List<FarmElement> faulty = farm.accept(new NeedsAttentionVisitor(20));

        assertEquals(List.of("pump-n2", "sensor-n2", "valve-n2"), faulty.stream().map(FarmElement::id).toList());
    }

    @Test
    void needsAttentionThresholdIsInjected() {
        // with a 60 % limit sensor-s2 (55) also becomes a problem; sensor-n1 (80) and sensor-s1 (90) stay fine
        List<String> ids = farm.accept(new NeedsAttentionVisitor(60)).stream().map(FarmElement::id).toList();

        assertTrue(ids.contains("sensor-s2"));
        assertEquals(4, ids.size());
    }

    @Test
    void needsAttentionIsEmptyForHealthySubtree() {
        assertTrue(node(SampleFarm.SOUTH_FIELD).accept(new NeedsAttentionVisitor(20)).isEmpty());
    }

    // ---- JSON export -----------------------------------------------------------------------

    @Test
    void jsonHasCorrectNestingAndIndentation() {
        Plot plot = new Plot("p1", List.<FarmElement>of(new Valve("v1", false, 20.0)));
        Field field = new Field("f1", List.of(plot));

        String expected = """
                {
                  "type": "field",
                  "id": "f1",
                  "children": [
                    {
                      "type": "plot",
                      "id": "p1",
                      "children": [
                        {
                          "type": "valve",
                          "id": "v1",
                          "stuck": false,
                          "unitCost": 20.0
                        }
                      ]
                    }
                  ]
                }""";

        assertEquals(expected, field.accept(new JsonExportVisitor()));
    }

    @Test
    void jsonSeparatesSiblingsWithCommasAndShowsEmptyContainers() {
        Plot plot = new Plot("p1", List.<FarmElement>of(
                new Valve("v1", false, 1.0), new Valve("v2", true, 2.0)));
        Plot empty = new Plot("p2", List.of());

        String json = new Field("f1", List.of(plot, empty)).accept(new JsonExportVisitor());

        assertTrue(json.contains("\n        },\n        {\n"));
        assertTrue(json.contains("\"children\": []"));
    }

    @Test
    void jsonEscapesQuotesAndBackslashes() {
        Crop crop = new Crop("c1", "Wheat \"red\" \\ winter", 1.0, 1.0, 1.0);

        String json = crop.accept(new JsonExportVisitor());

        assertTrue(json.contains("\"name\": \"Wheat \\\"red\\\" \\\\ winter\""));
    }

    @Test
    void jsonOfWholeFarmMentionsEveryElementOnce() {
        String json = farm.accept(new JsonExportVisitor());

        assertEquals(1, occurrences(json, "\"type\": \"farm\""));
        assertEquals(2, occurrences(json, "\"type\": \"field\""));
        assertEquals(4, occurrences(json, "\"type\": \"plot\""));
        assertEquals(4, occurrences(json, "\"type\": \"crop\""));
        assertEquals(4, occurrences(json, "\"type\": \"pump\""));
        assertEquals(4, occurrences(json, "\"type\": \"sensor\""));
        assertEquals(3, occurrences(json, "\"type\": \"valve\""));
    }

    private static int occurrences(String text, String part) {
        return text.split(java.util.regex.Pattern.quote(part), -1).length - 1;
    }
}
