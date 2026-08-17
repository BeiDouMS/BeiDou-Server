package soloMapling.itemPool;

import org.gms.client.inventory.Equip;

import static soloMapling.FreeMarket.EquipListGenerator.generateCleanItemEquip;

public class ScrolledItemComparator {
    private final Equip scrolledItem;
    private final Equip cleanItem;
    private ComparisonResult comparisonResult;

    // Constructor to initialize with a scrolled item
    public ScrolledItemComparator(Equip scrolledItem) {
        this.scrolledItem = scrolledItem;
        this.cleanItem = retrieveCleanVersion(scrolledItem.getItemId());
        this.comparisonResult = compareAttributes();
    }

    // Retrieves the clean version of the item based on its itemId
    private Equip retrieveCleanVersion(int itemId) {
        // To be implemented: retrieve or create a clean Equip object based on itemId
        Equip cleanItem = (Equip) generateCleanItemEquip(itemId);
        return cleanItem;
    }

    // Compares attributes like attack, defense, stats, etc.
    public ComparisonResult compareAttributes() {
        int watkDiff = this.scrolledItem.getWatk() - this.cleanItem.getWatk();
        int matkDiff = this.scrolledItem.getMatk() - this.cleanItem.getMatk();
        int strDiff = this.scrolledItem.getStr() - this.cleanItem.getStr();
        int dexDiff = this.scrolledItem.getDex() - this.cleanItem.getDex();
        int intDiff = this.scrolledItem.getInt() - this.cleanItem.getInt();
        int lukDiff = this.scrolledItem.getLuk() - this.cleanItem.getLuk();
        int upgradeSlotsDiff = this.scrolledItem.getUpgradeSlots() - this.cleanItem.getUpgradeSlots();
        return new ComparisonResult(watkDiff, matkDiff, strDiff, dexDiff, intDiff, lukDiff, upgradeSlotsDiff);
    }

    public int getHighestStatValueDifference() {
        return this.comparisonResult.getHighestStatValue();
    }

    public int getHighestStatValue() {
        return getHighestStatValue(getHighestStatType());
    }

    public int getHighestStatValue(String statType) {
        switch (statType) {
            case "watt":
                return this.scrolledItem.getWatk();
            case "matt":
                return this.scrolledItem.getMatk();
            case "str":
                return this.scrolledItem.getStr();
            case "dex":
                return this.scrolledItem.getDex();
            case "int":
                return this.scrolledItem.getInt();
            case "luk":
                return this.scrolledItem.getLuk();
            default:
                return 0;
        }
    }

    public String getHighestStatType() {
        return this.comparisonResult.getHighestStatType();
    }

    public int getUpgradeSlotDiff() {
        return this.comparisonResult.getUpgradeSlotDiff();
    }

}
