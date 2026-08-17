package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.server.maps.MapleMap;

import java.awt.Point;
import java.util.List;

/*
 * Immutable analytic movement plan — the shared "where / how long" contract for the LOD layer.
 *
 * It is an ordered list of baked nav-graph edges plus the cumulative traversal time at each edge
 * boundary. Every BotNavigationGraph.Edge.cost is the simulated traversal time in ms
 * (computed once at bake time by running the physics sim), so an unobserved bot's position becomes a
 * pure function of elapsed wall-clock time — no runtime physics integration. This is the
 * linchpin of cheap background movement: the CoarseExecutor "renders the
 * plan with a clock" while the physics path "renders it with a sim", and because they share the same
 * plan they can never disagree about where the bot is or how long the trip takes.
 *
 * Pure data + pure math — no mutable cursor, no side effects — so it is trivially unit-testable
 * with a fake clock.
 */
final class MovementPlan {
    enum Kind {
        IN_MAP,
        CROSS_MAP
    }

    final Kind kind;
    final int mapId;
    final List<BotNavigationGraph.Edge> edges;
    private final long[] cumStartMs; // cumStartMs[i] = elapsed time at the START of edge i
    final long totalTimeMs;

    private MovementPlan(Kind kind, int mapId, List<BotNavigationGraph.Edge> edges,
                         long[] cumStartMs, long totalTimeMs) {
        this.kind = kind;
        this.mapId = mapId;
        this.edges = edges;
        this.cumStartMs = cumStartMs;
        this.totalTimeMs = totalTimeMs;
    }

    /*
     * Build an in-map plan from an A* edge result. Returns null when there is nothing to do
     * (no edges) — the caller treats that as "already there".
     */
    static MovementPlan inMap(int mapId, List<BotNavigationGraph.Edge> edges) {
        if (edges == null || edges.isEmpty()) {
            return null;
        }
        long[] cumStart = new long[edges.size()];
        long acc = 0;
        for (int i = 0; i < edges.size(); i++) {
            cumStart[i] = acc;
            acc += Math.max(0, edges.get(i).cost); // baked traversal time (ms); clamp defensive
        }
        return new MovementPlan(Kind.IN_MAP, mapId, List.copyOf(edges), cumStart, acc);
    }

    /*
     * Plan an in-map route on a live map+graph from startPos to targetPos. Resolves
     * both endpoints to nav regions and runs the same A* the physics path uses, so the analytic and
     * physics routes match. Returns null when either endpoint has no region or no path exists.
     */
    static MovementPlan inMap(BotNavigationGraph graph, MapleMap map, Point startPos, Point targetPos) {
        if (graph == null || map == null || startPos == null || targetPos == null) {
            return null;
        }
        int startRegion = graph.findRegionId(map, startPos);
        int targetRegion = graph.findRegionId(map, targetPos);
        if (startRegion < 0 || targetRegion < 0) {
            return null;
        }
        List<BotNavigationGraph.Edge> edges =
                BotNavigationManager.findPath(graph, map, startPos, startRegion, targetRegion, targetPos);
        return inMap(map.getId(), edges);
    }

    boolean isComplete(long elapsedMs) {
        return elapsedMs >= totalTimeMs;
    }

    /*
     * The interpolated position at elapsedMs, clamped to [0, totalTimeMs]: lerp along
     * whichever edge is in flight. Computed on demand only (lazy) — a coarse bot pays nothing per tick
     * unless something actually asks where it is.
     */
    Point positionAt(long elapsedMs) {
        if (edges.isEmpty()) {
            return null;
        }
        if (elapsedMs <= 0) {
            return new Point(edges.get(0).startPoint);
        }
        if (elapsedMs >= totalTimeMs) {
            return new Point(edges.get(edges.size() - 1).endPoint);
        }
        int i = edgeIndexAt(elapsedMs);
        BotNavigationGraph.Edge e = edges.get(i);
        long into = elapsedMs - cumStartMs[i];
        long dur = Math.max(1, e.cost);
        double t = Math.min(1.0, (double) into / dur);
        int x = (int) Math.round(e.startPoint.x + (e.endPoint.x - e.startPoint.x) * t);
        int y = (int) Math.round(e.startPoint.y + (e.endPoint.y - e.startPoint.y) * t);
        return new Point(x, y);
    }

    /* Index of the edge in flight at elapsedMs (skips zero-duration edges by taking the last match). */
    int edgeIndexAt(long elapsedMs) {
        int idx = 0;
        for (int i = 0; i < edges.size(); i++) {
            if (elapsedMs >= cumStartMs[i]) {
                idx = i;
            } else {
                break;
            }
        }
        return idx;
    }
}
