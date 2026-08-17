package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.provider.Data;
import org.gms.provider.DataProvider;
import org.gms.provider.DataProviderFactory;
import org.gms.provider.DataTool;
import org.gms.provider.wz.WZFiles;
import org.gms.server.life.LifeFactory;
import org.gms.server.life.Monster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Cached map -> representative (median) mob level + mob count, read straight from WZ: Map.wz `life`
// entries of type "m" give the mob ids (following info/link like MapFactory), and each mob's level
// comes from its MonsterStats. Lazy per map, cached forever after. Deterministic - no recordings, no
// config. Maps with no mobs (towns) report level -1 and are naturally skipped by callers.
//
// Our own creation. Lets a TrainingBot discover level-appropriate nearby maps without a hand-authored
// table.
public final class MapMobIndex {

    private MapMobIndex() {
    }

    // A static spawn point: raw WZ position (x, cy) plus the ledge key of the foothold it sits on —
    // derived from the WZ foothold tree's layer/group structure (-1 when the fh id is unknown). Not the
    // nav graph's region id, but the same physical grouping, so the capacity estimator can split spots
    // per ledge the way the live profile does without loading the map.
    public record SpawnPos(int x, int y, int fhGroup) {
    }

    public record MapMobInfo(int medianLevel, int mobCount, List<Integer> mobIds, List<SpawnPos> spawnPoints) {
        static final MapMobInfo NONE = new MapMobInfo(-1, 0, List.of(), List.of());
    }

    private static final ThreadLocal<DataProvider> MAP_SOURCE =
            ThreadLocal.withInitial(() -> DataProviderFactory.getDataProvider(WZFiles.MAP));
    private static final Map<Integer, MapMobInfo> CACHE = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> MOB_LEVEL = new ConcurrentHashMap<>();

    // Representative (median) mob level of a map, or -1 if it has no mobs (towns, etc.).
    public static int level(int mapId) {
        return info(mapId).medianLevel();
    }

    // Number of mob spawn entries on a map.
    public static int mobCount(int mapId) {
        return info(mapId).mobCount();
    }

    // Mob ids the map is defined to spawn (from WZ life data), or empty for mobless maps (towns).
    public static List<Integer> mobIds(int mapId) {
        return info(mapId).mobIds();
    }

    // Raw WZ spawn points (x, cy, foothold-group ledge key). Static data straight off the map img — no
    // live map load, no nav bake — so DECIDE can estimate a map's claimable-spot count before any bot
    // ever grinds it.
    public static List<SpawnPos> spawnPoints(int mapId) {
        return info(mapId).spawnPoints();
    }

    public static MapMobInfo info(int mapId) {
        return CACHE.computeIfAbsent(mapId, MapMobIndex::compute);
    }

    private static MapMobInfo compute(int mapId) {
        try {
            Data mapData = loadMapData(mapId);
            if (mapData == null) {
                return MapMobInfo.NONE;
            }
            Data life = mapData.getChildByPath("life");
            if (life == null) {
                return MapMobInfo.NONE;
            }
            Map<Integer, Integer> fhGroups = footholdGroups(mapData);
            List<Integer> levels = new ArrayList<>();
            List<Integer> mobIds = new ArrayList<>();
            List<SpawnPos> positions = new ArrayList<>();
            for (Data entry : life) {
                if (!"m".equals(DataTool.getString("type", entry, ""))) {
                    continue;
                }
                String idStr = DataTool.getString("id", entry, "");
                if (idStr.isEmpty()) {
                    continue;
                }
                int mobId;
                try {
                    mobId = Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    continue;
                }
                int lvl = mobLevel(mobId);
                if (lvl > 0) {
                    levels.add(lvl);
                    mobIds.add(mobId);
                    // x + cy is what MapFactory feeds calcPointBelow for the live spawn point; close
                    // enough for cluster counting without loading the map.
                    int x = DataTool.getInt("x", entry, 0);
                    int cy = DataTool.getInt("cy", entry, DataTool.getInt("y", entry, 0));
                    int fh = DataTool.getInt("fh", entry, -1);
                    positions.add(new SpawnPos(x, cy, fhGroups.getOrDefault(fh, -1)));
                }
            }
            if (levels.isEmpty()) {
                return MapMobInfo.NONE;
            }
            Collections.sort(levels);
            return new MapMobInfo(levels.get(levels.size() / 2), levels.size(), mobIds, positions);
        } catch (RuntimeException e) {
            return MapMobInfo.NONE;
        }
    }

    // fh id -> a per-map ledge key from the WZ foothold tree (one key per layer/group node). Footholds in
    // the same group form one connected walkable run, so this mirrors the nav graph's ledge notion closely
    // enough for the estimator's per-ledge spot split.
    private static Map<Integer, Integer> footholdGroups(Data mapData) {
        Map<Integer, Integer> out = new HashMap<>();
        Data root = mapData.getChildByPath("foothold");
        if (root == null) {
            return out;
        }
        int key = 0;
        for (Data layer : root) {
            for (Data group : layer) {
                key++;
                for (Data f : group) {
                    try {
                        out.put(Integer.parseInt(f.getName()), key);
                    } catch (NumberFormatException ignored) {
                        // non-numeric foothold node — skip
                    }
                }
            }
        }
        return out;
    }

    private static Data loadMapData(int mapId) {
        DataProvider src = MAP_SOURCE.get();
        Data mapData = src.getData(mapImgPath(mapId));
        if (mapData == null) {
            return null;
        }
        Data info = mapData.getChildByPath("info");
        String link = info != null ? DataTool.getString("link", info, "") : "";
        if (!link.isEmpty()) {
            try {
                Data linked = src.getData(mapImgPath(Integer.parseInt(link)));
                if (linked != null) {
                    return linked;
                }
            } catch (NumberFormatException ignored) {
                // malformed link — use the map as-is
            }
        }
        return mapData;
    }

    private static int mobLevel(int mobId) {
        return MOB_LEVEL.computeIfAbsent(mobId, id -> {
            try {
                Monster m = LifeFactory.getMonster(id);
                return (m == null || m.getStats() == null) ? -1 : m.getStats().getLevel();
            } catch (RuntimeException e) {
                return -1;
            }
        });
    }

    private static String mapImgPath(int mapId) {
        return "Map/Map" + (mapId / 100000000) + "/" + String.format("%09d", mapId) + ".img";
    }
}
