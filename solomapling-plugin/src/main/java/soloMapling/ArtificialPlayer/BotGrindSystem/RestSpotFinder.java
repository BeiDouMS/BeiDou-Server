package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;
import org.gms.server.life.Monster;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Portal;
import org.gms.server.maps.Rope;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Picks where a grinding bot takes its break. Safety is NON-NEGOTIABLE, then cost decides among the safe
// options. Safety is a LEDGE THREAT MODEL, not a raw distance:
//   1. A ledge that BEARS a mob spawn is HOT and can never host a rest spot — mobs patrol their whole
//      foothold chain, so the far tip of a spawn floor is walked over too (the old distance gate let that
//      tip win, usually right at the map edge). Hard reject.
//   2. For spawn-free ledges, clearance is AXIS-AWARE: a sit point is threatened by a mob patrolling a
//      spawn ledge only within the mob's horizontal reach, and vertical separation is worth far more
//      (mobs don't climb — sitting a tier ABOVE a spawn is safe; the same gap sideways on the spawn floor
//      is not). A spawn-free ledge whose best point can't clear the map's inflation radius is HOT too
//      (adjacency) — this catches the low mini-shelf tucked under a spawn floor.
//   3. Among safe candidates, rank by clearance minus a travel penalty (walking cheap, rope-climbing not)
//      and sit at an INTERIOR point (clamped off the ledge lip), never the extremity.
// A rope hang is an optional rest flavor: findRope() offers a safe mid-rope band above the mobs (the
// caller gates it by chance and falls back to a ground spot).
// Observation gates how far it will travel for that safety: an observed bot will climb across tiers to a
// safe ledge (a watching player sees it head off to rest); an unobserved bot only rests if a safe spot is
// close (nobody's watching, so a long invisible climb is wasted — it just skips the break instead).
//
// find() returns null ONLY on the unobserved-no-nearby-safe-spot path, which the caller reads as "skip
// this break". find() when observed and findLocal() always return a spot (worst case a safe portal's
// ground). findRope() returns null when no safe rope band exists (chair fallback).
public class RestSpotFinder {

    public enum Kind {GROUND, ROPE}

    // A resolved rest spot: a ground point to stand/sit on, or a mid-rope hang point (with the rope).
    public record RestSpot(Point point, Kind kind, Rope rope) {
        static RestSpot ground(Point p) {
            return new RestSpot(p, Kind.GROUND, null);
        }
    }

    // Fallback inflation radius (px) — used only when a map yields no mob ids at all (WZ nor live). Real
    // maps derive their inflation from mob hitboxes (see inflationRadius).
    private static final int DEFAULT_INFLATION_PX = 240;

    // Per-map inflation = max(maxMobReach * HITBOX_K, INFLATION_FLOOR_PX). K scales a mob's body reach up
    // into a real safety margin (patrol + knockback slack the hitbox alone doesn't cover); the floor keeps
    // breathing room on tiny-mob maps. Calibrated so big-golem maps land >= the old hand-tuned 240px — the
    // !bot restspot dump prints the computed value, so tune K/floor in-game. Cached per map (WZ-derived, so
    // stable).
    private static final double HITBOX_K = 2.5;
    private static final int INFLATION_FLOOR_PX = 140;
    private static final Map<Integer, Integer> INFLATION_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    private static final int SPOT_SAMPLES = 12;           // max samples across a ledge when hunting its safest x
    private static final int EDGE_MARGIN = 40;            // keep the sit point this far off a ledge's lip
    private static final int ROPE_END_MARGIN = 24;        // keep the hang this far off a rope's very top/bottom

    // Vertical separation above a spawn is worth this many px of horizontal separation in the safety metric
    // (mobs don't climb, so height is the strongest protection). Only counts when the point is ABOVE the
    // threat — being below/level gets no vertical credit.
    private static final double SAFE_VERTICAL_WEIGHT = 3.0;

    // Live-mob veto: at commit time, reject a sit point with a hostile mob this close (inflation-scaled).
    private static final double LIVE_VETO_SCALE = 1.0;

    // Travel cost (a proxy: horizontal walk + heavy vertical for the rope climbs between tiers)
    private static final double VERTICAL_WEIGHT = 3.0;    // 1px of climb "costs" this many px of walk
    private static final int OBS_MAX_CLIMB_PX = 1800;     // observed: climb the whole map if that's where the safe ledge is
    private static final int UNOBS_MAX_CLIMB_PX = 450;    // unobserved: current tier (+ an adjacent shelf) only, else skip
    private static final int LOCAL_MAX_CLIMB_PX = 220;    // timeout fallback: walk-only, stay on this ledge/tier
    private static final double OBS_TRAVEL_WEIGHT = 0.20; // observed: safety dominates, travel is a tie-break
    private static final double UNOBS_TRAVEL_WEIGHT = 1.0;// unobserved: strongly prefer the nearest safe spot

    // A spawn threat: the x-span a mob patrols (a whole spawn ledge, or a single point when the spawn isn't
    // on a known ledge) and the ledge y it stands at.
    private record Threat(int spanMinX, int spanMaxX, int y) {}

    // The map's spawn/threat model, built once and shared by ledge ranking, rope ranking, and the portal
    // fallback: which ledges bear spawns (hard-reject), the patrol-span threats for the clearance metric,
    // the inflation radius in force, and the ledge lookup by region id.
    private record ThreatContext(Set<Integer> spawnRegions, List<Threat> threats, int inflation,
                                 Map<Integer, GCMovement.Ledge> ledgeByRegion) {}

    private record Ranked(GCMovement.Ledge ledge, Point spot, double clearance, boolean hot, String hotReason,
                          double score) {}

    private record SafePoint(Point point, double clearance) {}

    // Main entry. null => no safe spot worth traveling to right now; the caller skips the break and keeps
    // grinding (only happens unobserved — an observed bot always gets a safe spot, trekking if it must).
    public static RestSpot find(Character chr) {
        boolean observed = chr.getMap() != null && GCMovement.isMapObserved(chr.getMapId());
        if (observed) {
            Point s = bestSafe(chr, OBS_MAX_CLIMB_PX, OBS_TRAVEL_WEIGHT, true);
            if (s == null) {
                s = bestSafe(chr, Integer.MAX_VALUE, OBS_TRAVEL_WEIGHT, true); // very tall map: reach the safe ledge anyway
            }
            return RestSpot.ground(s != null ? s : portalFallback(chr)); // last resort: the safest reachable portal
        }
        Point s = bestSafe(chr, UNOBS_MAX_CLIMB_PX, UNOBS_TRAVEL_WEIGHT, false);
        return s != null ? RestSpot.ground(s) : null; // may be null -> skip the break
    }

    // Timeout fallback: a safe GROUND spot reachable from HERE without a real climb, so a stalled trek
    // re-targets to something it can walk to instead of sitting among the mobs. Never null (the break
    // already committed).
    public static RestSpot findLocal(Character chr) {
        boolean observed = chr.getMap() != null && GCMovement.isMapObserved(chr.getMapId());
        Point s = bestSafe(chr, LOCAL_MAX_CLIMB_PX, UNOBS_TRAVEL_WEIGHT, observed);
        return RestSpot.ground(s != null ? s : portalFallback(chr));
    }

    // A safe mid-rope hang, or null if the map has no rope with a safe band reachable from here. Observed
    // rests only (a rope hang is a visible flourish; no point on a map nobody can see). The band sits above
    // a hot bottom ledge by the inflation radius (a mob standing there can't reach up to it) and off both
    // rope ends by a margin; the mid-band point is the hang target.
    public static RestSpot findRope(Character chr) {
        MapleMap map = chr != null ? chr.getMap() : null;
        Point cur = chr != null ? chr.getPosition() : null;
        if (map == null || cur == null || !GCMovement.isMapObserved(chr.getMapId())) {
            return null;
        }
        Set<Integer> reachable = GCMovement.reachableRegions(map, cur.x, cur.y);
        ThreatContext ctx = buildContext(map);
        Rope bestRope = null;
        Point bestHang = null;
        double bestScore = -Double.MAX_VALUE;
        for (Rope rope : map.getRopes()) {
            int topY = rope.topY();
            int bottomY = rope.bottomY();
            Point bg = GCMovement.groundPointBelow(map, rope.x(), bottomY);
            int bottomRegion = bg != null ? GCMovement.regionIdAt(map, bg.x, bg.y) : -1;
            Point tg = GCMovement.groundPointBelow(map, rope.x(), topY - 5);
            int topRegion = tg != null ? GCMovement.regionIdAt(map, tg.x, tg.y) : -1;
            if (!inReach(reachable, bottomRegion) && !inReach(reachable, topRegion)) {
                continue; // can't climb onto this rope from here
            }
            boolean bottomHot = isLedgeHot(ctx, bottomRegion);
            int highBound = topY + ROPE_END_MARGIN;                                  // min y (highest hang)
            int lowBound = bottomY - (bottomHot ? ctx.inflation() : ROPE_END_MARGIN); // max y (lowest hang)
            if (highBound > lowBound) {
                continue; // no safe band (both ends hot + short rope, etc.) -> chair fallback
            }
            int hangY = (highBound + lowBound) / 2;
            int climb = Math.abs(hangY - cur.y);
            if (climb > OBS_MAX_CLIMB_PX) {
                continue;
            }
            Point hang = new Point(rope.x(), hangY);
            double clearance = clearance(hang, ctx.threats());
            double travelCost = Math.abs(rope.x() - cur.x) + climb * VERTICAL_WEIGHT;
            double score = clearance - OBS_TRAVEL_WEIGHT * travelCost;
            if (score > bestScore) {
                bestScore = score;
                bestRope = rope;
                bestHang = hang;
            }
        }
        return bestRope == null ? null : new RestSpot(bestHang, Kind.ROPE, bestRope);
    }

    private static boolean inReach(Set<Integer> reachable, int regionId) {
        return regionId >= 0 && (reachable.isEmpty() || reachable.contains(regionId));
    }

    // A rope-end ledge is hot if it bears a spawn or its own interior can't clear the inflation radius
    // (adjacency). No ledge there (rope ends in air / off-graph) => nothing stands there => not hot.
    private static boolean isLedgeHot(ThreatContext ctx, int regionId) {
        if (regionId < 0) {
            return false;
        }
        if (ctx.spawnRegions().contains(regionId)) {
            return true;
        }
        GCMovement.Ledge l = ctx.ledgeByRegion().get(regionId);
        if (l == null) {
            return false; // a rope region, not a ledge — treat as open air
        }
        return clearance(new Point(l.centerX(), l.centerY()), ctx.threats()) < ctx.inflation();
    }

    // True when a hostile mob currently sits within the inflation radius of the spot — a last-moment veto so
    // a chair never lands on a mob that wandered in after selection. Observed maps only (unobserved mobs are
    // static/irrelevant, and packets — what actually shows — are gated on observation).
    public static boolean isSpotMobOccupied(Character chr, Point spot) {
        MapleMap map = chr != null ? chr.getMap() : null;
        if (map == null || spot == null || !GCMovement.isMapObserved(chr.getMapId())) {
            return false;
        }
        double veto = inflationRadius(map) * LIVE_VETO_SCALE;
        return mobWithin(map, spot, veto);
    }

    private static boolean mobWithin(MapleMap map, Point spot, double radius) {
        double r2 = radius * radius;
        for (Monster m : map.getAllMonsters()) {
            if (!SpotFinder.isHostile(m) || m.getPosition() == null) {
                continue;
            }
            if (m.getPosition().distanceSq(spot) <= r2) {
                return true;
            }
        }
        return false;
    }

    private static Point bestSafe(Character chr, int maxClimbPx, double travelWeight, boolean observed) {
        List<Ranked> ranked = rankSafe(chr, maxClimbPx, travelWeight);
        if (ranked.isEmpty()) {
            return null;
        }
        if (!observed) {
            return ranked.get(0).spot(); // no live-mob veto unobserved (mobs static, nothing rendered)
        }
        MapleMap map = chr.getMap();
        double veto = inflationRadius(map) * LIVE_VETO_SCALE;
        for (Ranked r : ranked) {
            if (!mobWithin(map, r.spot(), veto)) {
                return r.spot(); // first safe spot with no mob currently on it
            }
        }
        return ranked.get(0).spot(); // every safe spot momentarily occupied — take the best anyway
    }

    // GM debug (!bot restspot): EVERY reachable ledge in the observed budget with its axis-aware clearance,
    // hot classification (+ reason), the inflation radius in force, and the edge-clamped sit point — so the
    // dangerous ledges being rejected are visible and the inflation/margins can be calibrated in-game.
    public static List<String> debug(Character chr) {
        List<String> out = new ArrayList<>();
        MapleMap map = chr.getMap();
        Point cur = chr.getPosition();
        if (map == null || cur == null) {
            out.add("restspot: no map / position");
            return out;
        }
        boolean observed = GCMovement.isMapObserved(chr.getMapId());
        int budget = observed ? OBS_MAX_CLIMB_PX : UNOBS_MAX_CLIMB_PX;
        double tw = observed ? OBS_TRAVEL_WEIGHT : UNOBS_TRAVEL_WEIGHT;
        int inflation = inflationRadius(map);
        Set<Integer> reachable = GCMovement.reachableRegions(map, cur.x, cur.y);
        List<Ranked> all = rankAll(chr, budget, tw);
        RestSpot rope = observed ? findRope(chr) : null;
        out.add(String.format("restspot @%d pos(%d,%d) observed=%s climbBudget=%d inflation=%dpx ledges=%d reachable=%d spawns=%d safe=%d rope=%s",
                chr.getMapId(), cur.x, cur.y, observed, budget, inflation,
                GCMovement.walkableLedges(map).size(), reachable.size(), map.getMonsterSpawnPositions().size(),
                (int) all.stream().filter(r -> !r.hot()).count(),
                rope == null ? "none" : ("x" + rope.point().x + " y" + rope.point().y)));
        if (all.isEmpty()) {
            out.add("  (no reachable ledge in budget -> " + (observed ? "portal fallback" : "SKIP break") + ")");
        }
        int n = 0;
        for (Ranked r : all) {
            if (n++ >= 12) {
                out.add("  ...");
                break;
            }
            out.add(String.format("  #%d region%d spot(%d,%d) clear=%.0f %s score=%.0f",
                    n, r.ledge().regionId(), r.spot().x, r.spot().y, r.clearance(),
                    r.hot() ? "HOT(" + r.hotReason() + ")" : "SAFE", r.score()));
        }
        return out;
    }

    // Reachable ledges within the climb budget that are not hot, best score first.
    private static List<Ranked> rankSafe(Character chr, int maxClimbPx, double travelWeight) {
        List<Ranked> out = new ArrayList<>();
        for (Ranked r : rankAll(chr, maxClimbPx, travelWeight)) {
            if (!r.hot()) {
                out.add(r);
            }
        }
        return out;
    }

    // Every reachable ledge within the climb budget, scored and classified hot/safe, best score first. The
    // safety filter is applied by rankSafe; debug shows the unfiltered list so rejects are visible.
    private static List<Ranked> rankAll(Character chr, int maxClimbPx, double travelWeight) {
        List<Ranked> out = new ArrayList<>();
        MapleMap map = chr.getMap();
        Point cur = chr.getPosition();
        if (map == null || cur == null) {
            return out;
        }
        ThreatContext ctx = buildContext(map);
        Set<Integer> reachable = GCMovement.reachableRegions(map, cur.x, cur.y);
        for (GCMovement.Ledge ledge : ctx.ledgeByRegion().values()) {
            if (!reachable.isEmpty() && !reachable.contains(ledge.regionId())) {
                continue; // can't walk/climb there from here
            }
            int climb = Math.abs(ledge.centerY() - cur.y);
            if (climb > maxClimbPx) {
                continue; // beyond this break's climb budget (observation-tiered)
            }
            SafePoint sp = safestPoint(map, ledge, ctx.threats());
            boolean spawnBearing = ctx.spawnRegions().contains(ledge.regionId());
            boolean hot = spawnBearing || sp.clearance() < ctx.inflation();
            String reason = spawnBearing ? "spawn" : (hot ? "adjacent" : "");
            double travelCost = Math.abs(sp.point().x - cur.x) + climb * VERTICAL_WEIGHT;
            double score = sp.clearance() - travelWeight * travelCost;
            out.add(new Ranked(ledge, sp.point(), sp.clearance(), hot, reason, score));
        }
        out.sort((a, b) -> Double.compare(b.score(), a.score()));
        return out;
    }

    // Build the map's spawn/threat model: the ledges bearing spawns (HOT, hard reject) + the patrol-span
    // threats used for axis-aware clearance. A spawn snapped onto a known ledge threatens that ledge's whole
    // span; a spawn on no known ledge (rope/off-graph) degrades to a point threat.
    private static ThreatContext buildContext(MapleMap map) {
        Map<Integer, GCMovement.Ledge> ledgeByRegion = new HashMap<>();
        for (GCMovement.Ledge l : GCMovement.walkableLedges(map)) {
            ledgeByRegion.put(l.regionId(), l);
        }
        Set<Integer> spawnRegions = new HashSet<>();
        List<Threat> threats = new ArrayList<>();
        Set<Integer> ledgeThreatDone = new HashSet<>();
        for (Point p : map.getMonsterSpawnPositions()) {
            Point g = GCMovement.groundPointBelow(map, p.x, p.y);
            Point a = g != null ? g : p;
            int rid = GCMovement.regionIdAt(map, a.x, a.y);
            GCMovement.Ledge l = rid >= 0 ? ledgeByRegion.get(rid) : null;
            if (l != null) {
                spawnRegions.add(rid);
                if (ledgeThreatDone.add(rid)) {
                    threats.add(new Threat(l.minX(), l.maxX(), l.centerY()));
                }
            } else {
                threats.add(new Threat(a.x, a.x, a.y));
            }
        }
        return new ThreatContext(spawnRegions, threats, inflationRadius(map), ledgeByRegion);
    }

    // The interior sit point on a ledge that maximizes axis-aware clearance to the nearest spawn threat.
    // Samples across the ledge's interior span (clamped off both lips and inside the map's VR), and on a
    // clearance plateau prefers the point nearest the ledge centroid so chairs sit in the middle of a safe
    // floor, not at its extremity. Falls back to the (clamped) ledge center if the graph can't place x.
    private static SafePoint safestPoint(MapleMap map, GCMovement.Ledge ledge, List<Threat> threats) {
        Rectangle vr = map.getMapArea();
        int lo = ledge.minX() + EDGE_MARGIN;
        int hi = ledge.maxX() - EDGE_MARGIN;
        if (vr != null && vr.width > 0) {
            lo = Math.max(lo, vr.x + EDGE_MARGIN);
            hi = Math.min(hi, vr.x + vr.width - EDGE_MARGIN);
        }
        if (lo > hi) {
            lo = hi = ledge.centerX(); // ledge shorter than the margins — sit dead center
        }
        int span = Math.max(0, hi - lo);
        int steps = span <= 0 ? 0 : Math.min(SPOT_SAMPLES, Math.max(2, span / 24));
        int centroid = (lo + hi) / 2;
        Point bestPt = null;
        double bestClear = -1;
        int bestCentroidDist = Integer.MAX_VALUE;
        for (int i = 0; i <= steps; i++) {
            int x = steps == 0 ? lo : lo + (int) ((long) span * i / steps);
            Point gp = GCMovement.groundPointInRegion(map, ledge.regionId(), x);
            if (gp == null) {
                continue;
            }
            double clear = clearance(gp, threats);
            int cd = Math.abs(gp.x - centroid);
            // Best clearance wins; on a plateau (near-equal clearance) the more interior point wins.
            if (clear > bestClear + 1 || (Math.abs(clear - bestClear) <= 1 && cd < bestCentroidDist)) {
                bestClear = clear;
                bestPt = gp;
                bestCentroidDist = cd;
            }
        }
        if (bestPt == null) {
            Point c = GCMovement.groundPointInRegion(map, ledge.regionId(), centroid);
            bestPt = c != null ? c : new Point(ledge.centerX(), ledge.centerY());
            bestClear = clearance(bestPt, threats);
        }
        return new SafePoint(bestPt, bestClear);
    }

    // Axis-aware clearance of a point to the nearest spawn threat: horizontal gap to the patrol span plus a
    // heavily-weighted bonus for sitting ABOVE the threat (mobs can't climb up to it). Being level with or
    // below a threat gets no vertical credit, so a mini-shelf directly under a spawn floor scores ~0.
    private static double clearance(Point p, List<Threat> threats) {
        if (threats.isEmpty()) {
            return 100_000; // no mobs on this map -> everywhere is "safe"
        }
        double best = Double.MAX_VALUE;
        for (Threat t : threats) {
            double gx = p.x < t.spanMinX() ? t.spanMinX() - p.x
                    : p.x > t.spanMaxX() ? p.x - t.spanMaxX() : 0;
            double above = Math.max(0, t.y() - p.y); // p.y smaller = higher on screen = above the threat
            best = Math.min(best, gx + above * SAFE_VERTICAL_WEIGHT);
        }
        return best;
    }

    // The safest reachable portal's ground — the reliable "somewhere safe" answer when no ledge clears the
    // inflation floor. Each portal's ground point is run through the same threat model (a portal is NOT
    // assumed spawn-free — some maps spawn right on the entrance); pick the highest-clearance one, nearest as
    // the tie-break. Falls back to in-place if portal-less.
    private static Point portalFallback(Character chr) {
        MapleMap map = chr.getMap();
        Point cur = chr.getPosition();
        Point here = cur != null ? new Point(cur) : new Point(0, 0);
        if (map == null || cur == null) {
            return here;
        }
        List<Threat> threats = buildContext(map).threats();
        Point best = null;
        double bestClear = -1;
        double bestDistSq = Double.MAX_VALUE;
        for (Portal pt : map.getPortals()) {
            Point pp = pt.getPosition();
            if (pp == null) {
                continue;
            }
            Point ground = GCMovement.groundPointBelow(map, pp.x, pp.y);
            Point g = ground != null ? ground : pp;
            double clear = clearance(g, threats);
            double dsq = cur.distanceSq(g);
            if (clear > bestClear + 1 || (Math.abs(clear - bestClear) <= 1 && dsq < bestDistSq)) {
                best = g;
                bestClear = clear;
                bestDistSq = dsq;
            }
        }
        return best != null ? best : here;
    }

    // The threat reach of the map's mobs (px): the widest mob body on the map, scaled and floored. Mob ids
    // come from static WZ map data (MapMobIndex), falling back to whatever is live if the map has none, and
    // finally to the hand-tuned constant. Cached per map.
    private static int inflationRadius(MapleMap map) {
        return INFLATION_CACHE.computeIfAbsent(map.getId(), id -> {
            int maxReach = 0;
            for (int mobId : MapMobIndex.mobIds(id)) {
                maxReach = Math.max(maxReach, MobHitboxIndex.reach(mobId));
            }
            if (maxReach == 0) {
                for (Monster m : map.getAllMonsters()) {
                    maxReach = Math.max(maxReach, MobHitboxIndex.reach(m.getId()));
                }
            }
            if (maxReach == 0) {
                return DEFAULT_INFLATION_PX; // no ids anywhere — keep the hand-tuned constant
            }
            return Math.max((int) Math.round(maxReach * HITBOX_K), INFLATION_FLOOR_PX);
        });
    }
}
