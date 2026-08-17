package soloMapling.ArtificialPlayer.BotWanderSystem;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotGrindSystem.BotSpotPicker;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;
import soloMapling.server.ExecutorServiceManager;

import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// General-purpose flavor wander: a live bot strolls to a random legal walking point on its current map,
// idles a beat, then strolls somewhere else - so bots disperse instead of piling on the spawn portal
// during "shopping" / town-idle flavor. Map-agnostic; the shop case is just whole-map wander.
//
// Pure reuse of two existing primitives: BotSpotPicker.pickGroundSpot picks the legal point (it gathers
// walkable ledges and reachability-filters from the bot's live position, so a pick is never stranded on
// an island the bot can't path to), and GCMovement.move walks/jumps/climbs there across platforms. The
// only original part here is the loop that ties them together (pick -> stroll -> dwell -> repeat) plus a
// stuck-watchdog. Our own creation (not a GreenCat extraction).
public final class BotWanderSystem {

    private BotWanderSystem() {
    }

    private static final Random RANDOM = new Random();

    private static final int POLL_MS = 400;

    // Minimum horizontal distance of a chosen spot from the bot, so every stroll reads as a visible walk
    // rather than a twitch. Kept above ARRIVE_X so a freshly-issued move can never instantly false-arrive.
    private static final int MIN_STROLL = 60;
    private static final int STROLL_ATTEMPTS = 6;

    // Arrival box. X is tight; Y is generous because the ground point sits on a ledge the bot settles onto.
    private static final int ARRIVE_X = 25;
    private static final int ARRIVE_Y = 60;

    // Progress / stuck watchdog: if the bot makes no headway toward its target for STUCK_MS, abandon it and
    // pick a new spot instead of standing on the floor forever (geometry the pather can't quite settle on).
    private static final int PROGRESS_EPS = 12;
    private static final long STUCK_MS = 6_000;

    // Idle dwell between strolls, randomized for an organic feel (same 2-6s cadence as the bot FSM jitter).
    private static final long DWELL_MIN_MS = 2_500;
    private static final long DWELL_MAX_MS = 6_000;

    // Stagger the first stroll across a freshly-arrived crowd so they don't all step off on the same tick.
    private static final int START_JITTER_MS = 1_500;

    // Inset from the extreme left/right of the walkable terrain for the whole-map (shop/town) wander, so a
    // stroll target never lands on a foothold endpoint pressed against a wall, where the pather teeters
    // trying to step past the map edge ("walking off the map"). Per-side inset is
    // max(EDGE_MARGIN_MIN_PX, EDGE_MARGIN_PCT * walkableWidth): the % keeps roughly the middle 85% on wide
    // maps; the px floor keeps the inset meaningful on narrow shop maps where the % alone would be tiny.
    private static final double EDGE_MARGIN_PCT = 0.075;
    private static final int EDGE_MARGIN_MIN_PX = 30;

    private static final ScheduledExecutorService POOL = ExecutorServiceManager.getScheduledExecutorService();

    private static final Map<Integer, Wander> WANDERS = new ConcurrentHashMap<>();

    private static final byte WALKING = 0;
    private static final byte DWELLING = 1;

    private static final class Wander {
        final Character bot;
        final int mapId;          // bound to the map we started on; a map change ends the wander
        final boolean banded;     // true = loiter within [bandLo,bandHi] (near an NPC/object)
        final int bandLo;
        final int bandHi;

        ScheduledFuture<?> task;
        byte state = DWELLING;    // start dwelling-with-expired-timer so the first tick picks immediately
        long dwellUntilMs;        // 0 -> first tick strolls right away (clears the portal fast)
        Point target;
        int bestDist = Integer.MAX_VALUE;
        long progressAtMs;

        Wander(Character bot, int mapId, boolean banded, int bandLo, int bandHi) {
            this.bot = bot;
            this.mapId = mapId;
            this.banded = banded;
            this.bandLo = bandLo;
            this.bandHi = bandHi;
        }
    }

    // Whole-map town wander: stroll anywhere reachable on the bot's current map, but kept off the extreme
    // left/right edges (see startWholeMap) so a bot at the side doesn't teeter trying to walk off the map.
    public static void start(Character bot) {
        startWholeMap(bot);
    }

    // Loiter near a point: stroll only to legal spots whose X falls within radius of anchorX (e.g. near a
    // shop NPC or object). radius <= 0 falls back to whole-map. Note the band is X-only - vertically
    // stacked ledges in the band stay eligible; that's usually what "near the NPC" wants.
    public static void start(Character bot, int anchorX, int radius) {
        if (radius <= 0) {
            startWholeMap(bot);
        } else {
            startInternal(bot, true, anchorX - radius, anchorX + radius);
        }
    }

    // Whole-map wander with the extreme map edges trimmed off. We band the wander to [lo,hi] = the reachable
    // walkable X extent inset by EDGE_MARGIN per side, so strolls stay within roughly the middle of the map
    // instead of targeting a foothold endpoint against a wall. If the terrain or band can't be resolved
    // (nav graph not baked, or a ledge too narrow to trim), fall back to an untrimmed whole-map wander.
    private static void startWholeMap(Character bot) {
        if (bot == null || bot.getMap() == null) {
            startInternal(bot, false, 0, 0);
            return;
        }
        int[] band = trimmedBand(bot.getMap(), bot.getPosition());
        if (band == null) {
            startInternal(bot, false, 0, 0);
        } else {
            startInternal(bot, true, band[0], band[1]);
        }
    }

    // Reachable walkable X extent of the map, inset by EDGE_MARGIN per side. Mirrors BotSpotPicker's
    // reachability discipline: filter ledges to those reachable from `from`, but if the anchor resolves to
    // no reachable region (e.g. off a ledge), don't filter rather than come back empty. Returns {lo,hi}, or
    // null when there's no terrain or the span is too narrow to trim (caller falls back to the full span).
    private static int[] trimmedBand(MapleMap map, Point from) {
        List<GCMovement.Ledge> ledges = GCMovement.walkableLedges(map);
        if (ledges.isEmpty()) {
            return null;
        }
        Set<Integer> reachable = from != null ? GCMovement.reachableRegions(map, from.x, from.y) : Set.of();
        boolean filter = !reachable.isEmpty();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (GCMovement.Ledge l : ledges) {
            if (filter && !reachable.contains(l.regionId())) {
                continue;
            }
            min = Math.min(min, l.minX());
            max = Math.max(max, l.maxX());
        }
        if (max <= min) {
            return null;
        }
        int inset = Math.max(EDGE_MARGIN_MIN_PX, (int) (EDGE_MARGIN_PCT * (max - min)));
        int lo = min + inset;
        int hi = max - inset;
        return hi > lo ? new int[]{lo, hi} : null;
    }

    public static void stop(Character bot) {
        if (bot == null) {
            return;
        }
        Wander w = WANDERS.remove(bot.getId());
        if (w != null && w.task != null) {
            w.task.cancel(false);
        }
        // Leave the bot's current move intent alone: it finishes its last stroll and idles, which is
        // harmless. A caller stopping the wander to take over movement will issue its own move/travel.
    }

    public static boolean isWandering(Character bot) {
        return bot != null && WANDERS.containsKey(bot.getId());
    }

    private static void startInternal(Character bot, boolean banded, int bandLo, int bandHi) {
        if (bot == null || bot.getMap() == null) {
            return;
        }
        stop(bot);
        GCMovement.enable(bot);
        Wander w = new Wander(bot, bot.getMapId(), banded, bandLo, bandHi);
        w.progressAtMs = nowMs();
        long initialDelay = RANDOM.nextInt(START_JITTER_MS);
        w.task = POOL.scheduleAtFixedRate(() -> {
            try {
                tick(w);
            } catch (Throwable ignored) {
                // a thrown tick would cancel the periodic task; swallow and re-evaluate next poll
            }
        }, initialDelay, POLL_MS, TimeUnit.MILLISECONDS);
        WANDERS.put(bot.getId(), w);
    }

    private static void tick(Wander w) {
        Character bot = w.bot;
        if (bot == null) {
            return;
        }
        // Bail if this Wander was already stopped/replaced (cancel(false) doesn't interrupt an in-flight
        // tick) - prevents a late tick from re-issuing a move (and re-enabling GCMovement) after teardown.
        if (WANDERS.get(bot.getId()) != w) {
            return;
        }
        if (bot.getMap() == null || bot.getMapId() != w.mapId) {
            stop(bot); // gone, or left the map we were bound to - the caller owns any re-start
            return;
        }
        MapleMap map = bot.getMap();
        Point bp = bot.getPosition();
        long now = nowMs();

        if (w.state == DWELLING) {
            if (now < w.dwellUntilMs) {
                return; // still loitering
            }
            Point spot = pickStroll(map, bp, w);
            if (spot == null) {
                w.dwellUntilMs = now + DWELL_MIN_MS; // nothing reachable right now - idle and retry later
                return;
            }
            issueMove(w, spot, now);
            return;
        }

        // WALKING toward w.target
        int dx = Math.abs(bp.x - w.target.x);
        int dy = Math.abs(bp.y - w.target.y);
        if (dx <= ARRIVE_X && dy <= ARRIVE_Y && !GCMovement.isMoving(bot)) {
            w.state = DWELLING;
            w.dwellUntilMs = now + DWELL_MIN_MS + (long) (RANDOM.nextDouble() * (DWELL_MAX_MS - DWELL_MIN_MS));
            return;
        }

        int dist = dx + dy;
        if (dist < w.bestDist - PROGRESS_EPS) {
            w.bestDist = dist;
            w.progressAtMs = now;
        } else if (now - w.progressAtMs > STUCK_MS) {
            Point spot = pickStroll(map, bp, w); // give up on this target, try a fresh one
            if (spot != null) {
                issueMove(w, spot, now);
            } else {
                w.state = DWELLING;
                w.dwellUntilMs = now + DWELL_MIN_MS;
            }
            return;
        }

        if (!GCMovement.isMoving(bot)) {
            GCMovement.move(bot, w.target.x, w.target.y); // (re)issue the stroll
        }
    }

    // Pick a legal spot at least MIN_STROLL away in X, retrying a few times; on a tiny ledge where nothing
    // far enough exists, accept the last pick rather than freeze. Returns null only when the picker can't
    // produce any reachable point (caller idles and retries).
    private static Point pickStroll(MapleMap map, Point from, Wander w) {
        Point last = null;
        for (int i = 0; i < STROLL_ATTEMPTS; i++) {
            Point p = w.banded
                    ? BotSpotPicker.pickGroundSpot(map, from.x, from.y, w.bandLo, w.bandHi)
                    : BotSpotPicker.pickGroundSpot(map, from.x, from.y);
            if (p == null) {
                return last;
            }
            last = p;
            if (Math.abs(p.x - from.x) >= MIN_STROLL) {
                return p;
            }
        }
        return last;
    }

    private static void issueMove(Wander w, Point spot, long now) {
        w.target = spot;
        w.state = WALKING;
        w.bestDist = Integer.MAX_VALUE;
        w.progressAtMs = now;
        GCMovement.move(w.bot, spot.x, spot.y);
    }

    private static long nowMs() {
        return System.currentTimeMillis();
    }
}
