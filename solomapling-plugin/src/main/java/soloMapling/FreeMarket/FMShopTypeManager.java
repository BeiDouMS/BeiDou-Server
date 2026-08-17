package soloMapling.FreeMarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static soloMapling.FreeMarket.HiredMerchantArtificial.shopTypes.*;

public class FMShopTypeManager {
    static final Map<HiredMerchantArtificial.shopTypes, Integer> shopTypeWeights = new HashMap<>();

    static {
        // 0-5 = Warrior/Mage/Bow/Thief/Comm/Pirate Eqs
        shopTypeWeights.put(Warrior, 12); // War
        shopTypeWeights.put(Mage, 12);
        shopTypeWeights.put(Bowman, 12);
        shopTypeWeights.put(Thief, 18); // Thief
        shopTypeWeights.put(Common, 18); // Common
        shopTypeWeights.put(Pirate, 1); // Pirate

        shopTypeWeights.put(Scroll, 5); // 5% // 6-7 = Scrolls
        shopTypeWeights.put(DarkScroll, 5); // 5%

        shopTypeWeights.put(Potion, 5); // 5% // 8-11 = use etc chair mastery
        shopTypeWeights.put(ETC, 5);
        shopTypeWeights.put(Chair, 3);
        shopTypeWeights.put(Mastery, 4); // 4%
    }

    // Define items with their weights
    static final Map<HiredMerchantArtificial.shopTypes, Integer> secondaryShopTypeWeights = Map.of(
            Potion, 40,
            Scroll, 25,
            DarkScroll, 15,
            Mastery, 5,
            Chair, 2,
            ETC, 13
    );

    static final Map<HiredMerchantArtificial.shopTypes, Integer> hotRoomShopTypeWeights = Map.ofEntries(
            Map.entry(Warrior, 18),
            Map.entry(Mage, 18),
            Map.entry(Bowman, 18),
            Map.entry(Thief, 20),
            Map.entry(Common, 16),
            Map.entry(Pirate, 0),
            Map.entry(Scroll, 3),
            Map.entry(DarkScroll, 2),
            Map.entry(Potion, 2),
            Map.entry(ETC, 1),
            Map.entry(Chair, 1),
            Map.entry(Mastery, 1)
    );

    public static HiredMerchantArtificial.shopTypes selectHotRoomShopType() {
        return weightedSelect(hotRoomShopTypeWeights);
    }

    public static HiredMerchantArtificial.shopTypes selectShopTypeWeightedProbability() {
        return weightedSelect(shopTypeWeights);
    }

    private static HiredMerchantArtificial.shopTypes weightedSelect(Map<HiredMerchantArtificial.shopTypes, Integer> weights) {
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        int randomWeight = new Random().nextInt(totalWeight);
        int cumulativeWeight = 0;
        for (Map.Entry<HiredMerchantArtificial.shopTypes, Integer> entry : weights.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomWeight < cumulativeWeight) {
                return entry.getKey();
            }
        }
        return Potion;
    }

    public static List<HiredMerchantArtificial.shopTypes> getSecondaryShopItemTypes() {
        return getWeightedSelection(secondaryShopTypeWeights);
    }

    public static List<HiredMerchantArtificial.shopTypes> getWeightedSelection(Map<HiredMerchantArtificial.shopTypes, Integer> itemWeights) {
        Random random = new Random();

        // Determine whether to pick 1 or 2 items
        int count = random.nextBoolean() ? 1 : 2;
        List<HiredMerchantArtificial.shopTypes> selectedItems = new ArrayList<>();

        // Create a cumulative weight list for selection
        List<Map.Entry<HiredMerchantArtificial.shopTypes, Integer>> entries = new ArrayList<>(itemWeights.entrySet());
        int totalWeight = entries.stream().mapToInt(Map.Entry::getValue).sum();

        while (selectedItems.size() < count) {
            int randomWeight = random.nextInt(totalWeight) + 1; // Random value between 1 and totalWeight
            int currentWeightSum = 0;

            for (Map.Entry<HiredMerchantArtificial.shopTypes, Integer> entry : entries) {
                currentWeightSum += entry.getValue();
                if (randomWeight <= currentWeightSum) {
                    if (!selectedItems.contains(entry.getKey())) { // Avoid duplicates
                        selectedItems.add(entry.getKey());
                    }
                    break;
                }
            }
        }

        return selectedItems;
    }

}
