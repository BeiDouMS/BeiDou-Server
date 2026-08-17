package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.provider.Data;
import org.gms.provider.DataProvider;
import org.gms.provider.DataProviderFactory;
import org.gms.provider.DataTool;
import org.gms.provider.wz.WZFiles;
import org.gms.server.maps.Portal;
import soloMapling.ArtificialPlayer.BotTravelSystem.BotScriptedWarp;

import static soloMapling.server.MapleVersionManager.isPortalinCurrentVersion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * Portals-only world connectivity graph for GCTravel: which map walkably connects to
 * which. Scanned once from Map.wz — non-door, unscripted portals with a real target map,
 * following info/link exactly like MapFactory so the edge set matches the runtime map.
 *
 * Pure-movement scope: GreenCat's taxi / ferry / return-scroll / scripted-entrance content
 * edges are intentionally dropped. A route that would need one of those returns null from
 * .route and GCTravel warps that hop instead. Built lazily in-memory on first use
 * (~5k XMLs in a few seconds on a worker pool); not disk-cached.
 */
// Ported from GreenCatMS. Credit: NutNNut.
final class GCWorldGraph {
    private GCWorldGraph() {
    }

    private static final int NO_TARGET_MAPID = 999999999; // tm of spawn points / doors
    private static final int[] EMPTY = new int[0];
    private static volatile Map<Integer, int[]> edges;

    static Map<Integer, int[]> get() {
        Map<Integer, int[]> cached = edges;
        if (cached != null) {
            return cached;
        }
        synchronized (GCWorldGraph.class) {
            if (edges == null) {
                edges = build();
            }
            return edges;
        }
    }

    static boolean isReady() {
        return edges != null;
    }

    /*
     * Raw walkable-portal neighbours of mapId (the visually-adjacent maps) — for the LOD halo.
     * Unlike .neighbors this excludes taxi destinations, and unlike .get it never
     * triggers the world-graph build: returns empty until the graph already exists.
     */
    static int[] portalNeighbors(int mapId) {
        Map<Integer, int[]> g = edges; // read the volatile directly — do NOT force a build
        return g == null ? EMPTY : g.getOrDefault(mapId, EMPTY);
    }

    static int mapCount() {
        return get().size();
    }

    /*
     * Shortest portal-hop route from fromMapId to toMapId: the ordered map ids to
     * enter, ending with toMapId. Empty list when already there; null when not
     * reachable by walkable portals within maxHops (caller warps instead).
     */
    static List<Integer> route(int fromMapId, int toMapId, int maxHops) {
        if (fromMapId == toMapId) {
            return List.of();
        }
        if (maxHops <= 0) {
            return null;
        }
        Map<Integer, int[]> g = get();
        Map<Integer, Integer> cameFrom = new HashMap<>();
        ArrayDeque<Integer> frontier = new ArrayDeque<>();
        cameFrom.put(fromMapId, fromMapId);
        frontier.add(fromMapId);
        int depth = 0;
        while (!frontier.isEmpty() && depth < maxHops) {
            depth++;
            for (int level = frontier.size(); level > 0; level--) {
                int current = frontier.poll();
                for (int next : neighbors(g, current)) {
                    if (cameFrom.putIfAbsent(next, current) != null) {
                        continue;
                    }
                    if (next == toMapId) {
                        return reconstruct(cameFrom, fromMapId, toMapId);
                    }
                    frontier.add(next);
                }
            }
        }
        return null;
    }

    /* Walkable-portal neighbours plus taxi (cab) destinations and curated scripted warps (e.g. the
     * subway entrance), so town↔town and scripted-only training routes are routable. */
    private static int[] neighbors(Map<Integer, int[]> g, int mapId) {
        int[] portals = g.getOrDefault(mapId, EMPTY);
        int[] taxi = GCTaxi.destinations(mapId);
        int[] warp = BotScriptedWarp.destinations(mapId);
        if (taxi.length == 0 && warp.length == 0) {
            return portals;
        }
        int[] all = new int[portals.length + taxi.length + warp.length];
        System.arraycopy(portals, 0, all, 0, portals.length);
        System.arraycopy(taxi, 0, all, portals.length, taxi.length);
        System.arraycopy(warp, 0, all, portals.length + taxi.length, warp.length);
        return all;
    }

    private static List<Integer> reconstruct(Map<Integer, Integer> cameFrom, int from, int to) {
        List<Integer> hops = new ArrayList<>();
        for (int at = to; at != from; at = cameFrom.get(at)) {
            hops.add(at);
        }
        Collections.reverse(hops);
        return List.copyOf(hops);
    }

    private static Map<Integer, int[]> build() {
        Map<Integer, int[]> out = new ConcurrentHashMap<>();
        ThreadLocal<DataProvider> mapSources =
                ThreadLocal.withInitial(() -> DataProviderFactory.getDataProvider(WZFiles.MAP));
        ExecutorService pool = Executors.newFixedThreadPool(
                Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors())));
        Path mapRoot = Path.of(WZFiles.MAP.getFilePath(), "Map");
        for (int area = 0; area <= 9; area++) {
            Path areaDir = mapRoot.resolve("Map" + area);
            if (!Files.isDirectory(areaDir)) {
                continue;
            }
            try (var files = Files.list(areaDir)) {
                for (Path file : (Iterable<Path>) files::iterator) {
                    String name = file.getFileName().toString();
                    if (!name.endsWith(".img.xml")) {
                        continue;
                    }
                    final int fileArea = area;
                    try {
                        int mapId = Integer.parseInt(name.substring(0, name.length() - ".img.xml".length()));
                        pool.execute(() -> {
                            try {
                                readMap(mapSources.get(), fileArea, mapId, out);
                            } catch (RuntimeException ignored) {
                                // skip an unparseable map file
                            }
                        });
                    } catch (NumberFormatException ignored) {
                        // non-map file (e.g. AreaCode.img.xml)
                    }
                }
            } catch (IOException ignored) {
                // can't list an area dir — skip it
            }
        }
        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Collections.unmodifiableMap(out);
    }

    private static void readMap(DataProvider mapSource, int area, int mapId, Map<Integer, int[]> out) {
        Data mapData = mapSource.getData(mapImgPath(area, mapId));
        if (mapData == null) {
            return;
        }
        Data info = mapData.getChildByPath("info");
        String link = info != null ? DataTool.getString("link", info, "") : "";
        if (!link.isEmpty()) {
            try {
                int linkId = Integer.parseInt(link);
                mapData = mapSource.getData(mapImgPath(linkId / 100000000, linkId));
                if (mapData == null) {
                    out.put(mapId, EMPTY);
                    return;
                }
            } catch (NumberFormatException ignored) {
                // malformed link — read the map as-is
            }
        }
        Data portals = mapData.getChildByPath("portal");
        if (portals == null) {
            out.put(mapId, EMPTY);
            return;
        }
        Set<Integer> targets = new TreeSet<>();
        for (Data portal : portals) {
            int targetMapId = DataTool.getInt("tm", portal, NO_TARGET_MAPID);
            if (targetMapId == NO_TARGET_MAPID || targetMapId == mapId) {
                continue;
            }
            if (DataTool.getInt("pt", portal, 0) == Portal.DOOR_PORTAL) {
                continue;
            }
            String script = DataTool.getString("script", portal, "");
            if (!script.isEmpty()) {
                continue; // scripted portals execute their own warp — out of pure-movement scope
            }
            if (!isPortalinCurrentVersion(targetMapId)) {
                continue; // destination is gated out of the current server version (e.g. Nautilus on v55) —
                          // real players are blocked at the portal, so bots must not path there either
            }
            targets.add(targetMapId);
        }
        int[] arr = new int[targets.size()];
        int i = 0;
        for (int t : targets) {
            arr[i++] = t;
        }
        out.put(mapId, arr);
    }

    private static String mapImgPath(int area, int mapId) {
        return "Map/Map" + area + "/" + String.format("%09d", mapId) + ".img";
    }
}
