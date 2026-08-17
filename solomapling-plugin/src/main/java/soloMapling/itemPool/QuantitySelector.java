package soloMapling.itemPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class QuantitySelector {
    private static ItemQuantityConfig config;
    static String filePath = "src/main/java/soloMapling/itemPool/itemConfig/itemQuantities.yaml";

    // Load configuration at the start
    public static void loadConfig() {
        config = ItemQuantityConfig.readYaml(filePath);
        if (config == null) {
            throw new RuntimeException("Failed to load configuration.");
        }
    }

    public static int quantitySelector(String type, String tier) {

        if (config == null) {
            QuantitySelector.loadConfig();
        }

        if (config == null) {
            throw new IllegalStateException("Configuration is not loaded.");
        }

        ItemQuantityConfig.ItemType itemType = config.itemQuantities.get(type);
        if (itemType == null) {
            throw new IllegalArgumentException("Unknown type: " + type);
        }

        ItemQuantityConfig.TierRange range = itemType.tiers.get(tier);
        if (range == null) {
            throw new IllegalArgumentException("Unknown tier: " + tier + " for type: " + type);
        }

        Random random = new Random();
        return random.nextInt(range.max - range.min + 1) + range.min;
    }

    public static String distributedTierSelector(String tier) {
        Map<String, Map<String, Integer>> tierProbabilities = Map.of(
                "S", Map.of("S", 60, "A", 38, "B", 2),
                "A", Map.of("A", 70, "S", 10, "B", 20),
                "B", Map.of("B", 68, "A", 30, "S", 2)
        );

        // Get the probability map for the given tier
        Map<String, Integer> probabilities = tierProbabilities.getOrDefault(tier, Map.of("S", 20, "A", 40, "B", 40));

        // Flatten the probability distribution into a list
        List<String> weightedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : probabilities.entrySet()) {
            weightedList.addAll(Collections.nCopies(entry.getValue(), entry.getKey()));
        }

        // Randomly select from the weighted list
        Random random = new Random();
        return weightedList.get(random.nextInt(weightedList.size()));
    }

    public static void test() {
//        String type = "Scroll";
//        String tier = "S";
//        int quantity = QuantitySelector.quantitySelector(type, tier);
//        System.out.println("Selected quantity for " + type + " (" + tier + "): " + quantity);

        for (int i = 0; i < 100; i++) {
            System.out.println(distributedTierSelector("B"));
        }
    }

    public static void main(String[] args) {
        test();
    }
}
