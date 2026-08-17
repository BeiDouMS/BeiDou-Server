package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.server.maps.Foothold;
import org.gms.server.maps.MapleMap;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Ported from GreenCatMS. Credit: NutNNut.
final class BotNavigationGraph implements Serializable {
    // Cached nav graphs are serialized to disk. Keep explicit serialVersionUIDs so
    // harmless method-only edits do not break cache loading; use GRAPH_VERSION for
    // intentional cache invalidation when the serialized data shape changes.
    @Serial
    private static final long serialVersionUID = 1L;

    enum EdgeType {
        WALK,
        JUMP,
        DROP,
        CLIMB,
        PORTAL
    }

    static final class Segment implements Serializable {
        // Part of the on-disk BotNavigationGraph cache schema; do not remove.
        @Serial
        private static final long serialVersionUID = 1L;

        final int footholdId;
        final int x1;
        final int y1;
        final int x2;
        final int y2;
        final int minX;
        final int maxX;
        final boolean forbidFallDown;
        final boolean collidableFromBelow;

        Segment(Foothold foothold) {
            this(foothold, false);
        }

        Segment(Foothold foothold, boolean collidableFromBelow) {
            this.footholdId = foothold.getId();
            this.x1 = foothold.getX1();
            this.y1 = foothold.getY1();
            this.x2 = foothold.getX2();
            this.y2 = foothold.getY2();
            this.minX = Math.min(x1, x2);
            this.maxX = Math.max(x1, x2);
            this.forbidFallDown = foothold.isForbidFallDown();
            this.collidableFromBelow = collidableFromBelow;
        }

        boolean containsX(int x) {
            return x >= minX && x <= maxX;
        }

        int clampX(int x) {
            if (x < minX) {
                return minX;
            }
            if (x > maxX) {
                return maxX;
            }
            return x;
        }

        Point pointAt(int x) {
            int clampedX = clampX(x);
            if (x1 == x2) {
                return new Point(clampedX, Math.min(y1, y2));
            }

            double ratio = (clampedX - x1) / (double) (x2 - x1);
            int y = (int) Math.round(y1 + (y2 - y1) * ratio);
            return new Point(clampedX, y);
        }
    }

    static final class Region implements Serializable {
        // Part of the on-disk BotNavigationGraph cache schema; do not remove.
        @Serial
        private static final long serialVersionUID = 1L;

        final int id;
        final List<Segment> segments;
        final int minX;
        final int maxX;
        final int minY;
        final int maxY;
        final boolean isRopeRegion;
        final boolean isLadder;

        Region(int id, List<Segment> segments) {
            if (segments.isEmpty()) {
                throw new IllegalArgumentException("Bot nav region requires at least one segment");
            }

            this.id = id;
            this.segments = new ArrayList<>(segments);
            this.isRopeRegion = false;
            this.isLadder = false;

            int regionMinX = Integer.MAX_VALUE;
            int regionMaxX = Integer.MIN_VALUE;
            int regionMinY = Integer.MAX_VALUE;
            int regionMaxY = Integer.MIN_VALUE;
            for (Segment segment : segments) {
                regionMinX = Math.min(regionMinX, segment.minX);
                regionMaxX = Math.max(regionMaxX, segment.maxX);
                regionMinY = Math.min(regionMinY, Math.min(segment.y1, segment.y2));
                regionMaxY = Math.max(regionMaxY, Math.max(segment.y1, segment.y2));
            }

            this.minX = regionMinX;
            this.maxX = regionMaxX;
            this.minY = regionMinY;
            this.maxY = regionMaxY;
        }

        Region(int id, int ropeX, int topY, int bottomY, boolean isLadder) {
            this.id = id;
            this.segments = List.of();
            this.isRopeRegion = true;
            this.isLadder = isLadder;
            this.minX = ropeX;
            this.maxX = ropeX;
            this.minY = topY;
            this.maxY = bottomY;
        }

        int width() {
            return Math.max(0, maxX - minX);
        }

        int height() {
            return Math.max(0, maxY - minY);
        }

        Point leftPoint() {
            return pointAt(minX);
        }

        Point centerPoint() {
            if (isRopeRegion) {
                return new Point(minX, minY + height() / 2);
            }
            return pointAt(minX + width() / 2);
        }

        Point rightPoint() {
            return pointAt(maxX);
        }

        Point pointAt(int x) {
            if (isRopeRegion) {
                return new Point(minX, minY + height() / 2);
            }
            Segment bestSegment = findBestSegment(x);
            return bestSegment.pointAt(x);
        }

        boolean isForbidFallDownAt(int x) {
            if (isRopeRegion || segments.isEmpty()) {
                return false;
            }
            return findBestSegment(x).forbidFallDown;
        }

        private Segment findBestSegment(int x) {
            Segment best = segments.get(0);
            int bestDistance = distanceToSegment(best, x);
            for (int i = 1; i < segments.size(); i++) {
                Segment segment = segments.get(i);
                int distance = distanceToSegment(segment, x);
                if (distance < bestDistance) {
                    best = segment;
                    bestDistance = distance;
                }
            }
            return best;
        }

        private int distanceToSegment(Segment segment, int x) {
            if (segment.containsX(x)) {
                return 0;
            }
            return x < segment.minX ? segment.minX - x : x - segment.maxX;
        }
    }

    static final class Edge implements Serializable {
        // Part of the on-disk BotNavigationGraph cache schema; do not remove.
        @Serial
        private static final long serialVersionUID = 1L;

        final int fromRegionId;
        final int toRegionId;
        final EdgeType type;
        final Point startPoint;
        final Point endPoint;
        final int launchMinX;
        final int launchMaxX;
        final int launchStepX;
        final int portalId;
        final int ropeX;
        final int ropeTopY;
        final int ropeBottomY;
        final int cost;

        Edge(int fromRegionId,
             int toRegionId,
             EdgeType type,
             Point startPoint,
             Point endPoint,
             int launchMinX,
             int launchMaxX,
             int launchStepX,
             int portalId,
             int ropeX,
             int ropeTopY,
             int ropeBottomY,
             int cost) {
            this.fromRegionId = fromRegionId;
            this.toRegionId = toRegionId;
            this.type = type;
            this.startPoint = new Point(startPoint);
            this.endPoint = new Point(endPoint);
            this.launchMinX = Math.min(launchMinX, launchMaxX);
            this.launchMaxX = Math.max(launchMinX, launchMaxX);
            this.launchStepX = launchStepX;
            this.portalId = portalId;
            this.ropeX = ropeX;
            this.ropeTopY = ropeTopY;
            this.ropeBottomY = ropeBottomY;
            this.cost = cost;
        }

        Edge(int fromRegionId,
             int toRegionId,
             EdgeType type,
             Point startPoint,
             Point endPoint,
             int launchStepX,
             int portalId,
             int ropeX,
             int ropeTopY,
             int ropeBottomY,
             int cost) {
            this(fromRegionId, toRegionId, type, startPoint, endPoint,
                    startPoint.x, startPoint.x, launchStepX, portalId, ropeX, ropeTopY, ropeBottomY, cost);
        }

        boolean containsLaunchX(int x) {
            return x >= launchMinX && x <= launchMaxX;
        }

        boolean containsLaunchX(int x, int tolerance) {
            return x >= launchMinX - tolerance && x <= launchMaxX + tolerance;
        }

        /* Launch point at the in-window x nearest to x (the x execution would actually fire from). */
        Point pointAtNearestLaunchX(int x) {
            return new Point(Math.clamp(x, launchMinX, launchMaxX), startPoint.y);
        }
    }

    final int mapId;
    final int version;
    final BotMovementProfile movementProfile;
    final List<Region> regions;
    final Map<Integer, Region> regionsById;
    final Map<Integer, Integer> regionIdByFootholdId;
    final Map<Integer, List<Edge>> outgoingByRegionId;
    final java.util.Set<Integer> collidableWallIds;
    final java.util.Set<Integer> collidableFromBelowIds;

    BotNavigationGraph(int mapId,
                       int version,
                       BotMovementProfile movementProfile,
                       List<Region> regions,
                       Map<Integer, Region> regionsById,
                       Map<Integer, Integer> regionIdByFootholdId,
                       Map<Integer, List<Edge>> outgoingByRegionId,
                       java.util.Set<Integer> collidableWallIds) {
        this(mapId, version, movementProfile, regions, regionsById, regionIdByFootholdId, outgoingByRegionId, collidableWallIds, java.util.Set.of());
    }

    BotNavigationGraph(int mapId,
                       int version,
                       BotMovementProfile movementProfile,
                       List<Region> regions,
                       Map<Integer, Region> regionsById,
                       Map<Integer, Integer> regionIdByFootholdId,
                       Map<Integer, List<Edge>> outgoingByRegionId,
                       java.util.Set<Integer> collidableWallIds,
                       java.util.Set<Integer> collidableFromBelowIds) {
        this.mapId = mapId;
        this.version = version;
        this.movementProfile = movementProfile;
        this.regions = new ArrayList<>(regions);
        this.regionsById = new HashMap<>(regionsById);
        this.regionIdByFootholdId = new HashMap<>(regionIdByFootholdId);
        this.outgoingByRegionId = new HashMap<>();
        for (Map.Entry<Integer, List<Edge>> entry : outgoingByRegionId.entrySet()) {
            this.outgoingByRegionId.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        this.collidableWallIds = new java.util.HashSet<>(collidableWallIds);
        this.collidableFromBelowIds = new java.util.HashSet<>(collidableFromBelowIds);
    }

    BotNavigationGraph(int mapId,
                       int version,
                       List<Region> regions,
                       Map<Integer, Region> regionsById,
                       Map<Integer, Integer> regionIdByFootholdId,
                       Map<Integer, List<Edge>> outgoingByRegionId,
                       java.util.Set<Integer> collidableWallIds) {
        this(mapId, version, BotMovementProfile.base(), regions, regionsById, regionIdByFootholdId, outgoingByRegionId, collidableWallIds, java.util.Set.of());
    }

    BotNavigationGraph(int mapId,
                       int version,
                       List<Region> regions,
                       Map<Integer, Region> regionsById,
                       Map<Integer, Integer> regionIdByFootholdId,
                       Map<Integer, List<Edge>> outgoingByRegionId,
                       java.util.Set<Integer> collidableWallIds,
                       java.util.Set<Integer> collidableFromBelowIds) {
        this(mapId, version, BotMovementProfile.base(), regions, regionsById, regionIdByFootholdId, outgoingByRegionId, collidableWallIds, collidableFromBelowIds);
    }

    Region getRegion(int regionId) {
        return regionsById.get(regionId);
    }

    List<Edge> getOutgoing(int regionId) {
        return outgoingByRegionId.getOrDefault(regionId, List.of());
    }

    boolean hasInterRegionEdge(int fromRegionId, int toRegionId) {
        for (Edge edge : getOutgoing(fromRegionId)) {
            if (edge.fromRegionId != edge.toRegionId && edge.toRegionId == toRegionId) {
                return true;
            }
        }
        return false;
    }

    Set<Integer> getMutualAdjacentRegionIds(int regionId) {
        Set<Integer> adjacent = new HashSet<>();
        for (Edge edge : getOutgoing(regionId)) {
            if (edge.fromRegionId == edge.toRegionId) {
                continue;
            }
            if (hasInterRegionEdge(edge.toRegionId, regionId)) {
                adjacent.add(edge.toRegionId);
            }
        }
        return adjacent;
    }

    int findRegionId(MapleMap map, Point position) {
        if (position == null || map.getFootholds() == null) {
            return -1;
        }

        Foothold foothold = BotPhysicsEngine.findGroundFoothold(map, position);
        if (foothold != null) {
            int regionId = regionIdByFootholdId.getOrDefault(foothold.getId(), -1);
            if (regionId >= 0) {
                return regionId;
            }
        }

        return findRopeRegionId(position);
    }

    int findRopeRegionId(Point position) {
        for (Region region : regions) {
            if (!region.isRopeRegion) {
                continue;
            }
            if (Math.abs(position.x - region.minX) <= BotPhysicsEngine.cfg.ROPE_GRAB_X
                    && position.y >= region.minY
                    && position.y <= region.maxY) {
                return region.id;
            }
        }
        return -1;
    }
}
