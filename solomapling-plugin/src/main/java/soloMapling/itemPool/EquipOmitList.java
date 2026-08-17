package soloMapling.itemPool;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central blocklist of equip item ids that bots must never auto-equip at spawn,
 * even though they are perfectly valid equips in WZ (correct reqLevel, reqJob
 * and gender). The motivating case is the "flag" items - Maple Flag, the
 * Singapore/Malaysia/Korean flags, the Pirate Emblem Flag, etc. They are real,
 * equippable items, so the range-based selection in getRandomEquip and
 * GenericEquipPool happily picks them like any other gear, but a bot "fighting"
 * while waving a decorative flag pole looks broken.
 *
 * Both base-spawn equip paths consult this list and simply skip omitted ids, so
 * the bot re-rolls a different item of the same slot ("gets something else").
 * This does not touch the NX cosmetic overlay or Free Market shop generation.
 *
 * Data lives in EquipOmitList.yaml next to this class - add or remove ids there,
 * no code change needed. Set "enabled: false" in the YAML to turn the whole list
 * off. Fails open: if the YAML cannot be read the list is treated as empty so
 * bots still equip (with flags) rather than spawning naked.
 */
public class EquipOmitList {

    private static final String YAML_PATH =
            "src/main/java/soloMapling/itemPool/EquipOmitList.yaml";

    private static volatile boolean enabled = true;
    private static volatile boolean loaded = false;
    private static final Set<Integer> omittedIds = new HashSet<>();
    private static final List<int[]> omittedRanges = new ArrayList<>();

    /**
     * Load the omit list from YAML. Idempotent - only loads once. Safe to call
     * from any thread; the first caller does the work, the rest are no-ops.
     */
    @SuppressWarnings("unchecked")
    public static synchronized void load() {
        if (loaded) return;

        try {
            YamlReader reader = new YamlReader(new FileReader(YAML_PATH));
            Map<String, Object> root = (Map<String, Object>) reader.read();

            if (root != null) {
                Object en = root.get("enabled");
                if (en != null) {
                    enabled = Boolean.parseBoolean(String.valueOf(en).trim());
                }

                Object ids = root.get("ids");
                if (ids instanceof List) {
                    for (Object raw : (List<?>) ids) {
                        Integer id = toInt(raw);
                        if (id != null) omittedIds.add(id);
                    }
                }

                Object ranges = root.get("ranges");
                if (ranges instanceof List) {
                    for (Object raw : (List<?>) ranges) {
                        int[] r = toRange(raw);
                        if (r != null) omittedRanges.add(r);
                    }
                }
            }

            loaded = true;
            System.out.println("[EquipOmitList] Loaded " + omittedIds.size()
                    + " omitted ids and " + omittedRanges.size()
                    + " ranges (enabled=" + enabled + ")");
        } catch (Exception e) {
            // Fail open: keep the list empty so bot spawn still works.
            loaded = true;
            System.err.println("[EquipOmitList] Failed to load YAML, omit list disabled: "
                    + e.getMessage());
        }
    }

    /**
     * True if this item id should never be auto-equipped on a bot. Lazily loads
     * the YAML on first use, mirroring GenericEquipPool.
     */
    public static boolean isOmitted(int itemId) {
        if (!loaded) load();
        if (!enabled) return false;
        if (omittedIds.contains(itemId)) return true;
        for (int[] r : omittedRanges) {
            if (itemId >= r[0] && itemId <= r[1]) return true;
        }
        return false;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Runtime toggle for the whole list (e.g. from the operator console). Does
     * not rewrite the YAML - the YAML value applies again on the next load.
     */
    public static void setEnabled(boolean value) {
        enabled = value;
    }

    private static Integer toInt(Object obj) {
        try {
            if (obj instanceof Number) return ((Number) obj).intValue();
            if (obj instanceof String) return Integer.parseInt(((String) obj).trim());
        } catch (NumberFormatException ignored) {
            // not a number - skip this entry rather than break the whole load
        }
        return null;
    }

    // Parse an inclusive range written as "min-max" (e.g. "1302200-1302299").
    private static int[] toRange(Object obj) {
        if (!(obj instanceof String)) return null;
        String[] parts = ((String) obj).split("-");
        if (parts.length != 2) return null;
        try {
            int min = Integer.parseInt(parts[0].trim());
            int max = Integer.parseInt(parts[1].trim());
            return min <= max ? new int[]{min, max} : new int[]{max, min};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
