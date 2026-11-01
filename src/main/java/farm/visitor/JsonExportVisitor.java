package farm.visitor;

import farm.model.CompositeElement;
import farm.model.Crop;
import farm.model.Farm;
import farm.model.Field;
import farm.model.Plot;
import farm.model.Pump;
import farm.model.Sensor;
import farm.model.Valve;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Hand-written JSON export (no libraries). Each node returns its own JSON block; parents indent and nest it. */
public final class JsonExportVisitor implements Visitor<String> {

    @Override
    public String visitFarm(Farm farm) {
        return container("farm", farm);
    }

    @Override
    public String visitField(Field field) {
        return container("field", field);
    }

    @Override
    public String visitPlot(Plot plot) {
        return container("plot", plot);
    }

    @Override
    public String visitCrop(Crop crop) {
        return leaf("crop", crop.id(), attributes(
                "name", crop.name(),
                "areaHectares", crop.areaHectares(),
                "seedCostPerHectare", crop.seedCostPerHectare(),
                "expectedYieldTonsPerHectare", crop.expectedYieldTonsPerHectare()));
    }

    @Override
    public String visitPump(Pump pump) {
        return leaf("pump", pump.id(), attributes(
                "flowLitersPerHour", pump.flowLitersPerHour(),
                "hoursPerDay", pump.hoursPerDay(),
                "maintenanceCost", pump.maintenanceCost(),
                "operational", pump.operational()));
    }

    @Override
    public String visitSensor(Sensor sensor) {
        return leaf("sensor", sensor.id(), attributes(
                "kind", sensor.kind().name(),
                "batteryPercent", sensor.batteryPercent(),
                "unitCost", sensor.unitCost()));
    }

    @Override
    public String visitValve(Valve valve) {
        return leaf("valve", valve.id(), attributes(
                "stuck", valve.stuck(),
                "unitCost", valve.unitCost()));
    }

    private String container(String type, CompositeElement<?> node) {
        String children = node.children().stream()
                .map(child -> indent(child.accept(this)))
                .collect(Collectors.joining(",\n"));
        String header = "{\n  \"type\": " + quote(type) + ",\n  \"id\": " + quote(node.id()) + ",\n";
        if (children.isEmpty()) {
            return header + "  \"children\": []\n}";
        }
        return header + "  \"children\": [\n" + children + "\n  ]\n}";
    }

    private static String leaf(String type, String id, Map<String, Object> attributes) {
        List<String> lines = new ArrayList<>();
        lines.add("  \"type\": " + quote(type));
        lines.add("  \"id\": " + quote(id));
        attributes.forEach((key, value) -> lines.add("  " + quote(key) + ": " + render(value)));
        return "{\n" + String.join(",\n", lines) + "\n}";
    }

    private static Map<String, Object> attributes(Object... keysAndValues) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            result.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return result;
    }

    private static String render(Object value) {
        return value instanceof String text ? quote(text) : String.valueOf(value);
    }

    private static String indent(String block) {
        return block.lines().map(line -> "    " + line).collect(Collectors.joining("\n"));
    }

    private static String quote(String text) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }
}
