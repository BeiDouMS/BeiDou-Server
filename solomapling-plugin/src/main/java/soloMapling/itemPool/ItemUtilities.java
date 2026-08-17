package soloMapling.itemPool;

import org.gms.client.inventory.Equip;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;

import static soloMapling.ArtificialPlayer.BotLogic.generateCleanEquip;
import static soloMapling.DebugUtilities.debugprint;
import static soloMapling.itemPool.UpgradeSimulator.getEquipMarketValue;

public class ItemUtilities {

    public static boolean isEquip(Item item) {
        return item.getInventoryType() == InventoryType.EQUIP;
    }

    public static boolean isNonEquip(Item item) {
        return !isEquip(item);
    }

    public static boolean isCleanEquip(Item item) {
        if (isEquip(item)) {
            Equip eq = (Equip) item;
            Equip cleanEq = (Equip) generateCleanEquip(item.getItemId());
            int cleanUpgradeSlots = cleanEq.getUpgradeSlots();
            return eq.getLevel() == 0 && eq.getUpgradeSlots() == cleanUpgradeSlots; // No Upgrades, and Clean slots
        }
        return false;
    }

    public static boolean isUseable(Item item) {
        return item.getInventoryType() == InventoryType.USE;
    }

    public static boolean isSetup(Item item) {
        return item.getInventoryType() == InventoryType.SETUP;
    }

    public static boolean isETC(Item item) {
        return item.getInventoryType() == InventoryType.ETC;
    }

    public static boolean isCash(Item item) {
        return item.getInventoryType() == InventoryType.CASH;
    }

    /*
    getPrice - getItemMarketValue
    getItemPrice - getItemMarketValue
    getMarketPrice, getMarketValue
     */
    public static Integer getItemMarketValue(Item thisItem) {
        Integer itemMarketValue = 0;
        if (ItemUtilities.isEquip(thisItem)) {
            if (ItemUtilities.isCleanEquip(thisItem)) {
                itemMarketValue = (ItemDatabase.getInstance().getItemPrice(thisItem.getItemId()));
//                debugprint("Clean Item Value: ", itemMarketValue);
            } else if (!ItemUtilities.isCleanEquip(thisItem)) {
                itemMarketValue = getEquipMarketValue((Equip) thisItem);
//                debugprint("Scrolled Eq Market Value: ", itemMarketValue);
            }
        } else {
            itemMarketValue = (ItemDatabase.getInstance().getItemPrice(thisItem.getItemId()));
//            debugprint("Non-Eq item Value: ", itemMarketValue);
        }
        return itemMarketValue;
    }

}
