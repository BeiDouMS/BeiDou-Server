package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/*
 * Cross-map follow: keep a bot tailing a target across portals.
 *
 * Same-map following is driven by GCMovementDriver (its resolveTarget returns the
 * target's live position). This coarse ~400 ms poller adds the cross-map half: when the target is on
 * a different map (they stepped through a portal), it travels the bot to that map via GCTravel
 * and resumes same-map follow on arrival — chasing further if the target keeps moving. The bot routes
 * to the target's map, so for an adjacent map that's the same portal the target took.
 */
// Ported from GreenCatMS. Credit: NutNNut.
final class GCFollow {
    private GCFollow() {
    }

    private static final long POLL_MS = 400;

    private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "gcfollow-poll");
        t.setDaemon(true);
        return t;
    });

    private static final Map<Integer, Session> SESSIONS = new ConcurrentHashMap<>();

    private static final class Session {
        final Character bot;
        final Character target;
        ScheduledFuture<?> task;
        int travelDest = -1; // map the current follow-travel is heading to (-1 = not traveling)

        Session(Character bot, Character target) {
            this.bot = bot;
            this.target = target;
        }
    }

    static void start(Character bot, Character target) {
        if (bot == null || target == null) {
            return;
        }
        cancel(bot);
        GCMovement.enable(bot);
        Session s = new Session(bot, target);
        s.task = POOL.scheduleAtFixedRate(
                () -> {
                    try {
                        tick(s);
                    } catch (Throwable ignored) {
                        // a thrown poll would cancel the periodic task
                    }
                }, 0, POLL_MS, TimeUnit.MILLISECONDS);
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

    static boolean isFollowing(Character bot) {
        return bot != null && SESSIONS.containsKey(bot.getId());
    }

    private static void tick(Session s) {
        Character bot = s.bot;
        Character target = s.target;
        if (bot == null || bot.getMap() == null || target == null || target.getMap() == null) {
            GCMovement.endFollowState(bot); // target gone / offline — end follow
            cancel(bot);
            return;
        }

        if (bot.getMapId() == target.getMapId()) {
            // Same map: cancel any in-flight travel and let the 50 ms driver tail the target.
            if (GCMovement.isTraveling(bot)) {
                GCTravel.cancel(bot);
            }
            s.travelDest = -1;
            GCMovement.armSameMapFollow(bot, target);
            return;
        }

        // Different map: pause same-map follow and travel to the target's map.
        GCMovement.pauseFollowForTravel(bot);
        int destMap = target.getMapId();
        if (!GCMovement.isTraveling(bot)) {
            s.travelDest = destMap;
            GCTravel.travel(bot, destMap, null);
        } else if (s.travelDest != destMap) {
            // Target moved to a different map mid-travel — redirect the chase.
            GCTravel.cancel(bot);
            s.travelDest = destMap;
            GCTravel.travel(bot, destMap, null);
        }
    }
}
