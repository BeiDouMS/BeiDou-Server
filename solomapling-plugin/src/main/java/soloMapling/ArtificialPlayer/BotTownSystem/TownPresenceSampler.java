package soloMapling.ArtificialPlayer.BotTownSystem;

import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Portal;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

// Anchor-weighted town scatter: pick N ground spots on a map from a weighted distribution instead of
// uniformly, so ambient bots read as "lived in" (hanging out near shops/quest givers, concentrated on
// the main streets) rather than sprinkled evenly. Zero map-specific code - every signal is WZ-derived:
//
//   - NPC positions are the strongest anchor (life clusters around shops and quest givers); portal
//     positions are a weaker anchor (foot traffic near entrances). Each anchor projects a 2-D gaussian
//     bump onto nearby reachable ledges (tight in Y so an NPC pulls its own platform, not the column).
//   - A shape profile decays weight with height above the map's main ground band, so tall maps
//     (Ellinia) concentrate low and wide maps (Kerning) spread horizontally - falls out of the geometry.
//   - A thin uniform tail (~1 in 11 picks) ignores the weighting and lands anywhere reachable, so a few
//     organic stragglers end up on rarely-visited high ledges (the "someone's up on the crane" effect).
//
// Reads terrain only through the generic GCMovement spatial queries (same discipline as BotSpotPicker),
// so placement stays out of the movement package. Our own creation (not a GreenCat extraction).
public final class TownPresenceSampler {

    private TownPresenceSampler() {
    }

    private static final Random RANDOM = new Random();

    // Anchor pull strengths and falloff.
    private static final double NPC_STRENGTH = 1.0;
    private static final double PORTAL_STRENGTH = 0.45;
    private static final double SIGMA_X = 260.0;   // horizontal spread of an anchor's pull (px)
    private static final double SIGMA_Y = 130.0;   // vertical spread - keeps a pull on the anchor's platform

    // Height (px) above the main ground band at which the shape profile drops weight by ~1/e.
    private static final double HEIGHT_DECAY = 320.0;

    // Weight floor every reachable ledge keeps even with no anchor nearby, so the uniform tail and quiet
    // corners are still reachable (a fully-zero ledge would be unreachable to the weighted picks).
    private static final double BASE_WEIGHT = 0.15;

    // Fraction of picks that ignore weighting entirely (organic stragglers on low-weight ledges).
    private static final double TAIL_FRACTION = 0.09;

    // Best-effort horizontal gap between spots sharing a ledge, and how hard we try to honor it.
    private static final int MIN_SPACING = 30;
    private static final int X_CANDIDATES = 7; // X samples scored per ledge pick (bias toward anchors)

    // Pick up to `count` anchor-weighted ground spots on `map`, reachable from `anchor` (the town spawn
    // portal). Returns fewer than count only if the nav graph isn't baked / there are no ledges; the
    // caller falls back to the anchor for any shortfall (same contract as BotSpotPicker).
    public static List<Point> sample(MapleMap map, Point anchor, int count) {
        return sample(map, anchor, count, TownOverrides.EMPTY);
    }

    // As sample(), but composing the curation overrides: pinned spots are placed first, ban zones are
    // never placed in, and boost zones pull more of the crowd. Overrides compose with the algorithm - the
    // weighted distribution is still the floor everywhere the owner hasn't hand-touched.
    public static List<Point> sample(MapleMap map, Point anchor, int count, TownOverrides overrides) {
        List<Point> out = new ArrayList<>();
        if (map == null || anchor == null || count <= 0) {
            return out;
        }
        TownOverrides ov = overrides != null ? overrides : TownOverrides.EMPTY;

        // Pinned spots first (deliberate hand-polish) - snap each down to the foothold under it.
        for (Point pin : ov.pins()) {
            if (out.size() >= count) {
                return out;
            }
            Point ground = GCMovement.groundPointBelow(map, pin.x, pin.y);
            out.add(ground != null ? ground : new Point(pin));
        }
        int remaining = count - out.size();
        if (remaining <= 0) {
            return out;
        }

        List<GCMovement.Ledge> ledges = reachableLedges(map, anchor);
        if (ledges.isEmpty()) {
            return out;
        }
        List<Anchor> anchors = collectAnchors(map);
        double groundBandY = groundBandY(ledges);

        double[] weights = new double[ledges.size()];
        for (int i = 0; i < ledges.size(); i++) {
            weights[i] = ledgeWeight(ledges.get(i), anchors, groundBandY, ov);
        }

        Map<Integer, List<Integer>> occupiedByLedge = new HashMap<>();
        for (int i = 0; i < remaining; i++) {
            GCMovement.Ledge l = RANDOM.nextDouble() < TAIL_FRACTION
                    ? ledges.get(RANDOM.nextInt(ledges.size())) // uniform straggler
                    : weightedPick(ledges, weights);
            List<Integer> taken = occupiedByLedge.computeIfAbsent(l.regionId(), k -> new ArrayList<>());
            int x = pickX(l, anchors, taken, ov);
            taken.add(x);
            Point ground = GCMovement.groundPointInRegion(map, l.regionId(), x);
            out.add(ground != null ? ground : new Point(x, l.centerY()));
        }
        return out;
    }

    // Walkable ledges reachable from the anchor. Mirrors BotSpotPicker: if the anchor resolves to no
    // reachable region (off a ledge), don't filter rather than come back empty.
    private static List<GCMovement.Ledge> reachableLedges(MapleMap map, Point anchor) {
        List<GCMovement.Ledge> all = GCMovement.walkableLedges(map);
        if (all.isEmpty()) {
            return all;
        }
        Set<Integer> reachable = GCMovement.reachableRegions(map, anchor.x, anchor.y);
        if (reachable.isEmpty()) {
            return all;
        }
        List<GCMovement.Ledge> out = new ArrayList<>();
        for (GCMovement.Ledge l : all) {
            if (reachable.contains(l.regionId())) {
                out.add(l);
            }
        }
        return out.isEmpty() ? all : out;
    }

    // NPCs (strong) + portals (weak) as pull anchors. NPCs define shops / quest givers / interior rooms;
    // portals mark the entrances foot traffic gathers near.
    private static List<Anchor> collectAnchors(MapleMap map) {
        List<Anchor> anchors = new ArrayList<>();
        for (MapObject npc : map.getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY,
                Arrays.asList(MapObjectType.NPC))) {
            Point p = npc.getPosition();
            if (p != null) {
                anchors.add(new Anchor(p.x, p.y, NPC_STRENGTH));
            }
        }
        for (Portal portal : map.getPortals()) {
            Point p = portal.getPosition();
            if (p != null) {
                anchors.add(new Anchor(p.x, p.y, PORTAL_STRENGTH));
            }
        }
        return anchors;
    }

    // The Y of the map's main ground band: the lowest-on-screen (largest Y) among the broad platforms,
    // so height decay is measured from the floor players actually stand on, not a high sliver.
    private static double groundBandY(List<GCMovement.Ledge> ledges) {
        int maxSpan = 1;
        for (GCMovement.Ledge l : ledges) {
            maxSpan = Math.max(maxSpan, l.maxX() - l.minX());
        }
        int band = Integer.MIN_VALUE;
        for (GCMovement.Ledge l : ledges) {
            if ((l.maxX() - l.minX()) >= maxSpan / 2) {
                band = Math.max(band, l.centerY()); // broad platform -> candidate floor
            }
        }
        return band == Integer.MIN_VALUE ? ledges.get(0).centerY() : band;
    }

    // Per-ledge weight = span * shape-profile * (floor + anchor pull), then curation overrides: a ledge
    // whose center sits in a ban zone is excluded (weight 0); a boost zone multiplies its weight.
    private static double ledgeWeight(GCMovement.Ledge l, List<Anchor> anchors, double groundBandY, TownOverrides ov) {
        if (ov.isBanned(l.centerX(), l.centerY())) {
            return 0.0;
        }
        double span = Math.max(1, l.maxX() - l.minX());
        double heightAbove = Math.max(0.0, groundBandY - l.centerY()); // Y grows downward -> higher = smaller Y
        double shape = Math.exp(-heightAbove / HEIGHT_DECAY);
        double pull = 0.0;
        for (Anchor a : anchors) {
            pull += anchorPull(a, nearestX(l, a.x), l.centerY());
        }
        return span * shape * (BASE_WEIGHT + pull) * ov.boostMultiplier(l.centerX(), l.centerY());
    }

    // A single anchor's 2-D gaussian pull at (x,y).
    private static double anchorPull(Anchor a, int x, int y) {
        double dx = a.x - x;
        double dy = a.y - y;
        return a.strength * Math.exp(-(dx * dx) / (2 * SIGMA_X * SIGMA_X))
                * Math.exp(-(dy * dy) / (2 * SIGMA_Y * SIGMA_Y));
    }

    // Closest X on the ledge span to a given x (the point where the anchor pulls hardest on this ledge).
    private static int nearestX(GCMovement.Ledge l, int x) {
        return Math.max(l.minX(), Math.min(l.maxX(), x));
    }

    // Pick an X on the ledge biased toward its strongest nearby anchor, honoring best-effort spacing and
    // the curation overrides (never land in a ban zone; a boost zone raises the local score). Scores a
    // handful of uniform candidates and keeps the best.
    private static int pickX(GCMovement.Ledge l, List<Anchor> anchors, List<Integer> taken, TownOverrides ov) {
        if (l.maxX() <= l.minX()) {
            return l.minX();
        }
        int best = l.minX();
        double bestScore = -Double.MAX_VALUE;
        for (int i = 0; i < X_CANDIDATES; i++) {
            int x = l.minX() + RANDOM.nextInt(l.maxX() - l.minX() + 1);
            if (ov.isBanned(x, l.centerY())) {
                continue; // never place inside a ban zone
            }
            double pull = 0.0;
            for (Anchor a : anchors) {
                pull += anchorPull(a, x, l.centerY());
            }
            double score = pull * ov.boostMultiplier(x, l.centerY()) - crowding(x, taken);
            if (score > bestScore) {
                bestScore = score;
                best = x;
            }
        }
        return best;
    }

    // Penalty for landing within MIN_SPACING of an already-taken X on this ledge (keeps spots from stacking).
    private static double crowding(int x, List<Integer> taken) {
        double penalty = 0.0;
        for (int t : taken) {
            int d = Math.abs(t - x);
            if (d < MIN_SPACING) {
                penalty += (MIN_SPACING - d); // closer -> heavier penalty
            }
        }
        return penalty;
    }

    private static GCMovement.Ledge weightedPick(List<GCMovement.Ledge> ledges, double[] weights) {
        double total = 0.0;
        for (double w : weights) {
            total += w;
        }
        if (total <= 0.0) {
            return ledges.get(RANDOM.nextInt(ledges.size()));
        }
        double r = RANDOM.nextDouble() * total;
        double cumulative = 0.0;
        for (int i = 0; i < ledges.size(); i++) {
            cumulative += weights[i];
            if (r < cumulative) {
                return ledges.get(i);
            }
        }
        return ledges.get(ledges.size() - 1);
    }

    // Human-readable dump of where weight concentrates on a map: anchor count + the top-N ledges by
    // weight (region id, X span, centerY, relative weight %). Feeds the !env townpresence weights command
    // so tuning is tune -> look -> tune without a restart. Diagnostics only.
    public static String describe(MapleMap map, Point anchor, int topN) {
        return describe(map, anchor, topN, TownOverrides.EMPTY);
    }

    public static String describe(MapleMap map, Point anchor, int topN, TownOverrides overrides) {
        if (map == null || anchor == null) {
            return "town weights: no map/anchor";
        }
        TownOverrides ov = overrides != null ? overrides : TownOverrides.EMPTY;
        List<GCMovement.Ledge> ledges = reachableLedges(map, anchor);
        if (ledges.isEmpty()) {
            return "town weights: no baked ledges (nav graph not built yet)";
        }
        List<Anchor> anchors = collectAnchors(map);
        double groundBandY = groundBandY(ledges);
        double total = 0.0;
        List<double[]> rows = new ArrayList<>(); // {regionId, minX, maxX, centerY, weight}
        for (GCMovement.Ledge l : ledges) {
            double w = ledgeWeight(l, anchors, groundBandY, ov);
            total += w;
            rows.add(new double[]{l.regionId(), l.minX(), l.maxX(), l.centerY(), w});
        }
        rows.sort((a, b) -> Double.compare(b[4], a[4]));
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("town weights map=%d: %d ledges, %d anchors, groundBandY=%.0f, %d pins%n",
                map.getId(), ledges.size(), anchors.size(), groundBandY, ov.pins().size()));
        int limit = Math.min(topN, rows.size());
        for (int i = 0; i < limit; i++) {
            double[] r = rows.get(i);
            double pct = total > 0 ? 100.0 * r[4] / total : 0.0;
            sb.append(String.format("  #%d region=%d x=[%.0f..%.0f] y=%.0f  %.1f%%%n",
                    i + 1, (int) r[0], r[1], r[2], r[3], pct));
        }
        return sb.toString();
    }

    private record Anchor(int x, int y, double strength) {
    }
}
