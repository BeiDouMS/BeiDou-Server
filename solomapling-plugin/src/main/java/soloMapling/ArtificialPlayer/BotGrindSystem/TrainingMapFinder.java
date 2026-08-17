package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.server.maps.MiniDungeonInfo;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Deterministic training-map discovery: BFS the nearby walkable maps from the bot's map, keep the
// ones whose representative mob level is inside the caller's two-sided band [minMob, maxMob], else the
// closest-level mob-bearing map. Reads only WZ (via GCMovement.mapsWithinHops + MapMobIndex); the
// walkable-portal BFS naturally excludes towns (no mobs) and PQ/instance maps (not portal-reachable).
// Mini-dungeon instances are explicitly rejected (MiniDungeonInfo.isDungeonMap), and maps outside the
// allowed original-content regions are rejected (TrainingRegions) so a bot never grinds/warps into a
// single-person instance or into Leafre / Aqua Road / new-school content even if a portal reaches it.
//
// Our own creation. Replaces TrainingBot's old REGIONS table.
public final class TrainingMapFinder {

    private TrainingMapFinder() {
    }

    private static final int MIN_MOB_COUNT = 2; // skip one-off quest-mob maps
    // When nothing is in-band, return this many closest-level maps (not one): a single-map fallback made
    // every out-of-band bot in a cohort converge on the identical map (single candidates skip the
    // weighted pick entirely), guaranteeing crowding on exactly the maps with the least to offer.
    private static final int FALLBACK_K = 3;

    // Reachable, region-allowed, mob-bearing maps whose median mob level sits in the two-sided band
    // [minMob, maxMob] (the caller bands both ends so a high bot won't admit trivial far-out maps).
    // `level` is used only for the fallback: when nothing is in-band, return the closest-level allowed
    // maps so the bot always has somewhere to go. Maps in `excluded` (e.g. one the caller just left
    // because it was overcrowded) are skipped, including for the fallback.
    public static List<TrainingMap> findTrainingMaps(int fromMapId, int level, int minMob, int maxMob,
                                                     int maxHops, Set<Integer> excluded) {
        return findTrainingMaps(fromMapId, level, minMob, maxMob, maxHops, excluded, false, 1);
    }

    // As above, but with includeOrigin: the BFS uses fromMapId as its search root and never emits it, so a
    // cohort that spawns on a mob-bearing field — a "deep hub" like Sharp Cliff I or Ant Tunnel Park — could
    // only ever transit through its own map, never grind it. includeOrigin folds fromMapId back in as a
    // hops=0 candidate, subject to the identical filters (region, mini-dungeon, excluded, mob count, band).
    public static List<TrainingMap> findTrainingMaps(int fromMapId, int level, int minMob, int maxMob,
                                                     int maxHops, Set<Integer> excluded, boolean includeOrigin) {
        return findTrainingMaps(fromMapId, level, minMob, maxMob, maxHops, excluded, includeOrigin, 1);
    }

    // hardMinMob is an absolute floor the FALLBACK also respects: a downward-only deep-hub pro must never
    // be offered an easy up-map even when nothing is in-band (the band's minMob only gates eligibility;
    // the old single-map fallback ignored it). 1 = no hard floor.
    public static List<TrainingMap> findTrainingMaps(int fromMapId, int level, int minMob, int maxMob,
                                                     int maxHops, Set<Integer> excluded, boolean includeOrigin,
                                                     int hardMinMob) {
        Map<Integer, Integer> nearby = GCMovement.mapsWithinHopsByDepth(fromMapId, maxHops);
        if (includeOrigin) {
            nearby.put(fromMapId, 0); // hops=0; the loop below applies the same admission filters to it
        }
        List<TrainingMap> eligible = new ArrayList<>();
        List<TrainingMap> outOfBand = new ArrayList<>(); // fallback candidates (>= hardMinMob), ranked below
        for (Map.Entry<Integer, Integer> e : nearby.entrySet()) {
            int mapId = e.getKey();
            int hops = e.getValue();
            if (excluded != null && excluded.contains(mapId)) {
                continue; // caller is steering away from this map (crowd cooldown)
            }
            if (MiniDungeonInfo.isDungeonMap(mapId)) {
                continue; // single-person mini-dungeon instance — bots must never grind/warp here
            }
            if (!TrainingRegions.isAllowed(mapId)) {
                continue; // outside original-content regions (Leafre / Aqua Road / new-school etc.)
            }
            MapMobIndex.MapMobInfo info = MapMobIndex.info(mapId);
            int lvl = info.medianLevel();
            if (lvl < 1 || info.mobCount() < MIN_MOB_COUNT) {
                continue; // no mobs (town) or too few to be a grind map
            }
            if (lvl >= minMob && lvl <= maxMob) {
                eligible.add(new TrainingMap(mapId, lvl, hops));
            } else if (lvl >= hardMinMob) {
                outOfBand.add(new TrainingMap(mapId, lvl, hops));
            }
        }
        if (!eligible.isEmpty()) {
            return eligible;
        }
        // Nothing in-band: the K closest-level allowed maps, so the caller's weighted pick still has
        // options to spread over (the level-fit gap-decay then fades the worst of them).
        outOfBand.sort((a, b) -> Integer.compare(Math.abs(a.mobLevel() - level), Math.abs(b.mobLevel() - level)));
        return List.copyOf(outOfBand.subList(0, Math.min(FALLBACK_K, outOfBand.size())));
    }
}
