package soloMapling.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceManager {
    //    private static final ExecutorService executorService = Executors.newCachedThreadPool(); // is this causing error 38 / dc to login?
    // Fable Phase 2 (F12): 100 -> 8. Its only consumers are chat-dispatch matching and
    // GM command bodies - a bounded handful of quick tasks. Blocking bulk work
    // (spawn waves, choreography) already runs on the virtual-thread executor.
    private static final ExecutorService executorService = Executors.newFixedThreadPool(8);
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
    private static final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();


    // Private constructor to prevent instantiation

    private ExecutorServiceManager() {
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public static ExecutorService getVirtualThreadExecutorService() {
        return virtualThreadExecutor;
    }

    /**
     * Use this for multithreaded processing. Best suited for lightweight, concurrent tasks.
     * Example
     * runAsync(() -> methodName(parameter));
     */
    public static void runAsync(Runnable task) {
        virtualThreadExecutor.submit(task);
    }

    public static void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public static void shutdown() {
        shutdownExecutor(executorService);
        shutdownExecutor(scheduledExecutorService);
    }

    private static void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
