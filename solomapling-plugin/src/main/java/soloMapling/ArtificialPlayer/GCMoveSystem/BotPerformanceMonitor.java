package soloMapling.ArtificialPlayer.GCMoveSystem;

/*
 * No-op stub of GreenCat's instrumentation hook. The donor recorded per-phase timings here;
 * the extracted movement engine keeps the call sites but discards the data.
 */
// Ported from GreenCatMS. Credit: NutNNut.
final class BotPerformanceMonitor {
    private BotPerformanceMonitor() {
    }

    static boolean enabled() {
        return false;
    }

    static void record(String name, long nanos) {
        // no-op
    }

    static void recordPathfind(String caller, long nanos) {
        // no-op
    }
}
