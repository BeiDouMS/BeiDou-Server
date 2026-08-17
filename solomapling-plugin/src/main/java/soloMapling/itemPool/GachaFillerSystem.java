package soloMapling.itemPool;
import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.Map.Entry;

public class GachaFillerSystem {

    // Configurable parameters
    private static final int DEFAULT_LIST_SIZE = 10;
    private static final double DEFAULT_ITEM_REPLACE_CHANCE = 0.30; // % chance to replace coin with item

    // Type probabilities (must sum to 1.0)
    private static final Map<String, Double> TYPE_PROBABILITIES = new HashMap<>() {{
        put("useables", 0.50);
        put("ores", 0.20);
        put("jewels", 0.20);
        put("crystals", 0.10);
    }};

    // Tier probabilities (must sum to 1.0)
    private static final Map<String, Double> TIER_PROBABILITIES = new HashMap<>() {{
        put("s_tier", 0.20);
        put("a_tier", 0.30);
        put("b_tier", 0.50);
    }};

    private static final String YAML_PATH = "src/main/java/soloMapling/itemPool/itemConfig/gachaFiller.yaml";

    private static Random random = new Random();
    private static Map<String, Map<String, List<Integer>>> gachaData;

    // Static block to load YAML data once
    static {
        try {
            loadGachaData();
        } catch (Exception e) {
            System.err.println("Failed to load gacha data: " + e.getMessage());
        }
    }

    public static List<Integer> createGachaListWithPrize(int prize_id) {
        List<Integer> list = createGachaFillerList();
        int halfwayPoint = list.size() / 4;
        int insertPosition = halfwayPoint + (int)(Math.random() * (list.size() - halfwayPoint + 1));
        list.add(insertPosition, prize_id);
        return list;
    }

    public static List<Integer> createGachaFillerList() {
        return createGachaFillerList(DEFAULT_LIST_SIZE, DEFAULT_ITEM_REPLACE_CHANCE);
    }

    public static List<Integer> createGachaFillerList(int size, double itemReplaceChance) {
        List<Integer> fillerList = new ArrayList<>(Collections.nCopies(size, 0));

        for (int i = 0; i < fillerList.size(); i++) {
            // Roll to see if we replace coin with item
            if (random.nextDouble() < itemReplaceChance) {
                // Roll for type
                String selectedType = rollForType();
                // Roll for tier
                String selectedTier = rollForTier();
                // Get random item from that type/tier
                int itemId = getRandomItem(selectedType, selectedTier);
                fillerList.set(i, itemId);
            }
        }

        return fillerList;
    }

    private static String rollForType() {
        double roll = random.nextDouble();
        double cumulative = 0.0;

        for (Entry<String, Double> entry : TYPE_PROBABILITIES.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }

        // Fallback (shouldn't happen if probabilities sum to 1.0)
        return "useables";
    }

    private static String rollForTier() {
        double roll = random.nextDouble();
        double cumulative = 0.0;

        for (Entry<String, Double> entry : TIER_PROBABILITIES.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }

        // Fallback (shouldn't happen if probabilities sum to 1.0)
        return "b_tier";
    }

    public static int getRandomItem(String type, String tier) {
        if (gachaData == null || !gachaData.containsKey(type)) {
            System.err.println("Type not found: " + type);
            return 0;
        }

        Map<String, List<Integer>> typeData = gachaData.get(type);
        if (!typeData.containsKey(tier)) {
            System.err.println("Tier not found: " + tier + " for type: " + type);
            return 0;
        }

        List<Integer> itemList = typeData.get(tier);
        if (itemList == null || itemList.isEmpty()) {
            System.err.println("No items found for type: " + type + ", tier: " + tier);
            return 0;
        }

        // Return random item from the list
        return itemList.get(random.nextInt(itemList.size()));
    }

    public static int getRandomMesoGachaFiller() {
        // Variety of meso drop types. Adds visual flare
        int[] numbers = {10, 10, 10, 10, 10, 69, 69, 69, 69, 420, 420, 420, 1337};
        return numbers[(int)(Math.random() * 13)];
    }

    @SuppressWarnings("unchecked")
    private static void loadGachaData() throws Exception {
        File yamlFile = new File(YAML_PATH);

        // If file doesn't exist at relative path, try to find it relative to working directory
        if (!yamlFile.exists()) {
            String workingDir = System.getProperty("user.dir");
            yamlFile = new File(workingDir, YAML_PATH);

            if (!yamlFile.exists()) {
                throw new FileNotFoundException("Cannot find gacha YAML file at: " + yamlFile.getAbsolutePath());
            }
        }

        YamlReader reader = new YamlReader(new FileReader(yamlFile));
        Object object = reader.read();

        if (object instanceof Map) {
            gachaData = new HashMap<>();
            Map<String, Object> rawData = (Map<String, Object>) object;

            // Convert the raw data to our expected structure
            for (Entry<String, Object> typeEntry : rawData.entrySet()) {
                String type = typeEntry.getKey();
                Map<String, List<Integer>> tierMap = new HashMap<>();

                if (typeEntry.getValue() instanceof Map) {
                    Map<String, Object> tiers = (Map<String, Object>) typeEntry.getValue();

                    for (Entry<String, Object> tierEntry : tiers.entrySet()) {
                        String tier = tierEntry.getKey();
                        List<Integer> items = new ArrayList<>();

                        if (tierEntry.getValue() instanceof List) {
                            List<?> rawItems = (List<?>) tierEntry.getValue();
                            for (Object item : rawItems) {
                                // Handle different number types
                                if (item instanceof Integer) {
                                    items.add((Integer) item);
                                } else if (item instanceof Long) {
                                    items.add(((Long) item).intValue());
                                } else if (item instanceof Double) {
                                    items.add(((Double) item).intValue());
                                } else if (item instanceof String) {
                                    // Try to parse string as integer
                                    try {
                                        items.add(Integer.parseInt((String) item));
                                    } catch (NumberFormatException e) {
                                        System.err.println("Failed to parse item: " + item);
                                    }
                                }
                            }
                        } else if (tierEntry.getValue() instanceof ArrayList) {
                            // YamlBeans might return ArrayList
                            ArrayList<?> rawItems = (ArrayList<?>) tierEntry.getValue();
                            for (Object item : rawItems) {
                                if (item != null) {
                                    // Convert to string first, then parse
                                    String itemStr = item.toString();
                                    try {
                                        items.add(Integer.parseInt(itemStr));
                                    } catch (NumberFormatException e) {
                                        System.err.println("Failed to parse item: " + itemStr);
                                    }
                                }
                            }
                        }

                        tierMap.put(tier, items);
                    }
                }

                gachaData.put(type, tierMap);
            }
        }

        reader.close();
    }

    // Method to reload gacha data if needed
    public static void reloadGachaData() throws Exception {
        loadGachaData();
    }

    // Utility method to print the configuration
    public static void printConfiguration() {
        System.out.println("=== Gacha Filler Configuration ===");
        System.out.println("Default List Size: " + DEFAULT_LIST_SIZE);
        System.out.println("Item Replace Chance: " + (DEFAULT_ITEM_REPLACE_CHANCE * 100) + "%");
        System.out.println("\nType Probabilities:");
        for (Entry<String, Double> entry : TYPE_PROBABILITIES.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + (entry.getValue() * 100) + "%");
        }
        System.out.println("\nTier Probabilities:");
        for (Entry<String, Double> entry : TIER_PROBABILITIES.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + (entry.getValue() * 100) + "%");
        }
    }

    // Utility method to print loaded gacha data (for debugging)
    public static void printLoadedData() {
        System.out.println("\n=== Loaded Gacha Data ===");
        if (gachaData != null) {
            for (Entry<String, Map<String, List<Integer>>> typeEntry : gachaData.entrySet()) {
                System.out.println(typeEntry.getKey() + ":");
                for (Entry<String, List<Integer>> tierEntry : typeEntry.getValue().entrySet()) {
                    System.out.println("  " + tierEntry.getKey() + ": " + tierEntry.getValue());
                }
            }
        }
    }

    // Example usage
    public static void main(String[] args) {
        // Print configuration
        printConfiguration();

        // Print loaded data for verification
        printLoadedData();

        // Create a gacha filler list with default settings
        List<Integer> gachaList = createGachaFillerList();
        System.out.println("\nGenerated Gacha List: " + gachaList);

        // Create a custom sized list with 60% item chance
        List<Integer> customList = createGachaFillerList(20, 0.6);
        System.out.println("\nCustom Gacha List (size=20, 60% item chance): " + customList);

        // Test specific item retrieval
        System.out.println("\nRandom S-tier useable: " + getRandomItem("useables", "s_tier"));
        System.out.println("Random B-tier ore: " + getRandomItem("ores", "b_tier"));
    }
}
