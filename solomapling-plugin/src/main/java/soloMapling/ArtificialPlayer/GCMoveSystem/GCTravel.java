package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Portal;
import soloMapling.ArtificialPlayer.BotTravelSystem.BotScriptedWarp;
import soloMapling.BotLogger;

import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/*
 * Cross-map travel executor (the "GCTravel" half). Layers a coarse hop-by-hop orchestrator on top
 * of GCMovement: route via GCWorldGraph, then for each hop walk to the portal that
 * leads to the next map (using GCMove) and step through it; warp (changeMap) any hop with
 * no walkable portal (link/scripted/special) — pure-movement scope. The route is recomputed from
 * the live current map each poll, so landing anywhere unexpected self-corrects.
 *
 * Runs on its own ~300 ms poller per traveling bot, independent of the 50 ms physics tick.
 */
// Ported from GreenCatMS. Credit: NutNNut.
final class GCTravel {
    private GCTravel() {
    }

    // Route-search depth cap. Kept >= TrainingBot's discovery radius so anything a bot can DISCOVER it
    // can also ROUTE to (route may use taxi/ferry shortcuts, so the actual hop count is usually lower).
    private static final int MAX_HOPS = 20;
    private static final int POLL_MS = 300;
    // "At the portal" box: portals often sit a little above the floor the bot stands on, so the
    // vertical tolerance is generous while X stays tight.
    private static final int ENTER_X = 35;
    private static final int ENTER_Y = 100;
    // Warp a hop ONLY when the bot is genuinely stuck — neither closing on the portal NOR moving from the
    // spot it stands in for this long. A bot still walking a huge map (even one whose straight-line distance
    // to the portal isn't dropping — climbing a rope to an elevated portal, detouring round terrain, getting
    // knocked around by mobs) keeps changing position, so it's never cut off; map size is fine.
    private static final long HOP_STUCK_MS = 12_000;
    private static final int HOP_PROGRESS_EPS_PX = 16;  // min portal-distance drop that counts as closing in
    private static final int HOP_MOVE_EPS_PX = 16;      // min position change that counts as "not in the same spot"
    // Stand on the portal this long (after the walk has finished) before stepping through, so the
    // server warp never out-runs the walk packets (which read as "vanished mid-stride" to clients).
    private static final long PORTAL_ENTER_DWELL_MS = 350;
    // "Basically on top of it" snap: a bot that keeps failing to settle while near the hop target
    // just acts. 100px so it reaches a bot bouncing at the sloped lip of a portal platform, "just
    // barely off" below/beside it (the settled box above already tolerates 100px of Y, so a
    // smaller snap circle left exactly those bots uncovered). The clock survives brief excursions
    // (a wide arc, a slide back down the slope) via the grace — only staying away longer resets
    // it. A clean walk-up still normally enters through the settled dwell first.
    private static final int SNAP_ENTER_RADIUS_PX = 100;
    private static final long SNAP_ENTER_MS = 1_200;
    private static final long SNAP_NEAR_GRACE_MS = 1_500;
    // Soft-lock treadmill window: all samples confined to one small bounding box for the whole
    // window means the bot is cycling in place (jump, fall, slide back, retry). A box, not an
    // anchored circle: slope physics slides a bot a couple hundred px per failed cycle, which
    // kept escaping (and thereby resetting) the old fixed-anchor circle so it never latched.
    // A genuinely traveling bot outgrows 400px within a few seconds of walking.
    private static final int SOFT_LOCK_SPAN_PX = 400;
    private static final long SOFT_LOCK_MS = 20_000;
    // Absolute per-hop wall-clock ceiling — the backstop no movement pattern can evade. The
    // longest legitimate hop (huge map, detours, climbs) stays well under this.
    private static final long HOP_MAX_MS = 90_000;

    private static final ScheduledExecutorService POOL = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "gctravel-poll");
        t.setDaemon(true);
        return t;
    });

    private static final Map<Integer, Trip> TRIPS = new ConcurrentHashMap<>();

    private static final class Trip {
        final Character bot;
        final int destMapId;
        final Consumer<Boolean> callback;
        ScheduledFuture<?> task;
        long settledAtMs;       // when the bot first arrived + stopped at the current hop's portal (0 = not yet)
        int hopBestDist = Integer.MAX_VALUE; // closest the bot has gotten to the current hop's portal
        int lastPosX = Integer.MIN_VALUE;    // last sampled bot position (liveness: is it still moving at all?)
        int lastPosY = Integer.MIN_VALUE;
        long hopProgressAtMs;   // last time the bot made progress (closed on the portal OR moved from its spot)
        long nearTargetSinceMs; // snap clock: since when the bot has been near the hop target (0 = not near)
        long lastNearTargetAtMs;// last sample inside the snap circle (grace keeps brief exits from resetting)
        int softMinX;           // bounding box of samples since the soft-lock window opened
        int softMaxX;
        int softMinY;
        int softMaxY;
        long softLockSinceMs;   // when the current soft-lock window opened (0 = unset)
        long hopStartAtMs;      // when the current hop's map was entered — feeds the absolute ceiling
        int lastMapId = -1;

        Trip(Character bot, int destMapId, Consumer<Boolean> callback) {
            this.bot = bot;
            this.destMapId = destMapId;
            this.callback = callback;
        }
    }

    static void travel(Character bot, int destMapId, Consumer<Boolean> callback) {
        if (bot == null) {
            return;
        }
        cancel(bot);
        if (bot.getMap() != null && bot.getMapId() == destMapId) {
            fire(callback, true);
            return;
        }
        GCMovement.enable(bot);
        Trip trip = new Trip(bot, destMapId, callback);
        trip.hopProgressAtMs = nowMs();
        trip.hopStartAtMs = nowMs();
        trip.task = POOL.scheduleAtFixedRate(
                () -> {
                    try {
                        tick(trip);
                    } catch (Throwable ignored) {
                        // a thrown poll would cancel the periodic task
                    }
                }, 0, POLL_MS, TimeUnit.MILLISECONDS);
        TRIPS.put(bot.getId(), trip);
    }

    static void cancel(Character bot) {
        if (bot == null) {
            return;
        }
        Trip t = TRIPS.remove(bot.getId());
        if (t != null && t.task != null) {
            t.task.cancel(false);
        }
    }

    static boolean isTraveling(Character bot) {
        return bot != null && TRIPS.containsKey(bot.getId());
    }

    private static void tick(Trip trip) {
        Character bot = trip.bot;
        if (bot == null || bot.getMap() == null) {
            finish(trip, false);
            return;
        }
        int cur = bot.getMapId();
        if (cur == trip.destMapId) {
            finish(trip, true);
            return;
        }

        // New map since the last poll = a hop completed (or a warp/unexpected change): reset the
        // hop timer and clear any stale GCMove target so we re-target a portal on this map.
        if (cur != trip.lastMapId) {
            trip.lastMapId = cur;
            trip.settledAtMs = 0L;
            trip.hopBestDist = Integer.MAX_VALUE;
            trip.lastPosX = Integer.MIN_VALUE;
            trip.lastPosY = Integer.MIN_VALUE;
            trip.hopProgressAtMs = nowMs();
            trip.nearTargetSinceMs = 0L;
            trip.softLockSinceMs = 0L;
            trip.hopStartAtMs = nowMs();
            GCMovement.clearMoveIntent(bot);
        }

        List<Integer> route = GCWorldGraph.route(cur, trip.destMapId, MAX_HOPS);
        if (route == null) {
            // No walkable portal path at all (e.g. towns linked only by taxi/ferry, which
            // pure-movement scope drops) — warp the remainder to the destination.
            warp(bot, trip.destMapId, "no walkable portal route to " + trip.destMapId);
            return;
        }
        if (route.isEmpty()) {
            finish(trip, true);
            return;
        }
        int nextHop = route.get(0);

        // Prefer a walkable portal; else a taxi (cab) ride; else warp the hop.
        Portal portal = findPortalTo(bot.getMap(), nextHop);
        if (portal != null) {
            approachAndAct(trip, bot, portal.getPosition(), nextHop,
                    "portal " + portal.getId() + " -> map " + nextHop,
                    () -> GCPortals.enter(bot, portal)); // walk to the portal, stand a beat, step through
            return;
        }
        GCTaxi.TaxiEdge taxi = GCTaxi.edge(cur, nextHop);
        if (taxi != null) {
            Point npcPos = GCTaxi.npcPos(bot.getMap(), taxi.npcId());
            if (npcPos == null) {
                warp(bot, nextHop, "taxi npc " + taxi.npcId() + " not on map " + cur);
                return;
            }
            approachAndAct(trip, bot, npcPos, nextHop,
                    "taxi npc " + taxi.npcId() + " -> map " + nextHop,
                    () -> warp(bot, nextHop, "taxi ride " + cur + " -> " + nextHop)); // walk to cab, ride
            return;
        }
        // Curated scripted-warp portal (e.g. subway entrance): its WZ target is a script, so findPortalTo
        // can't see it. Walk to the portal's spot and warp to the destination, like the script's pi.warp.
        BotScriptedWarp.WarpEdge sw = BotScriptedWarp.edge(cur, nextHop);
        if (sw != null) {
            Point trigger = BotScriptedWarp.portalPos(bot.getMap(), sw.portalName());
            if (trigger == null) {
                warp(bot, nextHop, "scripted portal '" + sw.portalName() + "' not on map " + cur);
                return;
            }
            approachAndAct(trip, bot, trigger, nextHop,
                    "scripted portal '" + sw.portalName() + "' -> map " + nextHop,
                    () -> warpToPortal(bot, sw.toMapId(), sw.toPortalId(),
                            "scripted warp " + cur + " -> " + nextHop));
            return;
        }
        warp(bot, nextHop, "no walkable portal/taxi/scripted-warp on map " + cur + " to " + nextHop);
    }

    /*
     * Walk the bot to dest, wait until it has arrived AND finished the walk (not mid-stride),
     * stand a short dwell, then run action (enter the portal / ride the cab). Progress-aware:
     * a bot still closing the distance OR just moving across the map is never cut off; only a bot that stays
     * in the same spot for the whole window warps the hop.
     */
    private static void approachAndAct(Trip trip, Character bot, Point dest, int nextHop, String intent, Runnable action) {
        Point bp = bot.getPosition();
        long now = nowMs();

        // Absolute per-hop ceiling: whatever the physics looked like, a hop this old has failed.
        if (now - trip.hopStartAtMs >= HOP_MAX_MS) {
            warp(bot, nextHop, "HOP-CEILING: hop " + (HOP_MAX_MS / 1000) + "s old on map "
                    + bot.getMapId() + " at (" + bp.x + "," + bp.y + "), intent: " + intent);
            return;
        }

        // Snap clock: time since the bot first came near the hop target. Survives brief
        // excursions (wide arc, slide back down the slope) via the grace window.
        long sdx = bp.x - dest.x;
        long sdy = bp.y - dest.y;
        boolean nearTarget = sdx * sdx + sdy * sdy <= (long) SNAP_ENTER_RADIUS_PX * SNAP_ENTER_RADIUS_PX;
        if (nearTarget) {
            trip.lastNearTargetAtMs = now;
            if (trip.nearTargetSinceMs == 0L) {
                trip.nearTargetSinceMs = now;
            }
        } else if (trip.nearTargetSinceMs != 0L && now - trip.lastNearTargetAtMs > SNAP_NEAR_GRACE_MS) {
            trip.nearTargetSinceMs = 0L;
        }

        boolean atDest = Math.abs(bp.x - dest.x) <= ENTER_X && Math.abs(bp.y - dest.y) <= ENTER_Y;
        boolean settled = atDest && !GCMovement.isMoving(bot);
        if (settled) {
            if (trip.settledAtMs == 0L) {
                trip.settledAtMs = now;                           // just arrived + stopped: start dwell
            } else if (now - trip.settledAtMs >= PORTAL_ENTER_DWELL_MS) {
                action.run();                                     // stood a beat — act now
            }
            return;
        }
        trip.settledAtMs = 0L;                                    // still walking / knocked off: restart dwell

        // Basically on top of the target but never settling — mid-arc counts as moving, so a bot
        // arc-jumping over the portal on a curved floor would hover here forever. Just act.
        if (nearTarget && now - trip.nearTargetSinceMs >= SNAP_ENTER_MS) {
            action.run();
            return;
        }

        // Soft-lock treadmill window (see SOFT_LOCK_* above): samples confined to one small
        // bounding box for the whole window — warp the hop and log where it was and what it
        // wanted, so recurring bad map+portal approaches can be mined from the log.
        if (trip.softLockSinceMs == 0L) {
            trip.softLockSinceMs = now;
            trip.softMinX = bp.x;
            trip.softMaxX = bp.x;
            trip.softMinY = bp.y;
            trip.softMaxY = bp.y;
        } else {
            trip.softMinX = Math.min(trip.softMinX, bp.x);
            trip.softMaxX = Math.max(trip.softMaxX, bp.x);
            trip.softMinY = Math.min(trip.softMinY, bp.y);
            trip.softMaxY = Math.max(trip.softMaxY, bp.y);
            if (trip.softMaxX - trip.softMinX > SOFT_LOCK_SPAN_PX
                    || trip.softMaxY - trip.softMinY > SOFT_LOCK_SPAN_PX) {
                trip.softLockSinceMs = now;                       // outgrew the box: real travel — reopen here
                trip.softMinX = bp.x;
                trip.softMaxX = bp.x;
                trip.softMinY = bp.y;
                trip.softMaxY = bp.y;
            } else if (now - trip.softLockSinceMs >= SOFT_LOCK_MS) {
                warp(bot, nextHop, "SOFT-LOCK: confined to "
                        + (trip.softMaxX - trip.softMinX) + "x" + (trip.softMaxY - trip.softMinY)
                        + "px for " + (SOFT_LOCK_MS / 1000) + "s on map " + bot.getMapId()
                        + " at (" + bp.x + "," + bp.y + "), intent: " + intent);
                return;
            }
        }

        // Progress = the bot is closing on the portal OR simply moving from where it stood. Straight-line
        // distance to the portal can plateau on a big map while the bot travels fine (climbing to an elevated
        // portal, detouring round terrain, getting knocked around by mobs), so a stalled distance alone is NOT
        // stuck. The position-displacement is measured from the last progress point (anchor), not the previous
        // tick, so slow steady movement still accumulates across the window. Only a bot that stays inside the
        // move epsilon for the whole window is genuinely wedged and gets warped.
        int dist = Math.abs(bp.x - dest.x) + Math.abs(bp.y - dest.y);
        boolean closingIn = dist < trip.hopBestDist - HOP_PROGRESS_EPS_PX;
        boolean moved = trip.lastPosX == Integer.MIN_VALUE
                || Math.abs(bp.x - trip.lastPosX) + Math.abs(bp.y - trip.lastPosY) > HOP_MOVE_EPS_PX;
        if (closingIn || moved) {
            if (closingIn) {
                trip.hopBestDist = dist;
            }
            trip.lastPosX = bp.x;
            trip.lastPosY = bp.y;
            trip.hopProgressAtMs = now;
        } else if (now - trip.hopProgressAtMs > HOP_STUCK_MS) {
            warp(bot, nextHop, "stuck " + (HOP_STUCK_MS / 1000) + "s — not moving toward hop target on map " + bot.getMapId());
            return;
        }

        if (!GCMovement.isMoving(bot)) {
            GCMovement.move(bot, dest.x, dest.y);                 // (re)issue the walk to the hop target
        }
    }

    private static Portal findPortalTo(MapleMap map, int targetMapId) {
        for (Portal p : map.getPortals()) {
            if (p.getTargetMapId() == targetMapId) {
                return p;
            }
        }
        return null;
    }

    private static void warp(Character bot, int mapId, String reason) {
        BotLogger.log("[GCTravel] " + bot.getName() + " warped to map " + mapId + " — " + reason);
        try {
            bot.changeMap(mapId);
        } catch (Throwable ignored) {
            // a bad map id / lifecycle race shouldn't kill the trip; the next poll re-evaluates
        }
    }

    private static void warpToPortal(Character bot, int mapId, int portalId, String reason) {
        BotLogger.log("[GCTravel] " + bot.getName() + " warped to map " + mapId + " portal " + portalId + " — " + reason);
        try {
            bot.changeMap(mapId, portalId);
        } catch (Throwable ignored) {
            // a bad map id / lifecycle race shouldn't kill the trip; the next poll re-evaluates
        }
    }

    private static void finish(Trip trip, boolean ok) {
        TRIPS.remove(trip.bot.getId());
        if (trip.task != null) {
            trip.task.cancel(false);
        }
        // Clear the travel's move intent only — never tear down an active follow session (a
        // follow-driven trip arriving on the target's map must let GCFollow resume same-map follow).
        GCMovement.clearMoveIntent(trip.bot);
        fire(trip.callback, ok);
    }

    private static void fire(Consumer<Boolean> cb, boolean ok) {
        if (cb != null) {
            try {
                cb.accept(ok);
            } catch (Throwable ignored) {
                // callback errors must not propagate into the poller
            }
        }
    }

    private static long nowMs() {
        return System.nanoTime() / 1_000_000L;
    }
}
