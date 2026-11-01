package farm.sim;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** ExecutorService wrapper with a configurable thread count and a graceful shutdown. */
public final class ProducerPool {
    private final ExecutorService executor;
    private final List<Future<?>> futures = new CopyOnWriteArrayList<>();

    public ProducerPool(int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be >= 1");
        }
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public void submit(Runnable producer) {
        futures.add(executor.submit(producer));
    }

    /**
     * Stops accepting tasks, waits until every submitted producer is finished (so no event is lost),
     * and rethrows a producer failure instead of hiding it.
     */
    public void shutdownAndAwait(Duration timeout) throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            executor.shutdownNow();
            throw new IllegalStateException("Producers did not finish within " + timeout);
        }
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new IllegalStateException("A producer failed", e.getCause());
            }
        }
    }
}
