package soloMapling.ArtificialPlayer.BotDecoratorSystem;

import com.esotericsoftware.yamlbeans.YamlReader;
import org.gms.server.ItemInformationProvider;
import soloMapling.itemPool.EquipOmitList;

import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Loads the flat curated list of generic/classless equips from YAML and caches
 * each item's WZ reqLevel at load time. Runtime picks enforce a hard level cap
 * (never pick gear the bot can't wear) and bias toward gear near the bot's level,
 * while still allowing occasional lower-level "fashion" picks.
 *
 * Call {@link #load()} once at startup (QuickEquip does this lazily). Then use
 * {@link #getRandom(String, int)} to pick a random item for a given category
 * and bot level.
 */
public class GenericEquipPool {

    private static final String YAML_PATH =
            "src/main/java/soloMapling/ArtificialPlayer/BotDecoratorSystem/GenericEquipPool.yaml";

    // Higher = stricter preference for gear near the bot's level. 0.05 gives a gentle tail
    // so a level 95 bot can still occasionally roll a level-35 whip for fashion.
    private static final double LEVEL_DECAY = 0.05;
    // Every eligible item keeps at least this relative weight, so outliers still occur.
    private static final double FASHION_FLOOR = 0.05;

    // Gender constants matching Character.getGender(): 0 = male, 1 = female, 2 = unisex.
    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;
    public static final int GENDER_UNISEX = 2;

    public static class PoolItem {
        final int id;
        final int minLevel;
        final int gender; // 0 male, 1 female, 2 unisex

        PoolItem(int id, int minLevel, int gender) {
            this.id = id;
            this.minLevel = minLevel;
            this.gender = gender;
        }
    }

    /**
     * Derive gender from the classic v83 equip-ID convention: the 4th digit
     * (thousands place) is 0 = male, 1 = female, 2 = unisex. This matches how
     * the client itself classifies equips — WZ has no separate reqGender field
     * for standard body equips.
     */
    private static int genderFromItemId(int itemId) {
        return (itemId / 1000) % 10;
    }

    private static final Map<String, List<PoolItem>> pools = new HashMap<>();
    private static boolean loaded = false;

    /**
     * Load the generic equip pool from YAML and cache each item's WZ reqLevel.
     * Safe to call multiple times - only loads once.
     */
    @SuppressWarnings("unchecked")
    public static synchronized void load() {
        if (loaded) return;

        try {
            YamlReader reader = new YamlReader(new FileReader(YAML_PATH));
            Map<String, Object> root = (Map<String, Object>) reader.read();

            ItemInformationProvider iip = ItemInformationProvider.getInstance();
            int itemCount = 0;

            for (Map.Entry<String, Object> entry : root.entrySet()) {
                String category = entry.getKey();
                Object val = entry.getValue();
                if (!(val instanceof List)) continue;

                List<PoolItem> list = new ArrayList<>();
                for (Object raw : (List<?>) val) {
                    int id = toInt(raw);
                    int min = iip.getEquipLevelReq(id);
                    int gender = genderFromItemId(id);
                    list.add(new PoolItem(id, min, gender));
                    itemCount++;
                }
                pools.put(category, list);
            }

            loaded = true;
            System.out.println("[GenericEquipPool] Loaded " + itemCount
                    + " items across " + pools.size() + " categories (reqLevel cached from WZ)");
        } catch (Exception e) {
            System.err.println("[GenericEquipPool] Failed to load YAML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Pick a random item ID from the given category for a bot at the given level
     * and gender. Never returns an item whose reqLevel exceeds botLevel or whose
     * gender doesn't match (unisex items are always eligible). Returns null if
     * the category is empty, the pool isn't loaded, or no item is eligible.
     *
     * @param botGender 0 = male, 1 = female (unisex items always pass).
     */
    public static Integer getRandom(String category, int botLevel, int botGender) {
        if (!loaded) return null;
        List<PoolItem> list = pools.get(category);
        if (list == null || list.isEmpty()) return null;

        // Single pass: filter by hard level cap + gender and build the weight table.
        List<PoolItem> eligible = new ArrayList<>(list.size());
        double[] weights = new double[list.size()];
        double total = 0.0;
        int n = 0;
        for (PoolItem item : list) {
            if (item.minLevel > botLevel) continue; // hard rule: never over-level
            if (item.gender != GENDER_UNISEX && item.gender != botGender) continue; // gender gate
            if (EquipOmitList.isOmitted(item.id)) continue; // central omit list (flag/junk items)
            double gap = botLevel - item.minLevel;
            double w = 1.0 / (1.0 + gap * LEVEL_DECAY);
            if (w < FASHION_FLOOR) w = FASHION_FLOOR;
            eligible.add(item);
            weights[n++] = w;
            total += w;
        }
        if (eligible.isEmpty()) return null;

        double roll = ThreadLocalRandom.current().nextDouble() * total;
        double acc = 0.0;
        for (int i = 0; i < n; i++) {
            acc += weights[i];
            if (roll <= acc) return eligible.get(i).id;
        }
        return eligible.get(n - 1).id;
    }

    private static int toInt(Object obj) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj instanceof String) return Integer.parseInt((String) obj);
        return 0;
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
