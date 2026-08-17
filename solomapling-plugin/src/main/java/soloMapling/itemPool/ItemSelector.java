package soloMapling.itemPool;

import com.esotericsoftware.yamlbeans.YamlReader;
import soloMapling.server.MapleVersionManager;

import java.io.FileReader;
import java.util.*;
import java.util.Map.Entry;

import static soloMapling.server.SoloMaplingUtilities.pickRandomItem;

public class ItemSelector {
    private Map<String, List<ItemNode>> items;

    public ItemSelector(String yamlFile) throws Exception {
        YamlReader reader = new YamlReader(new FileReader(yamlFile));
        items = (Map<String, List<ItemNode>>) reader.read();
    }

    // Convert a map to an ItemNode instance
    public static ItemNode mapToItemNode(Map<String, Object> itemMap) {
        ItemNode itemNode = new ItemNode(
                (String) itemMap.get("item"),
                (List<Integer>) itemMap.get("variant_id"),
                (Map<Integer, String>) itemMap.get("tier"),
                (Map<Integer, Integer>) itemMap.get("price"));
        return itemNode;
    }

    // Convert a map to an ScrollNode instance
    public static ScrollNode mapToScrollNode(Map<String, Object> itemMap) {
        ScrollNode scrollNode = new ScrollNode(
                (String) itemMap.get("item"),
                (List<Integer>) itemMap.get("variant_id"),
                (Map<Integer, String>) itemMap.get("tier"),
                (Map<Integer, Integer>) itemMap.get("price"),
                Integer.parseInt((String) itemMap.get("success_rate")),
                Integer.parseInt((String) itemMap.get("stat_bonus"))
        );
        return scrollNode;
    }

    // Method to get random item based on itemType, tier, and version
    public ItemNode getRandomItem(String itemType, String tier, int version) {
        List<ItemNode> filteredItems = new ArrayList<>();
        List<ItemNode> itemList = items.get(itemType);
        List<ItemNode> itemNodes = new ArrayList<>();

        if (itemList == null) {
            return null;
        }

        // Iterate through each item map and convert it to an ItemNode
        for (Object itemMap : itemList) {
            ItemNode itemNode = mapToItemNode((Map<String, Object>) itemMap);
            itemNodes.add(itemNode);
        }

        if (itemNodes == null) {
            System.out.println("No items found for item type: " + itemType);
            return null;
        }

        // todo make method
        for (ItemNode item : itemNodes) {
            Map<Integer, String> tierMap = item.getTier();
            for (Entry<Integer, String> entry : tierMap.entrySet()) {
                Object currentTier = entry.getValue();

                int entryVersion;
                try {
                    entryVersion = toInteger(entry.getKey());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid version key: " + entry.getKey());
                    continue;
                }

                int startVersion = entryVersion;
                int endVersion = getEndVersion(tierMap, String.valueOf(entry.getKey()));

                if (tier.equals(currentTier) && version >= startVersion && version < endVersion) {
                    filteredItems.add(item);
                    break;
                }
            }
        }

        if (filteredItems.isEmpty()) {
            return null;
        }

        // Select random item from the filtered list
        Random random = new Random();
        return filteredItems.get(random.nextInt(filteredItems.size()));
    }

    // Helper to get the end version for the tier range
    private int getEndVersion(Map<Integer, String> tierMap, String currentVersionKey) {
        int currentVersion = Integer.parseInt(currentVersionKey);
        int endVersion = Integer.MAX_VALUE;

        for (Object key : tierMap.keySet()) {
            int version; // = toInteger(key);

            try {
                version = toInteger(key);
            } catch (NumberFormatException e) {
                System.out.println("Invalid version key: " + key);
                continue;
            }

            if (version > currentVersion && version < endVersion) {
                endVersion = version;
            }
        }

        return endVersion;
    }

    public static Integer pickRandomVariantId(List<Integer> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return null;  // Return null if the list is empty or null
        }

        Random random = new Random();
        int randomIndex = random.nextInt(variantIds.size());
        return toInteger(variantIds.get(randomIndex));
    }

    public static int getPriceForVersion(Map<Integer, Integer> versionPriceMap) {
        // Sort the map by key using a TreeMap to ensure natural ordering
        SortedMap<Integer, Integer> sortedMap = new TreeMap<>(versionPriceMap);

        int result = -1;
        int version = MapleVersionManager.getItemPoolVersion();
        for (Map.Entry<Integer, Integer> entry : sortedMap.entrySet()) {
            int currentVersion = toInteger(entry.getKey());
            if (version < currentVersion) {
                break;  // Exit loop if the input version is lower than the current range start
            }
            result = toInteger(entry.getValue());
        }
        return result;
    }

    public static Integer toInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value; // Already an Integer
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value); // Parse String to Integer
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("String cannot be parsed to Integer: " + value);
            }
        } else if (value instanceof Number) {
            return ((Number) value).intValue(); // Convert Number to Integer
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        }
    }

    public static ItemNode getRandomItemFull(String itemPool, String itemType, String tier) {
        try {
            int version = MapleVersionManager.getItemPoolVersion();
            ItemSelector itemSelector = new ItemSelector("src/main/java/soloMapling/itemPool/itemConfig/" + itemPool);
            ItemNode randomItem = itemSelector.getRandomItem(itemType, tier, version);
            if (randomItem != null) {
                return randomItem;
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ScrollNode getScrollNodeData(int itemId) {
        return ItemDatabase.getInstance().getScrollData(itemId);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 30; i++) {
            List<String> ALL_SCROLLS = List.of("Earring", "Overall", "Claw", "Shoes", "Gloves", "Cape", "ETC");
            List<String> ALL_DARK_SCROLLS = List.of(
                    "Earring", "Overall", "Shoes",
                    "Gloves", "Cape", "Hat", "Top", "Bottom", "Shield", "ETC");

            String randomItem = pickRandomItem(ALL_DARK_SCROLLS);
            getRandomItemFull("darkscrolls.yaml", randomItem, "B");
        }
    }
}
