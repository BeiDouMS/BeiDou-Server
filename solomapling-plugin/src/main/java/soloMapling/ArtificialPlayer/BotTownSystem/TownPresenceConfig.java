package soloMapling.ArtificialPlayer.BotTownSystem;

import soloMapling.Environment.EnvironmentPopulationConfig;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ambient town population plan. Town lists live in {@code EnvironmentPopulation.yaml}
 * under {@code waves.town_presence.towns} (formerly a separate {@code TownPresence.yaml}).
 *
 * <p>Scalars from YamlBeans may be strings, so ints are parsed defensively.
 */
public final class TownPresenceConfig {

    private TownPresenceConfig() {
    }

    /** @deprecated Kept for docs/search; data is in EnvironmentPopulation.yaml. */
    @Deprecated
    private static final String LEGACY_YAML_PATH =
            "src/main/java/soloMapling/ArtificialPlayer/BotTownSystem/TownPresence.yaml";

    // One map within a town's family: how many ambient bots stand there, plus its curation overrides
    // (ban/boost zones + pins, hand-authored in the YAML and merged with any marked pins from the sidecar).
    public record MapShare(int mapId, int count, TownOverrides overrides) {
    }

    // One town's ambient plan. `wanderers` is the town-level count of roaming TownWandererBots (they drift
    // the town's map family), separate from the per-map stationed-SocialBot counts in `maps`.
    public record TownEntry(String name, int levelLo, int levelHi, int wanderers, List<MapShare> maps,
                            String dialogueOverride) {
        // The town's main map (first listed) - where roaming wanderers are seeded before they fan out.
        public int mainMapId() {
            return maps.isEmpty() ? -1 : maps.get(0).mapId();
        }
    }

    private static volatile List<TownEntry> cached;

    /** Drop cached towns so the next {@link #towns()} re-parses (called from population reload). */
    public static void invalidate() {
        cached = null;
    }

    // Parsed town entries (cached after first load). Returns an empty list on any parse/IO failure so a
    // bad edit degrades to "no town presence" rather than crashing world startup.
    public static List<TownEntry> towns() {
        List<TownEntry> local = cached;
        if (local == null) {
            local = load();
            cached = local;
        }
        return local;
    }

    // Force a re-read from EnvironmentPopulation.yaml (used by !env townpresence / population reload).
    public static List<TownEntry> reload() {
        EnvironmentPopulationConfig.reload();
        cached = loadFromRaw(EnvironmentPopulationConfig.rawTownsList());
        return cached;
    }

    // Every town map id in the ambient plan (main town map plus any interior sub-maps), across all towns.
    // The town-wide ambient-social managers (ConversationManager / SocialHotPotatoManager) union this with
    // their Henesys map ids to build the scope they animate; they rebuild on !env townpresence reload.
    public static Set<Integer> allTownMapIds() {
        Set<Integer> ids = new LinkedHashSet<>();
        for (TownEntry t : towns()) {
            for (MapShare m : t.maps()) {
                ids.add(m.mapId());
            }
        }
        return ids;
    }

    private static List<TownEntry> load() {
        // Ensure population YAML is loaded (sets rawTownsList).
        EnvironmentPopulationConfig.plan();
        List<?> raw = EnvironmentPopulationConfig.rawTownsList();
        if (raw.isEmpty()) {
            System.out.println("[TownPresenceConfig] no waves.town_presence.towns in EnvironmentPopulation.yaml"
                    + " (legacy path " + LEGACY_YAML_PATH + " is no longer read)");
        }
        return loadFromRaw(raw);
    }

    @SuppressWarnings("unchecked")
    private static List<TownEntry> loadFromRaw(List<?> townList) {
        List<TownEntry> out = new ArrayList<>();
        try {
            Map<Integer, List<Point>> sidecarPins = TownPinsStore.load();
            for (Object t : townList) {
                if (!(t instanceof Map<?, ?> town)) {
                    continue;
                }
                String name = str(town.get("name"), "town");
                int lo = toInt(town.get("level_lo"), 10);
                int hi = toInt(town.get("level_hi"), lo);
                int wanderers = toInt(town.get("wanderers"), 0);
                String dialogue = str(town.get("dialogue"), null);
                List<MapShare> shares = new ArrayList<>();
                Object mapsNode = town.get("maps");
                if (mapsNode instanceof List<?> mapList) {
                    for (Object m : mapList) {
                        if (!(m instanceof Map<?, ?> mm)) {
                            continue;
                        }
                        int mapId = toInt(mm.get("map"), -1);
                        int count = toInt(mm.get("count"), 0);
                        if (mapId > 0 && count > 0) {
                            shares.add(new MapShare(mapId, count,
                                    parseOverrides(mm, sidecarPins.getOrDefault(mapId, List.of()))));
                        }
                    }
                }
                if (!shares.isEmpty()) {
                    out.add(new TownEntry(name, lo, hi, wanderers, shares, dialogue));
                }
            }
        } catch (Exception e) {
            System.out.println("[TownPresenceConfig] failed to parse towns: " + e.getMessage());
            return new ArrayList<>();
        }
        return out;
    }

    // The curation overrides for a map, whether or not the map is listed in a town (so ad-hoc !env spawns
    // on any map still honor marked pins). Merges YAML ban/boost/pins with the sidecar's marked pins.
    public static TownOverrides overridesFor(int mapId) {
        for (TownEntry t : towns()) {
            for (MapShare m : t.maps()) {
                if (m.mapId() == mapId) {
                    return m.overrides();
                }
            }
        }
        List<Point> sidecar = TownPinsStore.forMap(mapId);
        return sidecar.isEmpty() ? TownOverrides.EMPTY : new TownOverrides(List.of(), List.of(), sidecar);
    }

    // Build a map's overrides from its YAML block (ban/boost rects + pins) merged with the already-loaded
    // sidecar pins for that map. Y bounds on a zone are optional (full-height band when absent).
    private static TownOverrides parseOverrides(Map<?, ?> mm, List<Point> sidecarPins) {
        List<TownOverrides.Zone> ban = parseZones(mm.get("ban"), 1.0);
        List<TownOverrides.Zone> boost = parseZones(mm.get("boost"), 2.0);
        List<Point> pins = new ArrayList<>();
        Object pinsNode = mm.get("pins");
        if (pinsNode instanceof List<?> pinList) {
            for (Object p : pinList) {
                if (p instanceof Map<?, ?> pm) {
                    int x = toInt(pm.get("x"), Integer.MIN_VALUE);
                    int y = toInt(pm.get("y"), Integer.MIN_VALUE);
                    if (x != Integer.MIN_VALUE && y != Integer.MIN_VALUE) {
                        pins.add(new Point(x, y));
                    }
                }
            }
        }
        pins.addAll(sidecarPins); // merge marked pins
        if (ban.isEmpty() && boost.isEmpty() && pins.isEmpty()) {
            return TownOverrides.EMPTY;
        }
        return new TownOverrides(ban, boost, pins);
    }

    private static List<TownOverrides.Zone> parseZones(Object node, double defaultMult) {
        List<TownOverrides.Zone> out = new ArrayList<>();
        if (!(node instanceof List<?> list)) {
            return out;
        }
        for (Object z : list) {
            if (!(z instanceof Map<?, ?> zm)) {
                continue;
            }
            int x1 = toInt(zm.get("x1"), Integer.MIN_VALUE);
            int x2 = toInt(zm.get("x2"), Integer.MAX_VALUE);
            int y1 = toInt(zm.get("y1"), Integer.MIN_VALUE);
            int y2 = toInt(zm.get("y2"), Integer.MAX_VALUE);
            double mult = toDouble(zm.get("mult"), defaultMult);
            out.add(new TownOverrides.Zone(x1, y1, x2, y2, mult));
        }
        return out;
    }

    private static double toDouble(Object o, double fallback) {
        if (o instanceof Number n) {
            return n.doubleValue();
        }
        if (o != null) {
            try {
                return Double.parseDouble(o.toString().trim());
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return fallback;
    }

    private static int toInt(Object o, int fallback) {
        if (o instanceof Number n) {
            return n.intValue();
        }
        if (o != null) {
            try {
                return Integer.parseInt(o.toString().trim());
            } catch (NumberFormatException ignored) {
                // fall through to fallback
            }
        }
        return fallback;
    }

    private static String str(Object o, String fallback) {
        return o != null ? o.toString() : fallback;
    }
}
