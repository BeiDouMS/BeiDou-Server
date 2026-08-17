package soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem;

import java.util.*;

/**
 * Manages portal connections between maps.
 * Each connection defines: which portal you enter on the source map,
 * and which portal you spawn at on the destination map.
 *
 * Usage:
 *   PortalConnection pc = new PortalConnection();
 *   pc.register(HENESYS_MAIN, 23, HENESYS_MARKET, 14);
 *   pc.register(HENESYS_MAIN, 24, HENESYS_PARK, 18);
 *   ...
 *
 *   // Later, when you want to warp:
 *   PortalRoute route = pc.getRoute(currentMapId, targetMapId);
 *   // route.sourcePortalId() = 23, route.destPortalId() = 14
 */
public class PortalConnection {

    public record PortalRoute(int sourceMapId, int sourcePortalId, int destMapId, int destPortalId) {
        @Override
        public String toString() {
            return String.format("Map %d [portal %d] -> Map %d [portal %d]",
                    sourceMapId, sourcePortalId, destMapId, destPortalId);
        }
    }

    /**
     * Get a random route from the current map to any directly connected map.
     */
    public PortalRoute getRandomRoute(int currentMapId) {
        Set<Integer> connected = getConnectedMaps(currentMapId);
        if (connected.isEmpty()) return null;
        List<Integer> options = new ArrayList<>(connected);
        int target = options.get(new Random().nextInt(options.size()));
        return getRoute(currentMapId, target);
    }

    // Key: "sourceMapId:destMapId" -> PortalRoute
    private final Map<String, PortalRoute> connections = new HashMap<>();

    // Adjacency list for multi-hop pathfinding later if needed
    private final Map<Integer, Set<Integer>> adjacency = new HashMap<>();

    /**
     * Register a one-way portal connection.
     */
    public void register(int sourceMapId, int sourcePortalId, int destMapId, int destPortalId) {
        String key = makeKey(sourceMapId, destMapId);
        connections.put(key, new PortalRoute(sourceMapId, sourcePortalId, destMapId, destPortalId));
        adjacency.computeIfAbsent(sourceMapId, k -> new HashSet<>()).add(destMapId);
    }

    /**
     * Register a two-way portal connection (most portals work both ways).
     */
    public void registerBidirectional(int mapA, int portalA, int mapB, int portalB) {
        register(mapA, portalA, mapB, portalB);
        register(mapB, portalB, mapA, portalA);
    }

    /**
     * Get the route from one map to a directly connected map.
     * Returns null if no direct connection exists.
     */
    public PortalRoute getRoute(int sourceMapId, int destMapId) {
        return connections.get(makeKey(sourceMapId, destMapId));
    }

    /**
     * Get all maps directly reachable from the given map.
     */
    public Set<Integer> getConnectedMaps(int mapId) {
        return adjacency.getOrDefault(mapId, Collections.emptySet());
    }

    /**
     * Check if two maps are directly connected.
     */
    public boolean isDirectlyConnected(int sourceMapId, int destMapId) {
        return connections.containsKey(makeKey(sourceMapId, destMapId));
    }

    /**
     * Find a multi-hop path between two maps (BFS).
     * Returns ordered list of PortalRoutes to follow, or empty list if unreachable.
     */
    public List<PortalRoute> findPath(int startMapId, int endMapId) {
        if (startMapId == endMapId) return Collections.emptyList();

        // Direct connection - skip BFS
        PortalRoute direct = getRoute(startMapId, endMapId);
        if (direct != null) return List.of(direct);

        // BFS for multi-hop
        Queue<Integer> queue = new LinkedList<>();
        Map<Integer, Integer> cameFrom = new HashMap<>(); // child -> parent map
        queue.add(startMapId);
        cameFrom.put(startMapId, null);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            for (int neighbor : getConnectedMaps(current)) {
                if (!cameFrom.containsKey(neighbor)) {
                    cameFrom.put(neighbor, current);
                    if (neighbor == endMapId) {
                        return reconstructPath(cameFrom, startMapId, endMapId);
                    }
                    queue.add(neighbor);
                }
            }
        }
        return Collections.emptyList(); // No path found
    }

    private List<PortalRoute> reconstructPath(Map<Integer, Integer> cameFrom, int start, int end) {
        List<PortalRoute> path = new ArrayList<>();
        int current = end;
        while (current != start) {
            int parent = cameFrom.get(current);
            path.add(getRoute(parent, current));
            current = parent;
        }
        Collections.reverse(path);
        return path;
    }

    private String makeKey(int sourceMapId, int destMapId) {
        return sourceMapId + ":" + destMapId;
    }
}