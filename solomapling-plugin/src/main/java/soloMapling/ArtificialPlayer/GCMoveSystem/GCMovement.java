package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Rope;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Public façade for GCMoveSystem — the GreenCat dynamic (calculation-based) movement engine.
 * This is the only public class in the package; everything below it (physics, nav,
 * graph baking, packet emission) is package-private plumbing.
 *
 * Two capabilities, both fire-and-forget and non-blocking:
 * 
 *   .move(Character, int, int) — walk/jump/climb to a point on the current map.
 *   .follow(Character, Character) — dynamically tail a character.
 * 
 * Both drive a raw org.gms.client.Character, so any BotSM subtype calls them via
 * getChr(), exactly like the recorded-path MovementCommands. The two engines
 * coexist via the shared MovementCommands movement lock (acquired on enable, released on
 * disable).
 */
// Ported from GreenCatMS. Credit: NutNNut.
public final class GCMovement {
    private GCMovement() {
    }

    private static final Map<Integer, BotMovementState> STATES = new ConcurrentHashMap<>();
    private static final Map<Integer, Runnable> ARRIVAL_CALLBACKS = new ConcurrentHashMap<>();

    // ── Lifecycle ───────────────────────────────────────────────────────────

    /* Put a bot under dynamic control: build its profile, warm the map graph, start the tick. */
    public static void enable(Character bot) {
        if (bot == null) {
            return;
        }
        ObserverTracker.ensureStarted(); // LOD observability poll (idempotent)
        STATES.computeIfAbsent(bot.getId(), id -> {
            // owner == null by default (no follow anchor, and avoids the nav warmup notice trying to
            // dropMessage through the shared BotClient). GCFollow sets owner to the followed character.
            BotMovementState st = new BotMovementState(bot, null);
            st.movementProfile = BotMovementProfile.fromCharacter(bot);
            if (bot.getMap() != null) {
                st.lastMapId = bot.getMapId();
                st.fhIndex = BotMovementManager.buildFhIndex(bot.getMap());
                Point cur = bot.getPosition();
                Point ground = BotPhysicsEngine.findGroundPoint(bot.getMap(), new Point(cur.x, cur.y - 1));
                BotPhysicsEngine.teleportTo(st, bot, ground != null ? ground : cur);
                BotMovementManager.resetEntryStateAfterTeleport(st);
                BotNavigationGraphProvider.warmGraphAsync(bot.getMap(), st.movementProfile);
            }
            GCMovementDriver.start(st);
            // Hold the shared movement lock for the whole dynamic session so the recorded-path
            // engine can't drive this bot concurrently.
            MovementCommands.tryAcquireMovementLock(bot);
            return st;
        });
    }

    /* Remove a bot from dynamic control and release the shared movement lock. */
    public static void disable(Character bot) {
        if (bot == null) {
            return;
        }
        GCFollow.cancel(bot);
        GCTravel.cancel(bot);
        GCFidget.cancel(bot);
        BotMovementState st = STATES.remove(bot.getId());
        if (st != null) {
            GCMovementDriver.stop(st);
            MovementCommands.releaseMovementLock(bot);
        }
        ARRIVAL_CALLBACKS.remove(bot.getId());
    }

    public static boolean isEnabled(Character bot) {
        return bot != null && STATES.containsKey(bot.getId());
    }

    /* Package-private snapshot of the enabled dynamic states (for LodMetrics reporting). */
    static java.util.Collection<BotMovementState> enabledStates() {
        return new java.util.ArrayList<>(STATES.values());
    }

    // ── Commands ────────────────────────────────────────────────────────────

    /* Walk/jump/climb to (x,y) on the bot's current map, then idle. */
    public static void move(Character bot, int x, int y) {
        move(bot, x, y, null);
    }

    /* As .move(Character, int, int) with an arrival callback. */
    public static void move(Character bot, int x, int y, Runnable onArrival) {
        if (bot == null) {
            return;
        }
        enable(bot);
        BotMovementState st = STATES.get(bot.getId());
        if (st == null) {
            return;
        }
        st.following = false;
        st.farmAnchor = null;
        st.farmAnchorMapId = -1;
        st.moveTarget = new Point(x, y);
        st.moveTargetPrecise = true;
        st.moveTargetSource = "gcmove";
        st.moveBestDist = Integer.MAX_VALUE;
        st.moveProgressAtMs = System.currentTimeMillis();
        if (onArrival != null) {
            ARRIVAL_CALLBACKS.put(bot.getId(), onArrival);
        } else {
            ARRIVAL_CALLBACKS.remove(bot.getId());
        }
    }

    /* Anchor at (x,y): walk there and hold position (sentry). */
    public static void farmHere(Character bot, int x, int y) {
        if (bot == null) {
            return;
        }
        enable(bot);
        BotMovementState st = STATES.get(bot.getId());
        if (st == null) {
            return;
        }
        st.following = false;
        st.farmAnchor = new Point(x, y);
        st.farmAnchorMapId = bot.getMapId();
        st.moveTarget = new Point(x, y);
        st.moveTargetPrecise = true;
        st.moveBestDist = Integer.MAX_VALUE;
        st.moveProgressAtMs = System.currentTimeMillis();
    }

    /* Dynamically tail a character — including ACROSS maps (travels to the target's map when they
     *  portal away, then resumes following on arrival). */
    public static void follow(Character bot, Character target) {
        if (bot == null || target == null) {
            return;
        }
        enable(bot);
        GCFollow.start(bot, target);
    }

    public static boolean isFollowing(Character bot) {
        return GCFollow.isFollowing(bot);
    }

    /* Cancel the current move/follow/travel; the bot idles in place (stays under dynamic control). */
    public static void stop(Character bot) {
        if (bot == null) {
            return;
        }
        GCFollow.cancel(bot);
        GCTravel.cancel(bot);
        clearMoveIntent(bot);
        BotMovementState st = STATES.get(bot.getId());
        if (st != null) {
            st.following = false;
            st.owner = null;
        }
    }

    /* Hard-teleport the bot to solid ground at/under (x,y) on its current map, resetting nav state so the
     * driver resumes cleanly from the new spot. For recovery when a bot is wedged and can't path out. */
    public static void teleportTo(Character bot, int x, int y) {
        if (bot == null) {
            return;
        }
        enable(bot);
        BotMovementState st = STATES.get(bot.getId());
        if (st == null) {
            return;
        }
        Point ground = BotPhysicsEngine.findGroundPoint(bot.getMap(), new Point(x, y));
        BotPhysicsEngine.teleportTo(st, bot, ground != null ? ground : new Point(x, y));
        BotMovementManager.resetEntryStateAfterTeleport(st);
        BotMovementManager.broadcastMovement(st);
    }

    /* Flag the bot as combat-alerted so it renders the 5s ALERT pose. The observing client already starts
     * its own alert timer when it renders our attack packet; this keeps the bot's OWN movement broadcasts
     * carrying ALERT instead of STAND for the duration, so a following idle/move packet doesn't cancel the
     * pose. Called by the attack layer after each swing. No-op if the bot isn't under GC control. */
    public static void markAlerted(Character bot) {
        if (bot == null) {
            return;
        }
        BotMovementState st = STATES.get(bot.getId());
        if (st != null) {
            BotContactDamage.markAlerted(st);
        }
    }

    /* Mage blink toward a point on the current map: pick a same-ledge landing up to ~150px toward it and
     * broadcast the teleport (or blink down to a lower platform). Returns true if it blinked, false if there's
     * no valid landing (caller walks). The caller (MovementStylePolicy / grind approach) decides who may
     * teleport — no MP/skill checks here (bots are decoration). */
    public static boolean teleport(Character bot, int targetX, int targetY) {
        if (bot == null) {
            return false;
        }
        BotMovementState st = STATES.get(bot.getId());
        return st != null && GCMovementSkills.execTeleport(st, bot, targetX, targetY);
    }

    /* Thief flash-jump (air dash) toward a target X on the current map. scaleCap = the bot's level-tier
     * dash cap (FlashJumpTiers; 1 = full maxed dash) — the platform fit may downshift under it so a small
     * platform gets a short dash. Returns true if it dashed, false if no arc fits (caller walks). */
    public static boolean flashJump(Character bot, int targetX, float scaleCap) {
        if (bot == null) {
            return false;
        }
        BotMovementState st = STATES.get(bot.getId());
        return st != null && GCMovementSkills.execFlashJump(st, bot, targetX, scaleCap);
    }

    /* Dev/calibration (the !gcmove fj hook): fire one flash jump toward dir (+1/-1) at an exact scale,
     * bypassing the level cap and platform fit, so real dash travel can be measured on flat ground. */
    public static boolean debugFlashJump(Character bot, int dir, float scale) {
        if (bot == null) {
            return false;
        }
        BotMovementState st = STATES.get(bot.getId());
        return st != null && GCMovementSkills.execFlashJumpForced(st, bot, dir, scale);
    }

    /* The bot's current facing under dynamic control: true = facing left, false = right, or null when it
     * isn't GC-driven. Used by the grind brain's turn-around micro-beat (face+step before swinging). */
    public static Boolean isFacingLeft(Character bot) {
        if (bot == null) {
            return null;
        }
        BotMovementState st = STATES.get(bot.getId());
        return (st == null) ? null : st.facingDir < 0;
    }

    // ── Package helpers (used by GCTravel / GCFollow) ────────────────────────

    /* Clear only the move/farm target + nav (NOT follow or travel). GCTravel uses this between hops
     *  and on arrival so it never tears down an active follow session. */
    static void clearMoveIntent(Character bot) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        if (st == null) {
            return;
        }
        st.moveTarget = null;
        st.moveTargetPrecise = false;
        st.farmAnchor = null;
        st.farmAnchorMapId = -1;
        BotMovementManager.clearNavigationState(st);
        ARRIVAL_CALLBACKS.remove(bot.getId());
    }

    /* GCFollow: target is on the bot's map — arm same-map follow (the driver does the walking). */
    static void armSameMapFollow(Character bot, Character target) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        if (st == null || target == null) {
            return;
        }
        st.owner = target;
        st.followTargetId = target.getId();
        st.following = true;
        st.moveTarget = null;
        st.farmAnchor = null;
        st.farmAnchorMapId = -1;
    }

    /* GCFollow: target is on another map — pause same-map follow while the bot travels there. */
    static void pauseFollowForTravel(Character bot) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        if (st != null) {
            st.following = false;
        }
    }

    /* GCFollow: the follow session ended (target gone) — clear follow state. */
    static void endFollowState(Character bot) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        if (st != null) {
            st.following = false;
            st.owner = null;
        }
    }

    // ── Cross-map travel (GCTravel) ─────────────────────────────────────────

    /* Travel to another map by chaining GCMove-to-portal + portal entry (warp special hops). */
    public static void travel(Character bot, int destMapId) {
        GCTravel.travel(bot, destMapId, null);
    }

    /* As .travel(Character, int) with a success/fail callback fired on arrival/abort. */
    public static void travel(Character bot, int destMapId, java.util.function.Consumer<Boolean> onDone) {
        GCTravel.travel(bot, destMapId, onDone);
    }

    public static boolean isTraveling(Character bot) {
        return GCTravel.isTraveling(bot);
    }

    public static void cancelTravel(Character bot) {
        GCTravel.cancel(bot);
    }

    /*
     * Travel to another map and then walk to (x,y) on it — e.g. "come to where I am".
     * Cross-map hops + final in-map navigation in one call. Captures nothing itself; the caller
     * passes the destination point (typically the commanding player's position at command time).
     */
    public static void travelTo(Character bot, int mapId, int x, int y) {
        travelTo(bot, mapId, x, y, null);
    }

    public static void travelTo(Character bot, int mapId, int x, int y, java.util.function.Consumer<Boolean> onDone) {
        if (bot == null) {
            return;
        }
        Runnable arrive = onDone == null ? null : () -> onDone.accept(true);
        if (bot.getMap() != null && bot.getMapId() == mapId) {
            move(bot, x, y, arrive); // already on the map — just navigate to the spot
            return;
        }
        GCTravel.travel(bot, mapId, ok -> {
            if (ok) {
                move(bot, x, y, arrive); // arrived on the destination map — now walk to the spot
            } else if (onDone != null) {
                onDone.accept(false);
            }
        });
    }

    /* Diagnostic: the portal-hop route from the bot's current map to destMapId (no movement). */
    public static String routeReport(Character bot, int destMapId) {
        if (bot == null || bot.getMap() == null) {
            return "GCTravel: no map.";
        }
        int from = bot.getMapId();
        long startedAt = System.nanoTime();
        java.util.List<Integer> route = GCWorldGraph.route(from, destMapId, 12);
        long ms = (System.nanoTime() - startedAt) / 1_000_000L;
        if (route == null) {
            return String.format("GCTravel route %d -> %d: NONE (warp; %d maps indexed, %dms)",
                    from, destMapId, GCWorldGraph.mapCount(), ms);
        }
        if (route.isEmpty()) {
            return "GCTravel: already on map " + destMapId;
        }
        return String.format("GCTravel route %d -> %d: %d hops %s (%dms)",
                from, destMapId, route.size(), route, ms);
    }

    public static boolean isMoving(Character bot) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        return st != null && (st.moveTarget != null || st.following || st.inAir || st.climbing
                || st.navEdge != null || st.portalDropAtMs > 0L);
    }

    /* True while the bot is on a rope/ladder (cleared only once it's back on a foothold). Combat holds
     * off attacking until then so the bot doesn't swing from the rope when a mob is near the rope top. */
    public static boolean isClimbing(Character bot) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        return st != null && st.climbing;
    }

    /* Mark the bot as actively grinding so the movement layer's grind-specific guards engage — chiefly
     * it stops idle-hanging on a rope (shouldHoldClimbIdle) and instead dismounts to keep fighting.
     * Set on GRIND entry, cleared when the bot leaves the grind. */
    public static void setGrinding(Character bot, boolean grinding) {
        if (bot == null) {
            return;
        }
        if (grinding) {
            enable(bot); // ensure a state exists to carry the flag
        }
        BotMovementState st = STATES.get(bot.getId());
        if (st != null) {
            st.grinding = grinding;
        }
    }

    /* Mark the bot as holding an explicit rest hang on a rope (a grind break's rope rest). While set, the
     * climb-idle hold stays engaged regardless of the grind guard, and the driver freezes the hang so no
     * nav / player-reaction / fidget layer can dislodge it. The bot must already be on the rope (isClimbing)
     * when this is set true; clear it before dismounting. No-op if the bot isn't under GC control when
     * clearing. */
    public static void setRestHold(Character bot, boolean resting) {
        if (bot == null) {
            return;
        }
        if (resting) {
            enable(bot); // ensure a state exists to carry the flag
        }
        BotMovementState st = STATES.get(bot.getId());
        if (st != null) {
            st.resting = resting;
        }
    }

    public static boolean isResting(Character bot) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        return st != null && st.resting;
    }

    /* Jump off the rope/ladder the bot is on, biased toward dx (-1 left, +1 right, 0 straight off).
     * No-op if not currently climbing. Used by grind recovery to dismount instead of hanging. */
    public static void dismountRope(Character bot, int dx) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        if (st != null && st.climbing) {
            BotMovementManager.jumpOffRope(st, bot, dx);
        }
    }

    /* True while the bot is climbing a rope/ladder as part of a committed navigation edge — i.e. the
     * driver is intentionally routing it up/down to another ledge (relocating to a fresh grind section,
     * approaching an upper-ledge mob), NOT hanging idle on a rope. Grind recovery uses this to leave a
     * deliberate traversal climb alone instead of fighting it, which would thrash mount/dismount. */
    public static boolean isNavigatingClimb(Character bot) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        return st != null && st.climbing && st.navEdge != null;
    }

    // ── Idle fidget primitives (organic liveliness) ─────────────────────────

    /* A standing hop in place. */
    public static void jumpInPlace(Character bot) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        if (st != null && !st.inAir && !st.climbing) {
            BotMovementManager.initiateJump(st, bot, 0);
        }
    }

    // ours: a directional (arced) engage hop toward dx (-1 left / +1 right; 0 = vertical). Unlike
    // jumpInPlace (always vertical), a non-zero dx launches a real moving arc — the manager/physics
    // already carry the ±walkStep horizontal velocity. Used by the grind brain's jump-attack so
    // thieves close/kite in an arc instead of pogo-ing. No-op if already airborne or on a rope.
    public static void jumpToward(Character bot, int dx) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        if (st != null && !st.inAir && !st.climbing) {
            BotMovementManager.initiateJump(st, bot, dx);
        }
    }

    /* Flip the bot's facing (left↔right) while idle; the driver renders the new stand stance. */
    public static void turnAround(Character bot) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        if (st != null) {
            st.facingDir = -st.facingDir;
        }
    }

    /* Face a direction (true = left) and broadcast the new idle stance immediately. Broadcasting now
     * (rather than waiting for the next tick) means the turn shows before whatever the caller does next
     * - e.g. an attack swing - and the client's last-movement stance is the new facing, so it won't snap
     * back the instant the swing ends. No-op if the bot isn't under dynamic control. */
    public static void face(Character bot, boolean left) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        if (st != null) {
            st.facingDir = left ? -1 : 1;
            BotMovementManager.broadcastMovement(st);
        }
    }

    /* Crouch/duck for durationMs (the driver holds the prone pose while idle). */
    public static void duck(Character bot, int durationMs) {
        BotMovementState st = bot == null ? null : STATES.get(bot.getId());
        if (st != null && !st.inAir && !st.climbing) {
            st.duckUntilMs = System.currentTimeMillis() + Math.max(1, durationMs);
        }
    }

    /* A small step to (x,y) (used by the auto-fidget for nudges / wander-and-return). */
    public static void nudgeTo(Character bot, int x, int y) {
        move(bot, x, y);
    }

    /* Toggle idle auto-fidget (turn / duck / hop / small wander near its rest spot, returns home). */
    public static void setFidget(Character bot, boolean on) {
        if (bot == null) {
            return;
        }
        if (on) {
            enable(bot);
            GCFidget.start(bot);
        } else {
            GCFidget.cancel(bot);
        }
    }

    public static boolean isFidgeting(Character bot) {
        return GCFidget.isActive(bot);
    }

    // ── Observability (LOD tier gate for callers) ───────────────────────────

    /*
     * True when a real player is on mapId — i.e. the map is "observed" (FULL tier). This is
     * the public gate other systems use to decide REAL vs ABSTRACT behavior (e.g. a training bot runs
     * real combat only on observed maps). Wraps the package-private ObserverTracker; never
     * forces the observer poll to start (it is started by .enable).
     */
    public static boolean isMapObserved(int mapId) {
        return ObserverTracker.isFull(mapId);
    }

    /*
     * Force mapId to FULL ("observed") immediately for a short window, regardless of the ~1s observer
     * poll - so visible movement / combat / broadcast resume the same tick a real player arrives,
     * instead of up to one poll later. Used by BotMapEntryResponder when a real player enters a map.
     */
    public static void markObservedNow(int mapId) {
        ObserverTracker.markObservedNow(mapId);
    }

    /*
     * This map's current LOD tier as a label: "full" (a real player is here), "halo" (a real player is on
     * a portal-adjacent map), "dwell" (recently observed, still held at full physics by the hysteresis
     * window), or "coarse" (unobserved). Diagnostics — see !gcmove lod train.
     */
    public static String lodTier(int mapId) {
        if (ObserverTracker.isFull(mapId)) {
            return "full";
        }
        if (ObserverTracker.isHalo(mapId)) {
            return "halo";
        }
        if (ObserverTracker.isActiveMap(mapId)) {
            return "dwell";
        }
        return "coarse";
    }

    /* Snapshot of the maps currently FULL (a real player present). Diagnostics only. */
    public static java.util.Set<Integer> observedFullMaps() {
        return ObserverTracker.fullMaps();
    }

    /* Snapshot of the maps currently HALO (portal-adjacent to a real player). Diagnostics only. */
    public static java.util.Set<Integer> observedHaloMaps() {
        return ObserverTracker.haloMaps();
    }

    // ── Spatial terrain queries (generic nav-graph reads; used by grind-spot finding, placement, …) ──

    /* A walkable ground ledge: a baked nav-graph region's id + bounds + center. */
    public record Ledge(int regionId, int minX, int maxX, int centerX, int centerY) {
    }

    /* Every walkable ground ledge on the map (ropes/ladders excluded). Empty if the graph isn't baked. */
    public static List<Ledge> walkableLedges(MapleMap map) {
        BotNavigationGraph g = BotNavigationGraphProvider.getGraph(map);
        if (g == null) {
            return List.of();
        }
        List<Ledge> out = new java.util.ArrayList<>();
        for (BotNavigationGraph.Region r : g.regions) {
            if (r.isRopeRegion || r.isLadder) {
                continue;
            }
            Point c = r.centerPoint();
            out.add(new Ledge(r.id, r.minX, r.maxX, c.x, c.y));
        }
        return out;
    }

    /* The ledge (region id) under (x,y), or -1 if none. */
    public static int regionIdAt(MapleMap map, int x, int y) {
        BotNavigationGraph g = BotNavigationGraphProvider.getGraph(map);
        return g == null ? -1 : g.findRegionId(map, new Point(x, y));
    }

    /*
     * True only when (ax,ay) and (bx,by) rest on two DIFFERENT walkable ledges of an ALREADY-baked
     * graph - it never triggers a build (peek only), so it is safe on hot paths like the bot combat
     * tick. Both points resolve against the same graph instance so the region ids are comparable.
     * Returns false when the map isn't baked yet, or when either point is on no ledge (a flying mob
     * over a floor, a point mid-air): callers treat that as "can't tell, don't filter" rather than
     * "different". Lets bot attacks reject mobs standing on a separate platform above/below instead
     * of gating only by a vertical pixel box.
     */
    public static boolean onDifferentLedge(MapleMap map, int ax, int ay, int bx, int by) {
        BotNavigationGraph g = BotNavigationGraphProvider.peekGraph(map);
        if (g == null) {
            return false;
        }
        int ra = g.findRegionId(map, new Point(ax, ay));
        int rb = g.findRegionId(map, new Point(bx, by));
        return ra >= 0 && rb >= 0 && ra != rb;
    }

    /* The set of region ids reachable from the ledge under (fromX,fromY) (empty if it's on none). */
    public static java.util.Set<Integer> reachableRegions(MapleMap map, int fromX, int fromY) {
        BotNavigationGraph g = BotNavigationGraphProvider.getGraph(map);
        if (g == null) {
            return java.util.Set.of();
        }
        int start = g.findRegionId(map, new Point(fromX, fromY));
        if (start < 0) {
            return java.util.Set.of();
        }
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        java.util.Deque<Integer> queue = new java.util.ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            int rid = queue.poll();
            for (BotNavigationGraph.Edge e : g.getOutgoing(rid)) {
                if (e.toRegionId != rid && seen.add(e.toRegionId)) {
                    queue.add(e.toRegionId);
                }
            }
        }
        return seen;
    }

    /* The exact ground point on a region at x (slope-aware), or null if the region is gone. Lets a
     * roaming bot leash its move target to a chosen ledge's ground line. */
    public static Point groundPointInRegion(MapleMap map, int regionId, int x) {
        BotNavigationGraph g = BotNavigationGraphProvider.getGraph(map);
        if (g == null) {
            return null;
        }
        BotNavigationGraph.Region r = g.getRegion(regionId);
        return r == null ? null : r.pointAt(x);
    }

    /* Snap an arbitrary (possibly airborne) point down to the foothold it rests over, or null if there's
     * no floor below it. Lets a bot aim a move at a jumping/airborne mob's actual platform instead of its
     * raw y, so the pathfinder doesn't take a long detour to reach a mob that's really right in front. */
    public static Point groundPointBelow(MapleMap map, int x, int y) {
        return BotPhysicsEngine.findGroundPoint(map, new Point(x, y));
    }

    /* Map ids reachable from fromMapId within maxHops over WALKABLE portals plus curated scripted warps
     * (e.g. the Kerning subway entrance), so subway-style training maps are discoverable. Excludes the
     * start map and taxi/ferry hops (keeps discovery town-local). Triggers the one-time world-graph build
     * on first call. */
    public static List<Integer> mapsWithinHops(int fromMapId, int maxHops) {
        return new java.util.ArrayList<>(mapsWithinHopsByDepth(fromMapId, maxHops).keySet());
    }

    /* As mapsWithinHops, but returns each reachable map mapped to its hop distance from fromMapId (1 =
     * adjacent). Insertion order is BFS order. Lets callers weight maps by how far out they are. */
    public static java.util.Map<Integer, Integer> mapsWithinHopsByDepth(int fromMapId, int maxHops) {
        java.util.LinkedHashMap<Integer, Integer> out = new java.util.LinkedHashMap<>();
        if (maxHops <= 0) {
            return out;
        }
        java.util.Map<Integer, int[]> g = GCWorldGraph.get();
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        java.util.ArrayDeque<Integer> frontier = new java.util.ArrayDeque<>();
        seen.add(fromMapId);
        frontier.add(fromMapId);
        int depth = 0;
        while (!frontier.isEmpty() && depth < maxHops) {
            depth++;
            for (int level = frontier.size(); level > 0; level--) {
                int current = frontier.poll();
                for (int next : g.getOrDefault(current, new int[0])) {
                    if (seen.add(next)) {
                        out.put(next, depth);
                        frontier.add(next);
                    }
                }
                // curated scripted warps (subway entrance, etc.) — a local hop, unlike taxi/ferry
                for (int next : soloMapling.ArtificialPlayer.BotTravelSystem.BotScriptedWarp.destinations(current)) {
                    if (seen.add(next)) {
                        out.put(next, depth);
                        frontier.add(next);
                    }
                }
            }
        }
        return out;
    }

    // ── LOD measurement tooling (M0) ────────────────────────────────────────

    /* Human-readable snapshot of the current dynamic-movement load (lines to drop to the GM). */
    public static List<String> lodStats() {
        return LodMetrics.stats();
    }

    /* Enable dynamic movement (+ idle fidget) on up to n idle bots to generate load. */
    public static int lodLoad(int n) {
        return LodMetrics.load(n);
    }

    /* Release every bot enabled by .lodLoad(int). */
    public static int lodUnload() {
        return LodMetrics.unload();
    }

    // ── Diagnostics (Phase-2 gate) ──────────────────────────────────────────

    /*
     * Force-bake the nav graph for the bot's current map (using its movement profile) and return
     * a human-readable summary of region / edge / rope counts. This is the "no hardcoded paths"
     * proof point — it exercises the WZ geometry load + physics-simulation edge discovery.
     */
    public static String bakeReport(Character bot) {
        if (bot == null || bot.getMap() == null) {
            return "GCMove: no map.";
        }
        BotMovementProfile profile = BotMovementProfile.fromCharacter(bot);
        long startedAt = System.nanoTime();
        BotNavigationGraph g = BotNavigationGraphProvider.rebuildGraph(bot.getMap(), profile);
        long ms = (System.nanoTime() - startedAt) / 1_000_000L;
        if (g == null) {
            return "GCMove: bake FAILED for map " + bot.getMapId();
        }
        int regions = g.regions.size();
        int walk = 0, jump = 0, drop = 0, climb = 0, portal = 0, total = 0;
        for (List<BotNavigationGraph.Edge> edges : g.outgoingByRegionId.values()) {
            for (BotNavigationGraph.Edge e : edges) {
                total++;
                switch (e.type) {
                    case WALK -> walk++;
                    case JUMP -> jump++;
                    case DROP -> drop++;
                    case CLIMB -> climb++;
                    case PORTAL -> portal++;
                }
            }
        }
        int ropes = bot.getMap().getRopes().size();
        return String.format(
                "GCMove bake map %d in %dms: regions=%d ropes=%d edges=%d "
                        + "(walk=%d jump=%d drop=%d climb=%d portal=%d)",
                bot.getMapId(), ms, regions, ropes, total, walk, jump, drop, climb, portal);
    }

    // Diagnostic for the widened rope top-exit probe. Iterates the player's map ropes, runs the
    // shared BotPhysicsEngine.findTopExitLanding, and compares against the old strict probe
    // (exactly rope.x, topY-3..topY+climbStep+2). For each rope prints x/topY/bottomY, ladder-or-rope,
    // the new landing Y (or none), old vs new pass, and which failure mode the old probe would hit.
    // Feeds tolerance calibration (TOP_EXIT_UP_TOL/DOWN_TOL/X_TOL) against real WZ geometry.
    private static final int ROPECHECK_MAX_LINES = 40;

    public static List<String> ropeCheckReport(Character player) {
        List<String> out = new ArrayList<>();
        if (player == null || player.getMap() == null) {
            out.add("GCMove ropecheck: no map.");
            return out;
        }
        MapleMap map = player.getMap();
        List<Rope> ropes = map.getRopes();
        int oldStrictBand = BotPhysicsEngine.climbStepPerTick() + 2; // topY+this was the old accept ceiling
        out.add("=== !gcmove ropecheck map " + player.getMapId() + " (" + ropes.size() + " ropes) ===");
        out.add(String.format("tol: up=%d down=%d x=%d (old band: rope.x, topY-3..topY+%d)",
                BotPhysicsEngine.TOP_EXIT_UP_TOL, BotPhysicsEngine.TOP_EXIT_DOWN_TOL,
                BotPhysicsEngine.TOP_EXIT_X_TOL, oldStrictBand));

        int oldPassCount = 0, newPassCount = 0, recovered = 0, stillFail = 0;
        int shown = 0;
        for (Rope rope : ropes) {
            int topY = rope.topY();
            Point oldGround = BotPhysicsEngine.pointBelowIndexed(map, new Point(rope.x(), topY - 3));
            boolean oldPass = oldGround != null && oldGround.y <= topY + oldStrictBand;
            Point newLanding = BotPhysicsEngine.findTopExitLanding(map, rope);
            boolean newPass = newLanding != null;

            if (oldPass) oldPassCount++;
            if (newPass) newPassCount++;
            if (!oldPass && newPass) recovered++;
            if (!oldPass && !newPass) stillFail++;

            if (shown < ROPECHECK_MAX_LINES) {
                String verdict;
                if (oldPass) {
                    verdict = "OK";
                } else if (!newPass) {
                    verdict = "STILL-FAIL (no foothold in widened band)";
                } else if (newLanding.x != rope.x()) {
                    verdict = "recovered FM-3 (off-axis dx=" + (newLanding.x - rope.x()) + ")";
                } else if (newLanding.y < topY) {
                    verdict = "recovered FM-2 (above top by " + (topY - newLanding.y) + ")";
                } else {
                    verdict = "recovered FM-1 (below top by " + (newLanding.y - topY) + ")";
                }
                out.add(String.format("  x=%d topY=%d botY=%d %s | new landY=%s | old=%s -> %s",
                        rope.x(), topY, rope.bottomY(), rope.isLadder() ? "ladder" : "rope",
                        newPass ? String.valueOf(newLanding.y) : "none",
                        oldPass ? "pass" : "fail", verdict));
                shown++;
            }
        }
        if (shown < ropes.size()) {
            out.add("  ... " + (ropes.size() - shown) + " more (capped at " + ROPECHECK_MAX_LINES + ")");
        }
        out.add(String.format("summary: old-pass=%d new-pass=%d recovered=%d still-fail=%d",
                oldPassCount, newPassCount, recovered, stillFail));
        return out;
    }

    // ── Internal driver callback ────────────────────────────────────────────

    /* Driver hook: a move was abandoned (no progress / unreachable) — drop its callback unfired. */
    static void abandonMove(BotMovementState entry) {
        if (entry != null && entry.bot != null) {
            ARRIVAL_CALLBACKS.remove(entry.bot.getId());
        }
    }

    static void fireArrival(BotMovementState entry) {
        if (entry == null || entry.bot == null) {
            return;
        }
        Runnable cb = ARRIVAL_CALLBACKS.remove(entry.bot.getId());
        if (cb != null) {
            try {
                cb.run();
            } catch (Throwable ignored) {
                // callback errors must not kill the tick
            }
        }
    }
}
