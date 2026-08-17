package soloMapling.itemPool;

import java.io.FileReader;
import java.util.*;

import com.esotericsoftware.yamlbeans.YamlReader;
import soloMapling.server.MapleVersionManager;

import static soloMapling.server.MapleVersionManager.getItemPoolVersion;
import static soloMapling.itemPool.ItemDatabase.loadItemDatabase;
import static soloMapling.itemPool.ItemSelector.toInteger;

public class ItemDatabase {
    private static final Map<Integer, ItemNode> itemYamlDB = new HashMap<>();
    private static ItemDatabase instance = null;

    public static ItemDatabase getInstance() {
        if (instance == null) {
            instance = new ItemDatabase();
            try {
                loadItemDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public static void loadItemDatabase() throws Exception {
        ItemDatabase db = ItemDatabase.getInstance();

        List<String> itemPools = Arrays.asList(
                "thief.yaml", "common.yaml", "etc.yaml",
                "scrolls.yaml", "darkscrolls.yaml",
                "useables.yaml"); // Add more options as needed

        for (String itemPool : itemPools) {
            String yamlFile = "src/main/java/soloMapling/itemPool/itemConfig/" + itemPool;
            YamlReader reader = new YamlReader(new FileReader(yamlFile));
            Map<String, List<ItemNode>> thiefitems = (Map<String, List<ItemNode>>) reader.read();

            for (Map.Entry<String, List<ItemNode>> entry : thiefitems.entrySet()) {
                String itemType = entry.getKey();
                List<ItemNode> itemList = thiefitems.get(itemType);

                for (Object item : itemList) {
                    db.processItem((Map<String, Object>) item);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void processItem(Map<String, Object> itemMap) {
        String name = (String) itemMap.get("item");
        List<Integer> variantIds = (List<Integer>) itemMap.get("variant_id");

        // Convert tier map
        Map<String, String> rawTiers = (Map<String, String>) itemMap.get("tier");
        Map<Integer, String> tiers = new HashMap<>();
        rawTiers.forEach((k, v) -> tiers.put((toInteger((Object) k)), v));

        // Convert price map
        Map<String, String> rawPrices = (Map<String, String>) itemMap.get("price");
        Map<Integer, Integer> prices = new HashMap<>();
        rawPrices.forEach((k, v) -> prices.put((toInteger((Object) k)), (toInteger((Object) v))));

        Object testNode = ((Object) itemMap.get("success_rate"));
        if (testNode != null) {
            int successRate = toInteger((Object) itemMap.get("success_rate"));
            int statBonus = toInteger((Object) itemMap.get("stat_bonus"));
            ScrollNode scrollNode = new ScrollNode(name, variantIds, tiers, prices, successRate, statBonus);
            indexByVariantIds(scrollNode, variantIds);
        } else {
            ItemNode itemNode = new ItemNode(name, variantIds, tiers, prices);
            indexByVariantIds(itemNode, variantIds);
        }
    }

    private void indexByVariantIds(ItemNode item, List<Integer> variantIds) {
        for (Object variantId : variantIds) {
            itemYamlDB.put(toInteger(variantId), item);
        }
    }

    public ItemNode getItemData(int variantId) {
        return itemYamlDB.get(variantId); // Returns null if variantId is not in the map
    }

    public String getItemName(int variantId) {
        ItemNode item = getItemData(variantId);
        return (item != null) ? item.getItem() : null;
    }

    public String getItemTier(int variantId) {
        return getItemTier(variantId, getItemPoolVersion());
    }

    public String getItemTier(int variantId, int version) {
        ItemNode item = getItemData(variantId);
        return (item != null) ? getValueForVersion(item.getTier(), version) : null;
    }

    public Integer getItemPrice(int variantId) {
        return getItemPrice(variantId, getItemPoolVersion());
    }

    public Integer getItemPrice(int variantId, int version) {
        ItemNode item = getItemData(variantId);
        return (item != null) ? getValueForVersion(item.getPriceMap(), version) : null;
    }

    public ScrollNode getScrollData(int itemId) {
        ItemNode item = getItemData(itemId);
        if (item instanceof ScrollNode) {
            ScrollNode scroll = (ScrollNode) item;
            return scroll;
        } else {
            return null;
        }
    }

    public int getScrollSuccessRate(int scrollId) {
        ScrollNode scroll = getScrollData(scrollId);
        return (scroll != null) ? (scroll.getSuccessRate()) : null;
    }

    public int getScrollStatBonus(int scrollId) {
        ScrollNode scroll = getScrollData(scrollId);
        return (scroll != null) ? (scroll.getStatBonus()) : null;
    }

    private <T> T getValueForVersion(Map<Integer, T> versionMap, int targetVersion) {
        T result = null;
        int highestValidVersion = -1;

        for (Map.Entry<Integer, T> entry : versionMap.entrySet()) {
            int version = entry.getKey();
            if (version <= targetVersion && version > highestValidVersion) {
                highestValidVersion = version;
                result = entry.getValue();
            }
        }
        return result;
    }

    public boolean checkIfItemExistsInCurrentVersion(int itemId) {
        ItemNode item = getItemData(itemId);
        if(item == null) {
            return false;
        }

        Map<Integer, String> tierMap = item.getTier();
        if (tierMap != null) {
            int earliestVersion = Integer.MAX_VALUE;
            for (Object key : (tierMap.keySet())) {
                int intKey = toInteger(key); // Convert each key to an integer
                if (intKey < earliestVersion) {
                    earliestVersion = intKey;
                }
            }
            // Return true if the current version is >= the earliest version, false otherwise
            boolean isInVersion = MapleVersionManager.getItemPoolVersion() >= earliestVersion;
            return isInVersion;
        }
        return false;
    }

}

// Usage example:
class Example {


    public static void test3() throws Exception {
        int curr = 1332020;
        ItemDatabase db = ItemDatabase.getInstance();
        System.out.println((db.getItemData(curr).getItem()));
        System.out.println((db.getItemData(curr).getVariantId()));
        System.out.println((db.getItemTier(curr)));
        System.out.println((db.getItemPrice(curr)));
        System.out.println((db.getItemData(curr)));

        System.out.println(db.getScrollStatBonus(2040805));
        System.out.println(db.checkIfItemExistsInCurrentVersion(1092049));
        System.out.println(db.checkIfItemExistsInCurrentVersion(2040407));
    }

//    public static void test() {
//        ItemDatabase db = new ItemDatabase();
//
//        // Create and process test YAML-like data structure
//        Map<String, List<Map<String, Object>>> testData = new HashMap<>();
//
//        Map<String, Object> scarabData = new HashMap<>();
//        scarabData.put("item", "Lv 70 Scarab");
//        scarabData.put("variant_id", Arrays.asList(123, 1234, 12435));
//
//        Map<String, String> tiers = new HashMap<>();
//        tiers.put("1", "S");
//        tiers.put("19", "A");
//        scarabData.put("tier", tiers);
//
//        Map<String, Integer> prices = new HashMap<>();
//        prices.put("1", 4000000);
//        prices.put("19", 3000000);
//        scarabData.put("price", prices);
//
//        List<Map<String, Object>> weaponsList = new ArrayList<>();
//        weaponsList.add(scarabData);
//        testData.put("Weapon", weaponsList);
//
//        // Process the test data
//        db.processYamlData(testData);
//
//        // Now we can test the lookups
//        db.getItemTier(123, 1).ifPresent(tier ->
//                System.out.println("Tier at version 1: " + tier));  // Should print S
//        db.getItemTier(123, 20).ifPresent(tier ->
//                System.out.println("Tier at version 20: " + tier)); // Should print A
//
//        // Test price lookups
//        db.getItemPrice(123, 1).ifPresent(price ->
//                System.out.println("Price at version 1: " + price));  // Should print 4000000
//        db.getItemPrice(123, 20).ifPresent(price ->
//                System.out.println("Price at version 20: " + price)); // Should print 3000000
//
//        // Test variant ID lookup
//        db.getItemData(12435).ifPresent(item ->
//                System.out.println("Found item by variant: " + item.getItem())); // Should print Lv 70 Scarab
//    }

    public static void main(String[] args) throws Exception {
        loadItemDatabase();
        test3();
    }
}