package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.server.life.NPC;
import org.gms.server.maps.MapleMap;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static soloMapling.server.MapleVersionManager.isPortalinCurrentVersion;

/*
 * Basic taxi connectivity: the Victoria Island town cab network. Town↔town has no walkable portal
 * path, so GCTravel can't reach e.g. Henesys→Kerning by walking. This adds those edges (so routing
 * finds them) and the executor rides them by walking to the cab NPC, then warping to the
 * destination town — "stand near the cab, ride." No fares/economy (pure-movement scope).
 *
 * Cab NPC ids mirror the Victoria cab scripts. Each town's cab connects to the other five.
 */
// Ported from GreenCatMS. Credit: NutNNut.
final class GCTaxi {
    private GCTaxi() {
    }

    /* A cab ride: stand near npcId on fromMapId, ride to toMapId. */
    record TaxiEdge(int fromMapId, int npcId, int toMapId) {
    }

    // {townMapId, cabNpcId} — the fully-connected Victoria Island cab network.
    private static final int[][] VICTORIA_CABS = {
            {104000000, 1002007}, // Lith Harbor
            {100000000, 1012000}, // Henesys
            {102000000, 1022001}, // Perion
            {101000000, 1032000}, // Ellinia
            {103000000, 1052016}, // Kerning City
            {120000000, 1092014}, // Nautilus Harbor
    };

    private static final Map<Integer, List<TaxiEdge>> BY_FROM = buildEdges();

    private static Map<Integer, List<TaxiEdge>> buildEdges() {
        Map<Integer, List<TaxiEdge>> byFrom = new HashMap<>();
        for (int[] from : VICTORIA_CABS) {
            if (!isPortalinCurrentVersion(from[0])) {
                continue; // never ride from a town gated out of this version
            }
            List<TaxiEdge> edges = new ArrayList<>();
            for (int[] to : VICTORIA_CABS) {
                // skip self, and any destination town gated out of the current server version
                if (from[0] != to[0] && isPortalinCurrentVersion(to[0])) {
                    edges.add(new TaxiEdge(from[0], from[1], to[0]));
                }
            }
            byFrom.put(from[0], List.copyOf(edges));
        }
        return Map.copyOf(byFrom);
    }

    static List<TaxiEdge> from(int mapId) {
        return BY_FROM.getOrDefault(mapId, List.of());
    }

    /* The cab edge from one town to another, or null if no cab drives that route. */
    static TaxiEdge edge(int fromMapId, int toMapId) {
        for (TaxiEdge e : from(fromMapId)) {
            if (e.toMapId() == toMapId) {
                return e;
            }
        }
        return null;
    }

    /* All taxi destination map ids reachable from mapId (for world-graph connectivity). */
    static int[] destinations(int mapId) {
        List<TaxiEdge> edges = from(mapId);
        int[] dests = new int[edges.size()];
        for (int i = 0; i < edges.size(); i++) {
            dests[i] = edges.get(i).toMapId();
        }
        return dests;
    }

    /* Live position of the cab NPC on map, or null if it isn't present/loaded. */
    static Point npcPos(MapleMap map, int npcId) {
        if (map == null) {
            return null;
        }
        NPC npc = map.getNPCById(npcId);
        return npc == null ? null : npc.getPosition();
    }
}
