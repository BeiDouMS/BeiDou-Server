package soloMapling.itemPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UniqueStatBonusList {
    private Map<Integer, Long> statBonusMap;

    static double midway_price_multiplier = 0.60;

    public UniqueStatBonusList() {
        this.statBonusMap = new TreeMap<>();
    }

    // Method to add a stat bonus and price pair
    public void addStatBonus(int statBonus, Long price) {
        // Check if the stat bonus already exists in the map
        if (statBonusMap.containsKey(statBonus)) {
            // If it exists, only replace if the new price is lower
            Long currentPrice = (statBonusMap.get(statBonus));
            if (price < currentPrice) {
                statBonusMap.put(statBonus, price);
            }
        } else {
            // If it doesn't exist, add the new stat bonus and price
            statBonusMap.put(statBonus, price);
        }
    }

    // Method to get the unique list of stat bonus and price pairs
    public Map<Integer, Long> getStatBonusList() {
        return statBonusMap;
    }

    // Method to correct anomalies in the map
    public static void correctAnomalies(Map<Integer, Long> statPriceMap) {
        List<Map.Entry<Integer, Long>> entries = new ArrayList<>(statPriceMap.entrySet());

        for (int i = 1; i < entries.size() - 1; i++) {
            Long prevPrice = (entries.get(i - 1).getValue());
            Long currentPrice = (entries.get(i).getValue());
            Long nextPrice = (entries.get(i + 1).getValue());

            // If current price is greater than next price (indicating an anomaly)
            if (currentPrice > nextPrice) {
                // Calculate corrected price closer to the nextPrice with an exponential effect
                Long correctedPrice = (long) (prevPrice + midway_price_multiplier * (nextPrice - prevPrice));
                entries.get(i).setValue(correctedPrice);
            }
        }

        // Update the original map with corrected prices
        for (Map.Entry<Integer, Long> entry : entries) {
            statPriceMap.put(entry.getKey(), entry.getValue());
        }
    }

    public static void applySellablesDeduction(Map<Integer, Long> statPriceMap, double sellablesCostOffseter) {
        boolean skipFirst = true;
        for (Map.Entry<Integer, Long> entry : statPriceMap.entrySet()) {
            if (skipFirst) {
                skipFirst = false;
                continue;
            }
            Long adjustedPrice = (long) (entry.getValue() * sellablesCostOffseter);
            entry.setValue(adjustedPrice); // Update the entry's value with the adjusted price
        }
    }
}
