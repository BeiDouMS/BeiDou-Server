package soloMapling.itemPool;

import java.util.HashMap;
import java.util.Map;

public class ComparisonResult {
    private final int wattDiff;
    private final int mattDiff;
    private final int strDiff;
    private final int dexDiff;
    private final int intDiff;
    private final int lukDiff;
    private final int upgradeSlotDiff;


    // Constructor to initialize differences
    public ComparisonResult(int wattDiff, int mattDiff, int strDiff, int dexDiff, int intDiff, int lukDiff, int upgradeSlotsDiff) {
        this.wattDiff = wattDiff;
        this.mattDiff = mattDiff;
        this.strDiff = strDiff;
        this.dexDiff = dexDiff;
        this.intDiff = intDiff;
        this.lukDiff = lukDiff;
        this.upgradeSlotDiff = upgradeSlotsDiff;
    }

    // Getters for each comparison result
    public int getAttackDifference() {
        return wattDiff;
    }

    public String getHighestStatType() {
        // Create a map to store stat values and their corresponding names
        Map<Integer, String> statMap = new HashMap<>();
        statMap.put(this.wattDiff, "watt");
        statMap.put(this.mattDiff, "matt");
        statMap.put(this.strDiff, "str");
        statMap.put(this.dexDiff, "dex");
        statMap.put(this.intDiff, "int");
        statMap.put(this.lukDiff, "luk");

        // Find the highest value
        int maxValue = Math.max(
                Math.max(
                        Math.max(wattDiff, mattDiff),
                        Math.max(strDiff, dexDiff)
                ),
                Math.max(intDiff, lukDiff)
        );

        // Return the corresponding stat name
        return statMap.get(maxValue);
    }

    public int getHighestStatValue() {
        return Math.max(wattDiff, Math.max(mattDiff, Math.max(strDiff, Math.max(dexDiff, Math.max(intDiff, lukDiff)))));
    }

    public int getUpgradeSlotDiff() {
        return this.upgradeSlotDiff;
    }

}
