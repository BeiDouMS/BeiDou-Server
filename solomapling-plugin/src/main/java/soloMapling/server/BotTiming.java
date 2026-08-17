package soloMapling.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

// Fable Phase 4: the one place for "wait a beat before doing X" - realistic bot
// timing without ever holding an OS thread or freezing a tick mid-sleep.
//
// THREE TOOLS - PICK BY SITUATION:
//
// 1. Pause my whole FSM until the next beat  ->  BotSM.waitFor(ms) (not here)
//    Use when the delay IS the bot's next action gap ("scroll, then nothing for
//    10-15s"). Call waitFor(...), set the next state, return from the tick. The
//    tick wheel skips the bot until the wait passes. The bot is intentionally
//    inert for the whole wait.
//        waitForRandom(10000, 15000); setMyState(NEXT); return;
//
// 2. Do ONE thing shortly, without pausing anything  ->  BotTiming.after(...)
//    Use for a delayed side-effect while the bot keeps ticking normally ("answer
//    the player ~1s after they spoke", "clear the chalkboard in 5 min"). The
//    timer rides the shared scheduled pool; the action body runs on a virtual
//    thread, so a slow action can't clog the timer pool.
//        BotTiming.afterRandom(800, 1500, () -> BotSpeak(bot, line));
//
// 3. A scripted sequence of beats  ->  BotTiming.chain()
//    Use for choreography: say a line, beat, emote, beat, act. The whole chain
//    runs in order on ONE virtual thread (pauses park that virtual thread only -
//    costs a few hundred bytes, no OS thread). This replaces the old pattern of
//    several Thread.sleep calls inside a tick.
//        BotTiming.chain()
//                .run(() -> BotSpeak(bot, "Check this out"))
//                .pauseRandom(600, 1100)
//                .run(() -> BotEmote(bot, 2))
//                .pause(1500)
//                .run(() -> doTheThing(bot))
//                .start();
//    Chains are fire-and-forget: the calling tick returns immediately and later
//    steps run even if the bot's situation changed. For a chain that should die
//    when circumstances change, gate it: .stopUnless(() -> bot.isAlive()) checks
//    before EVERY following step and silently ends the chain when false.
//
// RULE OF THUMB: pacing the bot's own next move = waitFor; everything else that
// used to be Thread.sleep inside bot code = after(...) or chain(). Never call
// Thread.sleep / blockingSleep in FSM or ticker code.
public final class BotTiming {

    private BotTiming() {
    }

    // One delayed action. Timer on the shared scheduled pool, body on a virtual thread.
    public static void after(long delayMs, Runnable action) {
        ExecutorServiceManager.getScheduledExecutorService().schedule(
                () -> ExecutorServiceManager.runAsync(() -> safely(action)),
                Math.max(0, delayMs), TimeUnit.MILLISECONDS);
    }

    // Same, with a uniformly random delay in [loMs, hiMs] - jitter reads as human.
    public static void afterRandom(long loMs, long hiMs, Runnable action) {
        after(randomBetween(loMs, hiMs), action);
    }

    public static Chain chain() {
        return new Chain();
    }

    // Ordered steps executed on one virtual thread; pauses park that thread only.
    public static final class Chain {
        private final List<Runnable> steps = new ArrayList<>();
        private BooleanSupplier keepGoing = null;

        private Chain() {
        }

        public Chain run(Runnable action) {
            steps.add(action);
            return this;
        }

        public Chain pause(long ms) {
            long delay = Math.max(0, ms);
            steps.add(() -> parkFor(delay));
            return this;
        }

        public Chain pauseRandom(long loMs, long hiMs) {
            steps.add(() -> parkFor(randomBetween(loMs, hiMs)));
            return this;
        }

        // Checked before every step that FOLLOWS this call; when it returns false the
        // rest of the chain is silently dropped. Set it first to guard the whole chain.
        public Chain stopUnless(BooleanSupplier condition) {
            this.keepGoing = condition;
            return this;
        }

        // Launches the chain on a virtual thread and returns immediately.
        public void start() {
            List<Runnable> script = new ArrayList<>(steps);
            BooleanSupplier gate = keepGoing;
            ExecutorServiceManager.runAsync(() -> {
                for (Runnable step : script) {
                    if (gate != null && !gate.getAsBoolean()) {
                        return;
                    }
                    if (!safely(step)) {
                        return; // a thrown step ends the chain; later steps often depend on it
                    }
                }
            });
        }
    }

    private static void parkFor(long ms) {
        try {
            Thread.sleep(ms); // parks the chain's virtual thread only - no OS thread held
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e); // ends the chain via safely()
        }
    }

    private static long randomBetween(long loMs, long hiMs) {
        long lo = Math.max(0, Math.min(loMs, hiMs));
        long hi = Math.max(loMs, hiMs);
        return lo + (hi > lo ? ThreadLocalRandom.current().nextLong(hi - lo + 1) : 0);
    }

    private static boolean safely(Runnable r) {
        try {
            r.run();
            return true;
        } catch (Throwable t) {
            System.out.println("[BotTiming] step failed: " + t);
            return false;
        }
    }
}
