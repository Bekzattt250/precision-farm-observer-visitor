package farm.sim;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProducerPoolTest {

    @Test
    void shutdownWaitsUntilAllSubmittedTasksAreDone() throws InterruptedException {
        ProducerPool pool = new ProducerPool(2);
        AtomicInteger done = new AtomicInteger();
        for (int i = 0; i < 5; i++) {
            pool.submit(() -> {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                done.incrementAndGet();
            });
        }

        pool.shutdownAndAwait(Duration.ofSeconds(5));

        assertEquals(5, done.get());
    }

    @Test
    void producerFailureIsNotHidden() {
        ProducerPool pool = new ProducerPool(1);
        pool.submit(() -> {
            throw new IllegalStateException("boom");
        });

        assertThrows(IllegalStateException.class, () -> pool.shutdownAndAwait(Duration.ofSeconds(5)));
    }

    @Test
    void poolNeedsAtLeastOneThread() {
        assertThrows(IllegalArgumentException.class, () -> new ProducerPool(0));
    }

    @Test
    void configRejectsNonPositiveValues() {
        assertThrows(IllegalArgumentException.class, () -> new SimulationConfig(0, 10));
        assertThrows(IllegalArgumentException.class, () -> new SimulationConfig(4, 0));
    }
}
