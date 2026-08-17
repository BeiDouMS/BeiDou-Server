package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

// WHICH map a training bot grinds: the whole selection cluster extracted from TrainingBot — the
// two-sided level band with its noob floor, chill visits, downward-only deep-hub floors, the
// level-scaled wander radius, the fit/distance/capacity weighting, and the world-wide occupancy
// registry with its slot-reservation race loop. TrainingBot's doDecide keeps only the FSM
// choreography (station-here, party targets, first-trip warp) and calls choose(). Ours (SoloMapling).
public final class TrainingMapChooser {

    // ── Admission band ──
    private static final int LEVEL_BAND = 12;                // full-weight comfort band around the bot's level
    // Two-sided admission: a map's mob level must be within [minMobFor(level), level + LEVEL_BAND]. The
    // lower edge is a CONSTANT look-down span, not a fraction, so a noob floors at mob-level 1: a level<=25
    // admits everything down to lvl-1 (snails right outside town — the lifeblood of the low-level areas)
    // AND up to level+12, while a level-80's floor sits at 55 so pros stay off trivial mobs. Levels are
    // only a small piece — near/far + crowd (distanceWeight, capacity) do the fine selection in the band.
    private static final int LOWER_SPAN = 25;
    // Below the comfort floor, a map's pick-weight decays with how far under it sits (belt-and-suspenders
    // with the admission band) — this only bites the fallback edge, where nothing was in-band at all.
    private static final int LEVEL_FIT_DECAY = 10;           // gap-decay scale (levels below comfort per e-fold)
    // Downward-only "pro" deep hubs (deep Ludibrium): the cohort should only ever migrate DEEPER (harder
    // mobs), never back up toward town. Enforced by tightening the admission floor to (level - this span)
    // — which the finder's FALLBACK now also respects — and by suppressing chill visits. See DeepHub.
    private static final int DOWNWARD_HUB_LOWER_SPAN = 10;

    // ── Chill visits (intentional, rare social variety) ──
    private static final double CHILL_VISIT_CHANCE = 0.06;
    private static final int CHILL_MIN_LEVEL = 30;           // below this there's nothing to "chill down" to
    private static final int CHILL_REACH = 3;                // chill visits stay near town (few hops out)
    private static final int CHILL_SPREAD = 25;              // how far below the comfort band a chill map may reach

    // ── Wander radius (hops from town) ──
    private static final int[][] HOPS_BY_LEVEL = {           // {level, hops}, linearly interpolated between points
            {1, 1}, {15, 2}, {30, 4}, {50, 8}, {70, 10}
    };
    private static final int ANYWHERE_LEVEL = 70;            // 70+ : open the radius to the whole landmass
    private static final int MAX_HOPS = 20;                  // "anywhere" radius; the walkable BFS stops at the coast anyway

    // ── Distance bias (fresh bots hug town, high bots venture to the edge of their reach) ──
    private static final int VENTURE_FULL_LEVEL = 50;        // level by which a bot fully prefers far maps
    private static final double DISTANCE_WEIGHT_BASE = 0.15; // floor so a non-preferred-distance map is still possible

    private static final int MAX_REDECIDES = 2;              // capacity-reservation re-rolls before accepting an over-cap map

    // How many training bots currently target each map (world-wide). DECIDE reserves a slot here
    // BEFORE travelling, so simultaneous deciders see each other and spread across maps.
    private static final Map<Integer, AtomicInteger> BOTS_PER_MAP = new ConcurrentHashMap<>();

    private TrainingMapChooser() {
    }

    // Pick a level-appropriate, uncrowded map for this bot and RESERVE an occupancy slot on it — the
    // returned pick's slot is HELD (the caller records it as its train target and releases it via
    // release() when done). weightedPick hard-caps maps at capacity, and the reservation loop closes
    // the cohort race: if our atomic increment pushed the map OVER capacity (another bot grabbed the
    // last slot at the same instant), release and re-pick — the bumped count now hard-zeros that map,
    // steering us deeper. Null = nothing reachable with mobs (caller idles in town and retries later).
    public static TrainingMap choose(Character chr, int homeMapId, Set<Integer> excluded, Consumer<String> debug) {
        int level = chr.getLevel();
        int reach = hopsForLevel(level);
        // Deep-hub grind: a cohort that spawns on a mob-bearing "deep hub" field may grind the hub
        // itself — the map BFS drops its own origin, so admit the current map explicitly when the bot
        // stands on its home hub and that hub has mobs.
        boolean includeOrigin = chr.getMapId() == homeMapId && MapMobIndex.level(homeMapId) >= 0;
        DeepHub.Info hub = DeepHub.of(homeMapId);
        boolean downwardOnly = hub != null && hub.downwardOnly();
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        // Normal trip: the two-sided band. Rarely, a mid/high bot takes a "chill" trip to an easy
        // NEAR-TOWN map — a strictly below-comfort band capped near town; nothing to chill at -> retry
        // as a normal trip. Downward-only pros never chill (a chill is an up-trip by construction).
        boolean chill = !downwardOnly && level >= CHILL_MIN_LEVEL && rng.nextDouble() < CHILL_VISIT_CHANCE;
        int minMob = chill ? Math.max(1, level - LEVEL_BAND - 1 - CHILL_SPREAD) : minMobFor(level);
        int maxMob = chill ? Math.max(1, level - LEVEL_BAND - 1) : level + LEVEL_BAND;
        int useReach = chill ? Math.min(reach, CHILL_REACH) : reach;
        int hardMinMob = 1;
        if (downwardOnly) {
            minMob = Math.max(minMob, level - DOWNWARD_HUB_LOWER_SPAN);
            hardMinMob = minMob; // the finder's fallback respects this too — a pro is never offered an easy up-map
        }

        for (int attempt = 0; attempt <= MAX_REDECIDES; attempt++) {
            List<TrainingMap> eligible = TrainingMapFinder.findTrainingMaps(
                    chr.getMapId(), level, minMob, maxMob, useReach, excluded, includeOrigin, hardMinMob);
            if (eligible.isEmpty() && chill) {
                chill = false;
                minMob = minMobFor(level);
                maxMob = level + LEVEL_BAND;
                useReach = reach;
                eligible = TrainingMapFinder.findTrainingMaps(
                        chr.getMapId(), level, minMob, maxMob, useReach, excluded, includeOrigin, hardMinMob);
            }
            if (eligible.isEmpty()) {
                return null; // nothing reachable with mobs (or all on cooldown)
            }
            TrainingMap candidate = weightedPick(eligible, level, useReach);
            reserve(candidate.mapId());
            if (botsOnMap(candidate.mapId()) <= mapCapacity(candidate.mapId()) || attempt == MAX_REDECIDES) {
                return candidate; // within capacity, or out of re-rolls -> accept (saturated region: share)
            }
            release(candidate.mapId());
            debug.accept("DECIDE: map " + candidate.mapId() + " over cap, re-picking");
        }
        return null; // not reached — the loop always returns on its last attempt
    }

    // ── Occupancy registry ──

    public static void reserve(int mapId) {
        BOTS_PER_MAP.computeIfAbsent(mapId, k -> new AtomicInteger()).incrementAndGet();
    }

    public static void release(int mapId) {
        AtomicInteger c = BOTS_PER_MAP.get(mapId);
        if (c != null) {
            c.decrementAndGet();
        }
    }

    public static int botsOnMap(int mapId) {
        AtomicInteger c = BOTS_PER_MAP.get(mapId);
        return c == null ? 0 : Math.max(0, c.get());
    }

    // Read-only occupancy peek for the !env grindprofile dump (watch distribution live).
    public static int botsTargeting(int mapId) {
        return botsOnMap(mapId);
    }

    // A map's bot carrying capacity = its claimable-spot count (or the span quota on a ROAM map).
    // SpotFinder prefers the exact live profile when the map has one, else a cached cluster-count
    // estimate over the static WZ spawn positions — no live map load, no nav bake.
    public static int mapCapacity(int mapId) {
        return SpotFinder.mapBotCapacity(mapId);
    }

    // ── Selection math ──

    // Wander radius (hops from town) for a level, interpolating HOPS_BY_LEVEL between its breakpoints.
    // ANYWHERE_LEVEL and up opens the radius to MAX_HOPS (the whole connected landmass). Low bots hug
    // town, 30+ bots range widely (Victoria's good maps are far out), 70+ can go essentially anywhere.
    static int hopsForLevel(int level) {
        if (level >= ANYWHERE_LEVEL) {
            return MAX_HOPS;
        }
        int[][] bp = HOPS_BY_LEVEL;
        if (level <= bp[0][0]) {
            return bp[0][1];
        }
        for (int i = 1; i < bp.length; i++) {
            if (level <= bp[i][0]) {
                int loL = bp[i - 1][0], hiL = bp[i][0];
                int loH = bp[i - 1][1], hiH = bp[i][1];
                return (int) Math.round(loH + (hiH - loH) * (double) (level - loL) / (hiL - loL));
            }
        }
        return MAX_HOPS; // above the top breakpoint but below ANYWHERE_LEVEL — safety only
    }

    // Weighted random over eligible maps: level-appropriate maps full weight, easier maps a reduced chance,
    // biased by distance (low bots near town, high bots further out), and CAPACITY-AWARE — a map at/over
    // its carrying capacity is hard-zeroed so it drops out of contention and selection spills to deeper,
    // less-crowded maps; the rest are weighted by remaining headroom. If every reachable map is full,
    // falls back to a soft divisor so the bot still goes somewhere (and shares) rather than idling.
    private static TrainingMap weightedPick(List<TrainingMap> maps, int level, int reach) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double[] weights = new double[maps.size()];
        double total = 0;
        for (int i = 0; i < maps.size(); i++) {
            TrainingMap m = maps.get(i);
            int cap = mapCapacity(m.mapId());
            int n = botsOnMap(m.mapId());
            double base = levelFitWeight(level, m.mobLevel()) * distanceWeight(level, m.hops(), reach);
            double w = (n >= cap) ? 0.0 : base * ((cap - n) / (double) cap); // hard cap + headroom weight
            weights[i] = w;
            total += w;
        }
        if (total <= 0) {
            // Every reachable map is at capacity — re-weight with the soft divisor so the surplus still
            // spreads onto the least-crowded map instead of idling in town.
            for (int i = 0; i < maps.size(); i++) {
                TrainingMap m = maps.get(i);
                double w = levelFitWeight(level, m.mobLevel())
                        * distanceWeight(level, m.hops(), reach)
                        / (1.0 + botsOnMap(m.mapId()));
                weights[i] = w;
                total += w;
            }
        }
        if (total <= 0) {
            return maps.get(rng.nextInt(maps.size()));
        }
        double r = rng.nextDouble() * total;
        for (int i = 0; i < maps.size(); i++) {
            r -= weights[i];
            if (r <= 0) {
                return maps.get(i);
            }
        }
        return maps.get(maps.size() - 1);
    }

    // Full weight within (or above) the comfort band; below it, the weight decays with the gap so
    // far-below maps fade out smoothly instead of holding a flat "chill" weight.
    private static double levelFitWeight(int level, int mobLevel) {
        int comfortLow = minMobFor(level); // full weight across the whole admitted band (noobs love low maps)
        if (mobLevel >= comfortLow) {
            return 1.0;
        }
        int gap = comfortLow - mobLevel; // only bites the fallback edge (below the admitted floor)
        return Math.exp(-gap / (double) LEVEL_FIT_DECAY);
    }

    // The lowest mob level a bot will normally grind: floors at 1 for noobs (level <= LOWER_SPAN + 1) so
    // the maps right outside town stay populated, and sits LOWER_SPAN below level for higher bots.
    private static int minMobFor(int level) {
        return Math.max(1, level - LOWER_SPAN);
    }

    // Distance preference, ramped by level. reach = the bot's hop radius; hops = this map's BFS distance
    // from the spawn town (1 = adjacent). A fresh bot favors close maps; the preference slides toward the
    // far edge of its reach as it approaches VENTURE_FULL_LEVEL. With only adjacent maps reachable
    // there's nothing to bias, so weight is flat.
    private static double distanceWeight(int level, int hops, int reach) {
        if (reach <= 1) {
            return 1.0;
        }
        double venture = Math.min(1.0, level / (double) VENTURE_FULL_LEVEL);
        double hopFrac = (hops - 1) / (double) (reach - 1); // 0 = nearest, 1 = edge of reach
        hopFrac = Math.max(0.0, Math.min(1.0, hopFrac));
        double pref = (1.0 - venture) * (1.0 - hopFrac) + venture * hopFrac;
        return DISTANCE_WEIGHT_BASE + pref;
    }
}
