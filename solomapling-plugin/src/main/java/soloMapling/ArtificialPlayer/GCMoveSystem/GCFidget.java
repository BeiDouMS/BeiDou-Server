package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;

import java.awt.Point;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/*
 * Idle auto-fidget: gives an otherwise-stationary bot some life. While the bot is idle (no
 * move / follow / travel), this coarse poller periodically does one small random action —
 * turn around, duck, hop in place, or a small wander — anchored to its rest spot, returning home
 * if it drifts. It stands down the instant a real command takes over (so it never fights movement).
 */
// Ported from GreenCatMS. Credit: NutNNut.
final class GCFidget {
    private GCFidget() {
    }

    private static final long POLL_MS = 1500;
    private static final long MIN_REST_MS = 5_000;   // gap between fidget actions
    private static final long MAX_REST_MS = 14_000;
    private static final int WANDER_PX = 45;         // small nudge radius around the anchor
    private static final int RETURN_DIST = 70;       // drifted past this -> walk back to the anchor
    private static final int DUCK_MIN_MS = 900;
    private static final int DUCK_MAX_MS = 1900;

    private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "gcfidget-poll");
        t.setDaemon(true);
        return t;
    });

    private static final Map<Integer, Session> SESSIONS = new ConcurrentHashMap<>();

    private static final class Session {
        final Character bot;
        ScheduledFuture<?> task;
        Point base;                 // rest anchor (null = re-anchor at the next idle position)
        long nextActionAtMs;
        boolean fidgetMoveActive;   // our own nudge/return move is in flight (vs a user command)

        Session(Character bot) {
            this.bot = bot;
        }
    }

    static void start(Character bot) {
        if (bot == null) {
            return;
        }
        cancel(bot);
        Session s = new Session(bot);
        s.task = POOL.scheduleAtFixedRate(
                () -> {
                    try {
                        tick(s);
                    } catch (Throwable ignored) {
                        // a thrown poll would cancel the periodic task
                    }
                }, POLL_MS, POLL_MS, TimeUnit.MILLISECONDS);
        SESSIONS.put(bot.getId(), s);
    }

    static void cancel(Character bot) {
        if (bot == null) {
            return;
        }
        Session s = SESSIONS.remove(bot.getId());
        if (s != null && s.task != null) {
            s.task.cancel(false);
        }
    }

    static boolean isActive(Character bot) {
        return bot != null && SESSIONS.containsKey(bot.getId());
    }

    private static void tick(Session s) {
        Character bot = s.bot;
        if (bot == null || bot.getMap() == null || !GCMovement.isEnabled(bot)) {
            cancel(bot);
            return;
        }

        boolean busy = GCMovement.isMoving(bot) || GCMovement.isTraveling(bot) || GCMovement.isFollowing(bot);
        if (busy && !s.fidgetMoveActive) {
            // A real command is driving the bot — stand down and re-anchor wherever it ends up.
            s.base = null;
            return;
        }
        if (busy) {
            return; // our own nudge/return is still in flight
        }
        s.fidgetMoveActive = false;

        Point pos = bot.getPosition();
        if (s.base == null) {
            s.base = new Point(pos);
        }

        long now = System.currentTimeMillis();
        if (now < s.nextActionAtMs) {
            return;
        }
        s.nextActionAtMs = now + ThreadLocalRandom.current().nextLong(MIN_REST_MS, MAX_REST_MS);

        // Drifted from the anchor (knocked away / wandered) -> walk back home.
        if (Math.abs(pos.x - s.base.x) + Math.abs(pos.y - s.base.y) > RETURN_DIST) {
            GCMovement.nudgeTo(bot, s.base.x, s.base.y);
            s.fidgetMoveActive = true;
            return;
        }

        switch (ThreadLocalRandom.current().nextInt(4)) {
            case 0 -> GCMovement.turnAround(bot);
            case 1 -> GCMovement.duck(bot, ThreadLocalRandom.current().nextInt(DUCK_MIN_MS, DUCK_MAX_MS));
            case 2 -> GCMovement.jumpInPlace(bot);
            default -> {
                int dx = ThreadLocalRandom.current().nextInt(-WANDER_PX, WANDER_PX + 1);
                GCMovement.nudgeTo(bot, s.base.x + dx, s.base.y); // small wander near the anchor
                s.fidgetMoveActive = true;
            }
        }
    }
}
