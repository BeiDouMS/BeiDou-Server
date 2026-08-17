package soloMapling.ArtificialPlayer.BotTypes;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static soloMapling.BotLogger.log;

public class DropGameLootPool {

    private static final String LOOT_POOL_PATH = "src/main/java/soloMapling/ArtificialPlayer/BotDialoguePack/DropGameLootPool.yaml";
    private static final Random random = new Random();
    private static final int SPECIAL_CHANCE_PERCENT = 33;

    private final List<LootEntry> entries = new ArrayList<>();
    private int totalWeight = 0;

    private final List<LootEntry> specialEntries = new ArrayList<>();
    private int specialTotalWeight = 0;

    public static class LootEntry {
        public final int itemId;
        public final int weight;
        public final boolean isEquip;

        public LootEntry(int itemId, int weight, boolean isEquip) {
            this.itemId = itemId;
            this.weight = weight;
            this.isEquip = isEquip;
        }
    }

    public static DropGameLootPool load(String tier) {
        DropGameLootPool pool = new DropGameLootPool();
        try {
            YamlReader reader = new YamlReader(new FileReader(LOOT_POOL_PATH));
            Map<String, Object> root = (Map<String, Object>) reader.read();
            Map<String, Object> tierNode = (Map<String, Object>) root.get(tier);
            if (tierNode == null) {
                log("DropGameLootPool: No tier found for: " + tier);
                return pool;
            }

            loadEntries(tierNode, "items", pool.entries, pool);
            loadEntries(tierNode, "special_items", pool.specialEntries, pool);
        } catch (Exception e) {
            log("DropGameLootPool: Failed to load tier: " + tier);
            e.printStackTrace();
        }
        return pool;
    }

    private static void loadEntries(Map<String, Object> tierNode, String key,
                                     List<LootEntry> targetList, DropGameLootPool pool) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) tierNode.get(key);
        if (items == null) return;

        int weight = 0;
        for (Map<String, Object> entry : items) {
            int itemId = Integer.parseInt(String.valueOf(entry.get("id")));
            int w = Integer.parseInt(String.valueOf(entry.get("weight")));
            boolean isEquip = entry.containsKey("equip") && Boolean.parseBoolean(String.valueOf(entry.get("equip")));
            targetList.add(new LootEntry(itemId, w, isEquip));
            weight += w;
        }

        if (key.equals("special_items")) {
            pool.specialTotalWeight = weight;
        } else {
            pool.totalWeight = weight;
        }
    }

    public LootEntry rollItem() {
        if (entries.isEmpty() && specialEntries.isEmpty()) {
            return null;
        }

        boolean useSpecial = !specialEntries.isEmpty()
                && random.nextInt(100) < SPECIAL_CHANCE_PERCENT;

        if (useSpecial) {
            return rollFrom(specialEntries, specialTotalWeight);
        }

        if (!entries.isEmpty()) {
            return rollFrom(entries, totalWeight);
        }

        return rollFrom(specialEntries, specialTotalWeight);
    }

    private LootEntry rollFrom(List<LootEntry> list, int total) {
        if (list.isEmpty() || total == 0) return null;
        int roll = random.nextInt(total);
        int cumulative = 0;
        for (LootEntry entry : list) {
            cumulative += entry.weight;
            if (roll < cumulative) {
                return entry;
            }
        }
        return list.get(list.size() - 1);
    }

    public boolean isEmpty() {
        return entries.isEmpty() && specialEntries.isEmpty();
    }

    public int size() {
        return entries.size() + specialEntries.size();
    }
}
