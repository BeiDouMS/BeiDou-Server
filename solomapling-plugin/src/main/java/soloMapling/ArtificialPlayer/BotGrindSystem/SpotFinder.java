package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;
import org.gms.server.life.Monster;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotSpotClaims;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

// Stateless spot math for the grind strategies (CampStrategy/RoamStrategy via GrindBrain). Two jobs:
//  - profile(map): cluster the map's static spawn points into candidate Spots (cluster-sized radii) and
//    measure the map once (walkable span, spawn density, regime). Cached per map — the spawn layout is
//    static WZ data, so this is build-once.
//  - pickBest / bestClusterHostileInBand / nearestHostileCrossLedge: score candidate spots for a specific
//    bot at SELECT_SPOT, and the cheap per-tick target scans FIGHT/WAIT/ROAM use.
//
// Replaces ZoneAllocator (wide band) and the band-scoring half of MobClusterFinder. It scores PLACES TO
// STAND (durable spawn-point density), once per relocation — not individual live mobs, every tick — which
// is what makes the bot settle and camp instead of chasing map-global respawns across the screen. Reads
// terrain only through the generic GCMovement queries. Our own creation (not a GreenCat extraction).
public final class SpotFinder {

    private SpotFinder() {
    }

    // ── Clustering tunables (§4c) ──
    private static final int SPOT_RADIUS_MIN = 250;          // cluster-sized spot radius clamp (lower)
    private static final int SPOT_RADIUS_MAX = 500;          // ... and upper (≈ one screen). THE critical knob-pair.
    private static final int SPOT_CLUSTER_MERGE_PX = 350;    // greedy single-linkage merge distance
    private static final double SPOT_CLUSTER_VERTICAL_SCALE = 2.5; // dy weight vs dx (stacked platforms split)
    private static final int MIN_LEDGE_SPAWNS_FOR_SPOT = 2;  // a thinner ledge folds into the cluster's dominant group

    // ── Regime thresholds (tune knob DEFAULTS only — never branch the FSM). Calibrate against a real
    //    !env grindprofile dump before trusting these (§13.10 #1). ──
    private static final double DENSITY_HI = 0.012;         // spawns/px above which a map reads COMPACT
    private static final double DENSITY_LO = 0.005;         // ... below which it reads SPARSE
    private static final int GAP_LO = 600;                  // inter-spot gap below which a map reads COMPACT
    private static final int SPARSE_SPAWN_COUNT = 6;        // very few spawn points → SPARSE regardless of density
    // Campability floor: if the best spot's sameLedgeSpawnCount is below this, no ledge holds a harvestable
    // pack → the map is roamed, not camped (tiny-platform / jumpy-mob maps). THE roam-vs-camp knob — tune
    // against real !env grindprofile dumps of Terrace Hall + a dice room vs HHG / Forest of Golems.
    private static final int MIN_CAMPABLE_SAMELEDGE_SPAWN = 4;

    // ── Spot-selection score weights (§4) ──
    // The density term counts spawns on the anchor's OWN ledge, not the whole cluster: the vertical-scale
    // merge folds ledges stacked within ~140px into one cluster, but combat's same-ledge gate only ever
    // harvests the anchor's ledge — a raw cluster count advertises feed the bot can't touch, so a
    // "great" spot could actually starve while a slightly smaller single-ledge spot was the honest camp.
    private static final double SPAWN_DENSITY_W = 10.0;     // per same-ledge spawn point (feed the bot can harvest)
    private static final double TIGHTNESS_W = 8.0;         // per same-ledge spawn per 100px of spot width (8 spawns tight >> 8 spawns diffuse)
    private static final double LEDGE_EXTENT_W = 1.0;      // per 100px of anchor-ledge length (mild room-to-fight bonus)
    private static final int LEDGE_EXTENT_CAP_PX = 1_000;  // extent bonus saturates at ~two screens of platform
    private static final double SELECT_JITTER = 12.0;      // additive noise ≈ one spawn point, so identical cohorts decorrelate
    private static final double LIVE_MOB_W = 6.0;          // per live hostile in radius now (start-hot bias)
    private static final double DISTANCE_W = 0.015;        // per px to the anchor (prefer near → minimal traversal)
    private static final double CROWDING_W = 30.0;         // per claimant (spread the cohort)
    // Soft cap, scaled by overflow: each holder past the cap costs a full penalty step. The step dwarfs
    // every other term, so under saturation ranking degenerates to "fewest holders first, score second" —
    // which is exactly the load-balancing we want. A flat penalty here is what used to stack the whole
    // surplus on one platform: all-claimed spots tied on the penalty, ranking fell back to spawn count,
    // and every overflow bot converged on the identical argmax spot.
    private static final double OVER_CAP_PENALTY = 100_000.0;

    // ── Intra-spot sharing (width-derived per-spot claim capacity) ──
    // shareCap ≈ one bot per MIN_BAND_PX of spot span, capped: a long platform legitimately hosts a few
    // bots — each leashed to its own BotSpotClaims.section band, so they stand spaced along it — while a
    // short spot stays effectively single-occupancy. CROWDING_W still prefers empty spots first, so
    // one-per-spot remains the uncrowded norm; the cap only bounds how deep sharing may go before the
    // overflow-scaled penalty pushes bots to the next spot (or, via mapSaturated, the next map).
    private static final int MIN_BAND_PX = 200;             // min personal band width (room to approach + swing)
    static final int SHARE_CAP_MAX = 4;                     // never more than this many claimants on one spot (StackStrategy reuses it per stack)

    // ── Stack detection (map-archetype STACK: vertically layered ledges — trees / towers / subway) ──
    private static final int STACK_DY_MIN = 40;             // closer than this = side-by-side, not stacked
    private static final int STACK_BLINK_DY = 180;          // vertical neighbour gap a mage teleport can bridge
    private static final int STACK_HOP_DY = 110;            // ... a plain jump/rope hop can bridge (non-teleport classes)
    private static final int STACK_X_OVERLAP_MIN = 80;      // member X extents must overlap this much to count as layered

    static int shareCapFor(int radius) {
        return clamp((2 * radius) / MIN_BAND_PX, 1, SHARE_CAP_MAX);
    }

    // Per-map profile cache. Static spawn layout → effectively build-once (no invalidation in practice).
    private static final ConcurrentHashMap<Integer, MapGrindProfile> CACHE = new ConcurrentHashMap<>();

    // Per-map estimated claimable-spot count from raw WZ spawn positions (used before a live profile
    // exists). Static data → build-once, same as the profile cache.
    private static final ConcurrentHashMap<Integer, Integer> ESTIMATED_SPOTS = new ConcurrentHashMap<>();

    // ── Profile (cached) ──

    // The cached grind profile for a map; builds clusters + measures the map on first touch. Building warms
    // the nav graph (walkableLedges / regionIdAt trigger the one-time per-map bake) — fine, the bot has
    // already committed to grinding here. get/putIfAbsent (not computeIfAbsent) so the expensive bake never
    // runs under a map bin lock; a rare double-build just races to the same result.
    public static MapGrindProfile profile(MapleMap map) {
        if (map == null) {
            return null;
        }
        int id = map.getId();
        MapGrindProfile cached = CACHE.get(id);
        if (cached != null) {
            return cached;
        }
        MapGrindProfile built = build(map);
        MapGrindProfile prev = CACHE.putIfAbsent(id, built);
        return prev != null ? prev : built;
    }

    // Peek the cached profile without building — never forces a map load / nav bake (safe from DECIDE).
    public static MapGrindProfile profileIfBuilt(int mapId) {
        return CACHE.get(mapId);
    }

    // A ROAM map's carrying capacity is span-derived, not spot-derived: its spots were just declared
    // un-campable, so counting their share caps over-admits. One bot per this much walkable span keeps
    // roamers' seek boxes (RoamStrategy.ROAM_LEASH_PX-sized) mostly disjoint.
    private static final int ROAM_CAP_SPAN_PX = 1_400;

    // How many training bots a map can carry = the sum of its spots' share caps (a wide platform counts
    // for the few spaced bots it can host, a sliver counts 1), or a walkable-span quota on a ROAM map
    // (whose spots don't gate grinding — Audit III 2.3). Prefer the exact live profile when one exists
    // (some bot already ground there); otherwise estimate by clustering the raw WZ spawn positions with
    // the same rules clusterSpawns uses, minus the foothold snap (raw x/cy is close enough for counting).
    // This is what keeps DECIDE's carrying capacity aligned with claimable slots — the old spawns/N
    // divisor over-admitted 2-4x on dense-cluster maps and guaranteed a stacked surplus.
    public static int mapBotCapacity(int mapId) {
        MapGrindProfile p = CACHE.get(mapId);
        if (p != null && p.roam()) {
            return Math.max(1, p.walkableSpanX() / ROAM_CAP_SPAN_PX);
        }
        if (p != null && !p.spots().isEmpty()) {
            int cap = 0;
            for (Spot s : p.spots()) {
                cap += s.shareCap();
            }
            return cap;
        }
        return Math.max(1, ESTIMATED_SPOTS.computeIfAbsent(mapId,
                id -> estimateShareCapacity(MapMobIndex.spawnPoints(id))));
    }

    // Cluster raw WZ spawn points (same merge metric + per-ledge split + tiling as clusterSpawns, with
    // the WZ foothold group standing in for the nav region) and count the resulting spots. Pure math —
    // public for the headless distribution tests.
    public static int estimateSpotCount(List<MapMobIndex.SpawnPos> spawnPoints) {
        if (spawnPoints == null || spawnPoints.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (List<SpawnPt> c : estimateClusters(spawnPoints)) {
            for (List<SpawnPt> g : partitionByLedge(c)) {
                count += (robustHalfSpreadX(g) <= SPOT_RADIUS_MAX) ? 1 : tileByX(g, 2 * SPOT_RADIUS_MAX).size();
            }
        }
        return count;
    }

    // Same clustering, summed as share capacity (each spot/slice contributes its width-derived shareCap).
    // Pure math — public for the headless distribution tests.
    public static int estimateShareCapacity(List<MapMobIndex.SpawnPos> spawnPoints) {
        if (spawnPoints == null || spawnPoints.isEmpty()) {
            return 0;
        }
        int cap = 0;
        for (List<SpawnPt> c : estimateClusters(spawnPoints)) {
            for (List<SpawnPt> g : partitionByLedge(c)) {
                if (robustHalfSpreadX(g) <= SPOT_RADIUS_MAX) {
                    cap += shareCapFor(clamp(robustHalfSpreadX(g), SPOT_RADIUS_MIN, SPOT_RADIUS_MAX));
                } else {
                    for (List<SpawnPt> slice : tileByX(g, 2 * SPOT_RADIUS_MAX)) {
                        cap += shareCapFor(clamp(robustHalfSpreadX(slice), SPOT_RADIUS_MIN, SPOT_RADIUS_MAX));
                    }
                }
            }
        }
        return cap;
    }

    private static List<List<SpawnPt>> estimateClusters(List<MapMobIndex.SpawnPos> spawnPoints) {
        List<SpawnPt> pts = new ArrayList<>(spawnPoints.size());
        for (MapMobIndex.SpawnPos sp : spawnPoints) {
            pts.add(new SpawnPt(new Point(sp.x(), sp.y()), sp.fhGroup()));
        }
        return greedySingleLinkage(pts, SPOT_CLUSTER_MERGE_PX);
    }

    private static MapGrindProfile build(MapleMap map) {
        List<Spot> spots = clusterSpawns(map);

        // Walkable bbox = the grind-relevant "size" (a map can have a huge VR but a small walkable strip).
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        for (GCMovement.Ledge l : GCMovement.walkableLedges(map)) {
            minX = Math.min(minX, l.minX());
            maxX = Math.max(maxX, l.maxX());
        }
        if (minX > maxX) {
            minX = 0;
            maxX = 0;
        }
        int spanX = Math.max(0, maxX - minX);
        int spawnCount = map.getMonsterSpawnPositions().size();
        double density = spawnCount / (double) Math.max(1, spanX);
        int meanGap = meanInterSpotGapX(spots);
        MapGrindProfile.Regime regime = classify(density, spawnCount, spots.size(), meanGap);

        // Campability: the best single ledge's harvestable spawns. If even the fattest spot can't hold a
        // pack (tiny-platform / jumpy-mob maps), the map is roamed, not camped. THE key calibration knob.
        int bestSameLedge = 0;
        for (Spot s : spots) {
            bestSameLedge = Math.max(bestSameLedge, s.sameLedgeSpawnCount());
        }
        boolean roam = bestSameLedge < MIN_CAMPABLE_SAMELEDGE_SPAWN;

        return new MapGrindProfile(map.getId(), minX, maxX, spanX, spawnCount, density,
                spots, spots.size(), meanGap, regime, roam, detectStacks(spots), System.currentTimeMillis());
    }

    // ── Stack detection: union overlapping-X spots whose anchors sit within blink reach vertically ──

    // Two spots are stack-adjacent when their ledges are vertically layered: different real regions, a
    // vertical gap in [DY_MIN, BLINK_DY], and X extents overlapping enough that a bot working one ledge
    // visibly sits above/below the other (slime tree levels, subway car roofs).
    private static boolean stackAdjacent(Spot a, Spot b) {
        if (a.regionId() < 0 || b.regionId() < 0 || a.regionId() == b.regionId()) {
            return false;
        }
        int dy = Math.abs(a.anchor().y - b.anchor().y);
        if (dy < STACK_DY_MIN || dy > STACK_BLINK_DY) {
            return false;
        }
        int overlap = Math.min(a.anchor().x + a.radius(), b.anchor().x + b.radius())
                - Math.max(a.anchor().x - a.radius(), b.anchor().x - b.radius());
        return overlap >= STACK_X_OVERLAP_MIN;
    }

    // Group stack-adjacent spots transitively (same union-find as the spawn clustering) and keep groups
    // of 2+. hopTraversable = every consecutive-by-y neighbour gap is within plain-jump reach, so a
    // non-teleport class can work the stack; a blink-only gap restricts it to mages.
    private static List<SpotStack> detectStacks(List<Spot> spots) {
        int n = spots.size();
        if (n < 2) {
            return List.of();
        }
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (stackAdjacent(spots.get(i), spots.get(j))) {
                    union(parent, i, j);
                }
            }
        }
        java.util.Map<Integer, List<Integer>> byRoot = new java.util.HashMap<>();
        for (int i = 0; i < n; i++) {
            byRoot.computeIfAbsent(find(parent, i), k -> new ArrayList<>()).add(i);
        }
        List<SpotStack> stacks = new ArrayList<>();
        for (List<Integer> group : byRoot.values()) {
            if (group.size() < 2) {
                continue;
            }
            group.sort((ia, ib) -> Integer.compare(spots.get(ia).anchor().y, spots.get(ib).anchor().y)); // top-down
            int x0 = Integer.MAX_VALUE, x1 = Integer.MIN_VALUE, feed = 0;
            boolean hop = true;
            for (int k = 0; k < group.size(); k++) {
                Spot s = spots.get(group.get(k));
                x0 = Math.min(x0, s.anchor().x - s.radius());
                x1 = Math.max(x1, s.anchor().x + s.radius());
                feed += s.sameLedgeSpawnCount();
                if (k > 0) {
                    int gap = s.anchor().y - spots.get(group.get(k - 1)).anchor().y;
                    hop &= gap <= STACK_HOP_DY;
                }
            }
            stacks.add(new SpotStack(List.copyOf(group), x0, x1,
                    spots.get(group.get(0)).anchor().y, spots.get(group.get(group.size() - 1)).anchor().y,
                    feed, hop));
        }
        return List.copyOf(stacks);
    }

    private static MapGrindProfile.Regime classify(double density, int spawnCount, int clusterCount, int meanGap) {
        if (spawnCount <= SPARSE_SPAWN_COUNT || density <= DENSITY_LO) {
            return MapGrindProfile.Regime.SPARSE;
        }
        if (clusterCount <= 1 || (density >= DENSITY_HI && meanGap <= GAP_LO)) {
            return MapGrindProfile.Regime.COMPACT;
        }
        return MapGrindProfile.Regime.SPREAD;
    }

    // Mean |Δx| between x-sorted adjacent spot anchors (0 if < 2 spots).
    private static int meanInterSpotGapX(List<Spot> spots) {
        if (spots.size() < 2) {
            return 0;
        }
        List<Integer> xs = new ArrayList<>(spots.size());
        for (Spot s : spots) {
            xs.add(s.anchor().x);
        }
        xs.sort(Integer::compareTo);
        long sum = 0;
        for (int i = 1; i < xs.size(); i++) {
            sum += Math.abs(xs.get(i) - xs.get(i - 1));
        }
        return (int) (sum / (xs.size() - 1));
    }

    // ── Clustering: greedy single-linkage, anisotropic (§4c) ──

    private record SpawnPt(Point foothold, int region) {
    }

    private static List<Spot> clusterSpawns(MapleMap map) {
        // 1. Snap every static spawn point to the foothold under it + its region.
        List<SpawnPt> pts = new ArrayList<>();
        for (Point p : map.getMonsterSpawnPositions()) {
            Point g = GCMovement.groundPointBelow(map, p.x, p.y);
            Point a = (g != null) ? g : p;
            int region = GCMovement.regionIdAt(map, a.x, a.y);
            pts.add(new SpawnPt(a, region));
        }
        if (pts.isEmpty()) {
            return List.of();
        }

        // 2. Greedy single-linkage under the anisotropic metric (dy weighted x VERTICAL_SCALE) so a long
        //    horizontal platform stays ONE cluster while stacked platforms split, without hard region gating.
        List<List<SpawnPt>> clusters = greedySingleLinkage(pts, SPOT_CLUSTER_MERGE_PX);

        // Ledge bounds by region id, for the extent term (graph is baked by the regionIdAt calls above).
        java.util.Map<Integer, GCMovement.Ledge> ledgeById = new java.util.HashMap<>();
        for (GCMovement.Ledge l : GCMovement.walkableLedges(map)) {
            ledgeById.put(l.regionId(), l);
        }

        // 3. Split each locality cluster along ledge boundaries, then turn each ledge group into one
        //    Spot (or tile a too-wide group into a row of MAX-bounded spots).
        List<Spot> spots = new ArrayList<>();
        for (List<SpawnPt> c : clusters) {
            for (List<SpawnPt> g : partitionByLedge(c)) {
                if (robustHalfSpreadX(g) <= SPOT_RADIUS_MAX) {
                    spots.add(makeSpot(g, ledgeById));
                } else {
                    for (List<SpawnPt> slice : tileByX(g, 2 * SPOT_RADIUS_MAX)) {
                        spots.add(makeSpot(slice, ledgeById));
                    }
                }
            }
        }
        return spots;
    }

    // Split a locality cluster along its ledge boundaries — the same authority the combat gate uses
    // (onDifferentLedge is a region comparison), so every spawn-bearing platform becomes its own
    // claimable spot. Without this, the anisotropic merge could chain two stacked platforms into ONE
    // cluster and the ledge that lost the anchor produced no spot at all, sitting visibly empty with a
    // full set of respawning spawn points (Monkey Swamp III: two 6-spawn platforms, seven pixels inside
    // the merge threshold). Graph-unplaced members (region -1) and sub-threshold stray ledges fold into
    // the dominant group — exactly the old accounting for them — and an unbaked graph (no real region
    // ids at all) degrades to one group per cluster, i.e. the old behaviour byte-for-byte.
    private static List<List<SpawnPt>> partitionByLedge(List<SpawnPt> cluster) {
        java.util.Map<Integer, List<SpawnPt>> byRegion = new java.util.LinkedHashMap<>();
        for (SpawnPt s : cluster) {
            byRegion.computeIfAbsent(s.region(), k -> new ArrayList<>()).add(s);
        }
        int domRegion = -1;
        for (java.util.Map.Entry<Integer, List<SpawnPt>> e : byRegion.entrySet()) {
            if (e.getKey() < 0) {
                continue;
            }
            if (domRegion < 0 || e.getValue().size() > byRegion.get(domRegion).size()
                    || (e.getValue().size() == byRegion.get(domRegion).size() && e.getKey() < domRegion)) {
                domRegion = e.getKey();
            }
        }
        if (domRegion < 0 || byRegion.size() <= 1) {
            return List.of(cluster); // graph placed nothing (or a single ledge) — keep the cluster whole
        }
        List<List<SpawnPt>> groups = new ArrayList<>();
        List<SpawnPt> dominant = new ArrayList<>(byRegion.get(domRegion));
        for (java.util.Map.Entry<Integer, List<SpawnPt>> e : byRegion.entrySet()) {
            if (e.getKey() == domRegion) {
                continue;
            }
            if (e.getKey() >= 0 && e.getValue().size() >= MIN_LEDGE_SPAWNS_FOR_SPOT) {
                groups.add(e.getValue()); // a real ledge with enough feed -> its own spot
            } else {
                dominant.addAll(e.getValue()); // stray spawn / unplaced -> fold into the dominant group
            }
        }
        groups.add(0, dominant);
        return groups;
    }

    // Union-find single-linkage: any two points within mergePx (anisotropic) join, transitively.
    private static List<List<SpawnPt>> greedySingleLinkage(List<SpawnPt> pts, int mergePx) {
        int n = pts.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        double thr2 = (double) mergePx * mergePx;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (dist2(pts.get(i).foothold(), pts.get(j).foothold()) <= thr2) {
                    union(parent, i, j);
                }
            }
        }
        java.util.Map<Integer, List<SpawnPt>> byRoot = new java.util.HashMap<>();
        for (int i = 0; i < n; i++) {
            byRoot.computeIfAbsent(find(parent, i), k -> new ArrayList<>()).add(pts.get(i));
        }
        return new ArrayList<>(byRoot.values());
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }

    private static void union(int[] parent, int a, int b) {
        int ra = find(parent, a), rb = find(parent, b);
        if (ra != rb) {
            parent[ra] = rb;
        }
    }

    private static double dist2(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = (a.y - b.y) * SPOT_CLUSTER_VERTICAL_SCALE;
        return dx * dx + dy * dy;
    }

    // p90 of |x - meanX| — the horizontal half-extent, ignoring a lone outlier. 0 for a single point.
    private static int robustHalfSpreadX(List<SpawnPt> members) {
        int n = members.size();
        if (n <= 1) {
            return 0;
        }
        long sumX = 0;
        for (SpawnPt s : members) {
            sumX += s.foothold().x;
        }
        double meanX = sumX / (double) n;
        List<Integer> devs = new ArrayList<>(n);
        for (SpawnPt s : members) {
            devs.add((int) Math.abs(s.foothold().x - meanX));
        }
        devs.sort(Integer::compareTo);
        int idx = (int) Math.round(0.9 * (n - 1));
        return devs.get(Math.max(0, Math.min(n - 1, idx)));
    }

    // Split an over-wide cluster into adjacent <=sliceWidth-wide slices by x; each slice owns its points.
    private static List<List<SpawnPt>> tileByX(List<SpawnPt> members, int sliceWidth) {
        List<SpawnPt> sorted = new ArrayList<>(members);
        sorted.sort((a, b) -> Integer.compare(a.foothold().x, b.foothold().x));
        List<List<SpawnPt>> slices = new ArrayList<>();
        List<SpawnPt> cur = new ArrayList<>();
        int sliceStartX = sorted.isEmpty() ? 0 : sorted.get(0).foothold().x;
        for (SpawnPt s : sorted) {
            if (!cur.isEmpty() && s.foothold().x - sliceStartX > sliceWidth) {
                slices.add(cur);
                cur = new ArrayList<>();
                sliceStartX = s.foothold().x;
            }
            cur.add(s);
        }
        if (!cur.isEmpty()) {
            slices.add(cur);
        }
        return slices;
    }

    // Anchor = a member foothold on the cluster's DOMINANT ledge (the region holding the most member
    // spawns), nearest that group's centroid — guaranteed walkable, and planted where the harvestable
    // feed actually is (a whole-cluster centroid can land the anchor on a thin side ledge whose spawns
    // the same-ledge combat gate then never reaches). radius = the cluster's robust half-spread,
    // clamped to [MIN, MAX]. Falls back to whole-cluster anchoring when the graph gave no region ids.
    private static Spot makeSpot(List<SpawnPt> members, java.util.Map<Integer, GCMovement.Ledge> ledgeById) {
        // Dominant ledge group (largest real-region bucket; ties break to the lower region id so the
        // profile is deterministic across builds).
        java.util.Map<Integer, List<SpawnPt>> byRegion = new java.util.HashMap<>();
        for (SpawnPt s : members) {
            byRegion.computeIfAbsent(s.region(), k -> new ArrayList<>()).add(s);
        }
        List<SpawnPt> dom = members;
        int domRegion = -1;
        for (java.util.Map.Entry<Integer, List<SpawnPt>> e : byRegion.entrySet()) {
            if (e.getKey() < 0) {
                continue; // spawns the graph couldn't place — never anchor-eligible
            }
            if (domRegion < 0 || e.getValue().size() > byRegion.get(domRegion).size()
                    || (e.getValue().size() == byRegion.get(domRegion).size() && e.getKey() < domRegion)) {
                domRegion = e.getKey();
            }
        }
        if (domRegion >= 0) {
            dom = byRegion.get(domRegion);
        }
        long sumX = 0, sumY = 0;
        for (SpawnPt s : dom) {
            sumX += s.foothold().x;
            sumY += s.foothold().y;
        }
        Point centroid = new Point((int) (sumX / dom.size()), (int) (sumY / dom.size()));
        SpawnPt anchor = dom.get(0);
        double bestSq = Double.MAX_VALUE;
        for (SpawnPt s : dom) {
            double dsq = s.foothold().distanceSq(centroid);
            if (dsq < bestSq) {
                bestSq = dsq;
                anchor = s;
            }
        }
        int radius = clamp(robustHalfSpreadX(members), SPOT_RADIUS_MIN, SPOT_RADIUS_MAX);
        GCMovement.Ledge ledge = ledgeById.get(anchor.region());
        int span = (ledge != null) ? Math.max(0, ledge.maxX() - ledge.minX()) : 0;
        int sameLedge = (domRegion >= 0) ? dom.size() : members.size(); // unbaked graph → no split known
        return new Spot(anchor.foothold(), anchor.region(), radius, members.size(), sameLedge, span,
                shareCapFor(radius));
    }

    // ── Selection + the FIGHT/WAIT radius scan ──

    // Score each candidate spot for this bot and return the highest scorer (or null if none reachable).
    // Hard filters: a spot's region must be reachable from where the bot stands (skip the filter if that
    // set is empty — graph unbaked / on no ledge); skip the just-left spot while its cooldown holds. An
    // over-cap spot is heavily down-weighted (chosen only if nothing else qualifies). DISTANCE_W is the
    // anti-traversal lever at the selection layer — a near healthy spot beats a far marginally-denser one.
    public static Spot pickBest(Character chr, MapGrindProfile p, int excludedIdx, long excludedUntilMs) {
        if (chr == null || p == null || p.spots().isEmpty()) {
            return null;
        }
        MapleMap map = chr.getMap();
        Point pos = chr.getPosition();
        if (map == null || pos == null) {
            return null;
        }
        Set<Integer> reach = GCMovement.reachableRegions(map, pos.x, pos.y);
        boolean filter = !reach.isEmpty();
        long now = System.currentTimeMillis();
        List<Spot> spots = p.spots();
        Spot best = null;
        double bestScore = -Double.MAX_VALUE;
        for (int i = 0; i < spots.size(); i++) {
            Spot s = spots.get(i);
            if (filter && s.regionId() >= 0 && !reach.contains(s.regionId())) {
                continue; // unreachable island ledge
            }
            if (i == excludedIdx && now < excludedUntilMs) {
                continue; // just-left this spot — let it cool down
            }
            int holders = BotSpotClaims.holders(map.getId(), i);
            double score = SPAWN_DENSITY_W * s.sameLedgeSpawnCount()
                    + TIGHTNESS_W * (100.0 * s.sameLedgeSpawnCount() / Math.max(1, 2 * s.radius()))
                    + LEDGE_EXTENT_W * (Math.min(s.ledgeSpanPx(), LEDGE_EXTENT_CAP_PX) / 100.0)
                    + LIVE_MOB_W * liveHostilesWithin(map, s.anchor(), s.radius())
                    - DISTANCE_W * pos.distance(s.anchor())
                    - CROWDING_W * holders
                    - OVER_CAP_PENALTY * Math.max(0, holders - s.shareCap() + 1)
                    + ThreadLocalRandom.current().nextDouble() * SELECT_JITTER;
            if (score > bestScore) {
                bestScore = score;
                best = s;
            }
        }
        return best;
    }

    // Nearest live hostile within a box around `from`, IGNORING the same-ledge gate — the target set for a
    // ROAMING bot on a map that can't be camped (mobs scattered across many tiny platforms / jumping between
    // them). The mover paths across ledges (walk / arced jump / rope / blink) to reach it. Cross-ledge is the
    // whole point here, so there is no onDifferentLedge filter.
    public static Monster nearestHostileCrossLedge(MapleMap map, Point from, int rangeX, int rangeY) {
        return nearestHostileCrossLedge(map, from, rangeX, rangeY, null, rangeY);
    }

    // As above, plus the roam "Phase-3-lite" vertical unlock: a mob beyond rangeY (up to stackRangeY) is
    // still admitted when it stands on a ledge in the SAME detected stack as the bot — a validated
    // blink/hop pair — so slime-tree / subway verticality works without re-opening open-descent plunges
    // (no stack adjacency -> the tight rangeY still gates). p null = plain horizontal-first behavior.
    public static Monster nearestHostileCrossLedge(MapleMap map, Point from, int rangeX, int rangeY,
                                                   MapGrindProfile p, int stackRangeY) {
        if (map == null || from == null) {
            return null;
        }
        Monster best = null;
        double bestSq = Double.MAX_VALUE;
        for (Monster m : map.getAllMonsters()) {
            if (!isHostile(m)) {
                continue;
            }
            Point mp = m.getPosition();
            if (mp == null || Math.abs(mp.x - from.x) > rangeX) {
                continue;
            }
            int ady = Math.abs(mp.y - from.y);
            if (ady > rangeY && !(ady <= stackRangeY && sameStack(p, map, from, mp))) {
                continue;
            }
            double dsq = from.distanceSq(mp);
            if (dsq < bestSq) {
                bestSq = dsq;
                best = m;
            }
        }
        return best;
    }

    // True when a and b stand on ledges belonging to the SAME detected stack — the validated vertical
    // pair a roamer may traverse (blink / hop / rope) without risking an open descent. Same-ledge pairs
    // return false (no stack traversal needed); unplaced ground (region -1) is never stack-validated.
    public static boolean sameStack(MapGrindProfile p, MapleMap map, Point a, Point b) {
        if (p == null || p.stacks().isEmpty()) {
            return false;
        }
        int ra = regionOfGround(map, a);
        int rb = regionOfGround(map, b);
        if (ra < 0 || rb < 0 || ra == rb) {
            return false;
        }
        for (SpotStack st : p.stacks()) {
            boolean hasA = false;
            boolean hasB = false;
            for (int idx : st.spotIndices()) {
                int r = p.spots().get(idx).regionId();
                hasA |= r == ra;
                hasB |= r == rb;
            }
            if (hasA && hasB) {
                return true;
            }
        }
        return false;
    }

    // The region of the ground under a point (an airborne/jumping mob reads as its landing ledge).
    private static int regionOfGround(MapleMap map, Point pt) {
        Point g = GCMovement.groundPointBelow(map, pt.x, pt.y);
        Point use = (g != null) ? g : pt;
        return GCMovement.regionIdAt(map, use.x, use.y);
    }

    // Cluster-biased hostile pick across a SET of member ledges within a tether box — the STACK
    // acquisition. members = the stack's spots; preferred non-null restricts the hunt to the bot's
    // assigned ledge (pass null to hunt the whole stack). A mob counts as "on" a member ledge via the
    // same onDifferentLedge authority the camp gate uses, so stacked bots never chase off-tether mobs.
    public static Monster bestStackHostile(MapleMap map, List<Spot> members, Spot preferred,
                                           int x0, int x1, int yTop, int yBottom,
                                           Point from, int clusterRadius) {
        if (map == null || members == null || members.isEmpty() || from == null) {
            return null;
        }
        List<Monster> band = new ArrayList<>();
        for (Monster m : map.getAllMonsters()) {
            if (!isHostile(m)) {
                continue;
            }
            Point mp = m.getPosition();
            if (mp == null || mp.x < x0 || mp.x > x1 || mp.y < yTop || mp.y > yBottom) {
                continue;
            }
            if (preferred != null) {
                if (!GCMovement.onDifferentLedge(map, preferred.anchor().x, preferred.anchor().y, mp.x, mp.y)) {
                    band.add(m);
                }
                continue;
            }
            for (Spot member : members) {
                if (!GCMovement.onDifferentLedge(map, member.anchor().x, member.anchor().y, mp.x, mp.y)) {
                    band.add(m);
                    break;
                }
            }
        }
        return bestClustered(band, from, clusterRadius);
    }

    // Cluster-biased target pick on the anchor's OWN ledge, within the [x0,x1] band and the coarse
    // radius-tall Y box: favours the mob sitting in the DENSEST local pack (most same-band neighbours
    // within clusterRadius), tie-broken by distance from `from` (the BOT's position, not the anchor —
    // anchor-relative picks made every sharer converge on the identical mob). The same-ledge gate is what
    // keeps a planted bot from acquiring a mob on a stacked platform and roping across to it; the Y box is
    // a coarse pre-filter and the sole vertical bound when the nav graph isn't baked.
    // Keeps a grinder from locking onto a lone straggler while a pack sits nearby, and feeds the AoE
    // reposition (the cluster it targets is the cluster it centres the swing on). clusterRadius <= 0
    // degrades to plain nearest.
    public static Monster bestClusterHostileInBand(MapleMap map, Point anchor, int radius,
                                                   int x0, int x1, Point from, int clusterRadius) {
        if (map == null || anchor == null) {
            return null;
        }
        Point ref = (from != null) ? from : anchor;
        List<Monster> band = new ArrayList<>();
        for (Monster m : map.getAllMonsters()) {
            if (!isHostile(m)) {
                continue;
            }
            Point mp = m.getPosition();
            if (mp == null || mp.x < x0 || mp.x > x1 || Math.abs(mp.y - anchor.y) > radius) {
                continue;
            }
            if (GCMovement.onDifferentLedge(map, anchor.x, anchor.y, mp.x, mp.y)) {
                continue;
            }
            band.add(m);
        }
        return bestClustered(band, ref, clusterRadius);
    }

    // Shared cluster scorer: the band member with the most same-band neighbours within clusterRadius,
    // tie-broken by distance from `ref`. clusterRadius <= 0 degrades to plain nearest.
    private static Monster bestClustered(List<Monster> band, Point ref, int clusterRadius) {
        Monster best = null;
        int bestScore = Integer.MIN_VALUE;
        double bestSq = Double.MAX_VALUE;
        long crSq = (long) clusterRadius * clusterRadius;
        for (Monster m : band) {
            Point mp = m.getPosition();
            int score = 0;
            if (clusterRadius > 0) {
                for (Monster o : band) {
                    if (o != m && mp.distanceSq(o.getPosition()) <= crSq) {
                        score++;
                    }
                }
            }
            double dsq = ref.distanceSq(mp);
            if (score > bestScore || (score == bestScore && dsq < bestSq)) {
                bestScore = score;
                bestSq = dsq;
                best = m;
            }
        }
        return best;
    }

    // Centroid of same-ledge live hostiles within `radius` of `center`, or null if fewer than `minMobs`
    // are packed there. Used to step an AoE class into the middle of the pack before firing.
    public static Point hostileClusterCentroid(MapleMap map, Point center, int radius, int minMobs) {
        if (map == null || center == null) {
            return null;
        }
        long sumX = 0;
        long sumY = 0;
        int n = 0;
        long rSq = (long) radius * radius;
        for (Monster m : map.getAllMonsters()) {
            if (!isHostile(m)) {
                continue;
            }
            Point mp = m.getPosition();
            if (mp == null || center.distanceSq(mp) > rSq) {
                continue;
            }
            if (GCMovement.onDifferentLedge(map, center.x, center.y, mp.x, mp.y)) {
                continue;
            }
            sumX += mp.x;
            sumY += mp.y;
            n++;
        }
        return n >= minMobs ? new Point((int) (sumX / n), (int) (sumY / n)) : null;
    }

    // Live hostiles within a spot's radius (backs the !env grindprofile debug dump).
    public static int liveHostilesWithin(MapleMap map, Spot spot) {
        return spot == null ? 0 : liveHostilesWithin(map, spot.anchor(), spot.radius());
    }

    private static int liveHostilesWithin(MapleMap map, Point anchor, int radius) {
        int count = 0;
        for (Monster m : map.getAllMonsters()) {
            if (!isHostile(m)) {
                continue;
            }
            Point mp = m.getPosition();
            if (mp == null || Math.abs(mp.x - anchor.x) > radius || Math.abs(mp.y - anchor.y) > radius) {
                continue;
            }
            if (GCMovement.onDifferentLedge(map, anchor.x, anchor.y, mp.x, mp.y)) {
                continue; // count only same-ledge mobs so a spot's "fight now" score reflects its own platform
            }
            count++;
        }
        return count;
    }

    // Shared hostile predicate (moved here from MobSeeker.isHostile; SpotFinder is the stateless spot/mob
    // util, so it's the single home — GrindBrain and TrainingBot both call SpotFinder.isHostile).
    public static boolean isHostile(Monster m) {
        return m != null && m.isAlive()
                && (m.getStats() == null || !m.getStats().isFriendly());
    }

    private static int clamp(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
