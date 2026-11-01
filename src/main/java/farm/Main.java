package farm;

import farm.event.EventType;
import farm.sim.SimulationConfig;

/** Usage: Main [producerThreads] [eventsPerProducer]   (defaults: 4 1000) */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws InterruptedException {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        int events = args.length > 1 ? Integer.parseInt(args[1]) : 1000;

        FarmSystem system = new FarmSystem(new SimulationConfig(threads, events));
        long started = System.nanoTime();
        system.run();
        long millis = (System.nanoTime() - started) / 1_000_000;

        System.out.println("Run finished in " + millis + " ms with " + threads + " pool threads");
        for (EventType type : EventType.values()) {
            System.out.println("  " + type + ": " + system.statistics().count(type));
        }
        System.out.println("Total events: " + system.statistics().total());
        System.out.println("Irrigation commands: " + system.irrigation().totalCommands());
        System.out.println("Water use (L/day): " + system.dailyWaterUsageLiters());
        System.out.println("Total cost: " + system.totalCost());
        System.out.println(system.farmAsJson());
    }
}
