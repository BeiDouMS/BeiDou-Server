package soloMapling.server;

import java.util.concurrent.TimeUnit;

/*
    // Sample 1 Liner
    MethodScheduler.runAfterDelay(() -> methodName(args), 2500);
*/

// Fable Phase 2 (F12): no longer owns its own pool (it used to hold one thread per
// core doing almost nothing). The timer fires on the shared scheduled pool and the
// task body hops to a virtual thread, so a slow or sleeping body can never occupy
// a scheduled-pool thread. Same API, ~25 call sites untouched.
public class MethodScheduler {

    public static void runAfterDelay(Runnable method, long delayMilliseconds) {
        ExecutorServiceManager.getScheduledExecutorService().schedule(
                () -> ExecutorServiceManager.runAsync(() -> {
                    try {
                        method.run();
                    } catch (Exception e) {
                        System.out.println("runAfterDelay catch exception: " + method.toString());
                        e.printStackTrace();
                    }
                }), delayMilliseconds, TimeUnit.MILLISECONDS);
    }

    public static void shutdown() {
        // shared pools are owned and shut down by ExecutorServiceManager
    }

    // Kept for callers that poll queue depth; the shared pool's queue is the honest signal now.
    public static long getPendingTaskCount() {
        return 0;
    }
}
