package soloMapling.ArtificialPlayer.BotTravelSystem;

import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Portal;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static soloMapling.server.MapleVersionManager.isPortalinCurrentVersion;

// Curated table of scripted "warp" portals the pure-movement world graph can't see. The graph keeps only
// non-scripted portals, so a map reachable ONLY through a scripted portal (e.g. the Kerning subway
// entrance, subway_in2.js -> warp 103000101) would be isolated and bots would never train there. These
// edges add it back: the travel executor walks the bot to the portal then changeMap()s to the destination,
// like the script's pi.warp. Seeded with what bots need; version-gated destinations are dropped.
// Our own creation (not a GreenCatMS port).
public final class BotScriptedWarp {
    private BotScriptedWarp() {
    }

    // A scripted-portal hop: stand on portalName on fromMapId, warp to toMapId landing at toPortalId.
    public record WarpEdge(int fromMapId, String portalName, int toMapId, int toPortalId) {
    }

    private static final WarpEdge[] EDGES = {
            // Kerning subway: the booth's "in00" portal is scripted (subway_in2.js -> Line 1 <Area 1>,
            // portal 3). The way back (103000101 "out00" -> booth) is a plain portal already in the graph,
            // so this one inbound edge reconnects the whole subway.
            new WarpEdge(103000100, "in00", 103000101, 3),
    };

    private static final Map<Integer, List<WarpEdge>> BY_FROM = buildEdges();

    private static Map<Integer, List<WarpEdge>> buildEdges() {
        Map<Integer, List<WarpEdge>> byFrom = new HashMap<>();
        for (WarpEdge e : EDGES) {
            if (!isPortalinCurrentVersion(e.toMapId())) {
                continue; // destination gated out of this version — don't route bots there
            }
            byFrom.computeIfAbsent(e.fromMapId(), k -> new ArrayList<>()).add(e);
        }
        Map<Integer, List<WarpEdge>> immutable = new HashMap<>();
        for (Map.Entry<Integer, List<WarpEdge>> en : byFrom.entrySet()) {
            immutable.put(en.getKey(), List.copyOf(en.getValue()));
        }
        return Map.copyOf(immutable);
    }

    public static List<WarpEdge> from(int mapId) {
        return BY_FROM.getOrDefault(mapId, List.of());
    }

    // The scripted-warp edge from one map to another, or null if none.
    public static WarpEdge edge(int fromMapId, int toMapId) {
        for (WarpEdge e : from(fromMapId)) {
            if (e.toMapId() == toMapId) {
                return e;
            }
        }
        return null;
    }

    // All scripted-warp destination map ids reachable from mapId (for world-graph connectivity).
    public static int[] destinations(int mapId) {
        List<WarpEdge> edges = from(mapId);
        int[] dests = new int[edges.size()];
        for (int i = 0; i < edges.size(); i++) {
            dests[i] = edges.get(i).toMapId();
        }
        return dests;
    }

    // Live position of the trigger portal on map, or null if it isn't present/loaded.
    public static Point portalPos(MapleMap map, String portalName) {
        if (map == null) {
            return null;
        }
        Portal p = map.getPortal(portalName);
        return p == null ? null : p.getPosition();
    }
}
