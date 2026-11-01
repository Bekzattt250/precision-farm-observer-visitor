package farm.sim;

public record SimulationConfig(int producerThreads, int eventsPerProducer) {
    public SimulationConfig {
        if (producerThreads < 1 || eventsPerProducer < 1) {
            throw new IllegalArgumentException("producerThreads and eventsPerProducer must be >= 1");
        }
    }
}
