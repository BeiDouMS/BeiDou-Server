package soloMapling.server;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

// Fable audit Phase 0: lightweight bot-system performance counters.
// Hot paths bump LongAdders (cheap, contention-free); the !env perf command turns
// them into per-second rates measured since the previous readout. Counters only -
// nothing here changes bot behavior.
public final class BotPerfStats {

    private BotPerfStats() {
    }

    // bumped from hot paths
    public static final LongAdder MACRO_TICKS = new LongAdder();      // BotSM updateState runs
    public static final LongAdder MOVEMENT_TICKS = new LongAdder();   // GCMovementDriver ticks
    public static final LongAdder IDLE_BROADCASTS = new LongAdder();  // idle-refresh packets built + broadcast
    public static final LongAdder NUDGES = new LongAdder();           // nudgeSoon accepted (past debounce)

    // tick wheel health (Fable Phase 2), written by BotTickService.drive
    public static final LongAdder WHEEL_DISPATCHES = new LongAdder();
    public static final LongAdder WHEEL_LAG_MS = new LongAdder();
    private static volatile long wheelLagMaxMs = 0;

    public static void recordWheelDispatch(long lagMs) {
        WHEEL_DISPATCHES.increment();
        WHEEL_LAG_MS.add(Math.max(0, lagMs));
        if (lagMs > wheelLagMaxMs) {
            wheelLagMaxMs = lagMs;
        }
    }

    // shared combat ticker health, written once per sweep by TrainingBot.combatTickAll
    private static volatile long combatLastMs = 0;
    private static volatile long combatMaxMs = 0;
    private static volatile int combatBots = 0;

    public static void recordCombatSweep(long elapsedMs, int botCount) {
        combatLastMs = elapsedMs;
        combatBots = botCount;
        if (elapsedMs > combatMaxMs) {
            combatMaxMs = elapsedMs;
        }
    }

    // previous readout, for delta rates (guarded by the synchronized report())
    private static long prevAtMs = System.currentTimeMillis();
    private static long prevMacro = 0;
    private static long prevMove = 0;
    private static long prevIdle = 0;
    private static long prevNudge = 0;
    private static long prevWheelDisp = 0;
    private static long prevWheelLag = 0;

    // Core report lines: threads, heap, and per-second rates since the previous call.
    // The first call after boot averages over the whole uptime - call twice a few
    // seconds apart for a live rate. Bot/tier counts are appended by the caller
    // (!env perf) so this class stays free of bot-package imports.
    public static synchronized List<String> report() {
        long now = System.currentTimeMillis();
        double secs = Math.max(0.001, (now - prevAtMs) / 1000.0);
        long macro = MACRO_TICKS.sum();
        long move = MOVEMENT_TICKS.sum();
        long idle = IDLE_BROADCASTS.sum();
        long nudge = NUDGES.sum();

        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long maxMb = rt.maxMemory() / (1024 * 1024);

        List<String> out = new ArrayList<>();
        out.add(String.format("=== Bot perf (window: %.0fs) ===", secs));
        out.add(String.format("threads: %d platform   heap: %d / %d MB",
                ManagementFactory.getThreadMXBean().getThreadCount(), usedMb, maxMb));
        out.add(String.format("macro ticks: %.1f/s   movement ticks: %.1f/s",
                (macro - prevMacro) / secs, (move - prevMove) / secs));
        out.add(String.format("idle broadcasts: %.1f/s   nudges: %.2f/s",
                (idle - prevIdle) / secs, (nudge - prevNudge) / secs));
        out.add(String.format("combat sweep: last %dms, max %dms, %d grinder(s)",
                combatLastMs, combatMaxMs, combatBots));
        long disp = WHEEL_DISPATCHES.sum();
        long lagTotal = WHEEL_LAG_MS.sum();
        long dispDelta = disp - prevWheelDisp;
        long lagDelta = lagTotal - prevWheelLag;
        out.add(String.format("tick wheel: %d bot(s), %.1f dispatch/s, lag avg %dms max %dms, throttle x%.2f",
                BotTickService.size(), dispDelta / secs,
                dispDelta > 0 ? lagDelta / dispDelta : 0, wheelLagMaxMs, BotTickService.throttleFactor()));

        prevAtMs = now;
        prevMacro = macro;
        prevMove = move;
        prevIdle = idle;
        prevNudge = nudge;
        prevWheelDisp = disp;
        prevWheelLag = lagTotal;
        return out;
    }
}
