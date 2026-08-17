package soloMapling.ArtificialPlayer;

import org.gms.client.Character;
import org.gms.client.inventory.BodyPart;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.InventoryType;
import org.gms.constants.inventory.ItemConstants;
import org.gms.server.ItemInformationProvider;

import java.util.List;
import java.util.Random;

import static soloMapling.server.SoloMaplingUtilities.getRandomNumber;

public class BotCustomization {

    // bot customization - Handles decorating the bots. Hairs, equips. Visible stuff
    static List<Integer> v83_chair_ids = List.of(
            3010000, 3010001, 3010002, 3010003, 3010004, 3010005, 3010006, 3010007,
            3010008, 3010009, 3010010, 3010011, 3010012, 3010013, 3010014, 3010015,
            3010016, 3010017, 3010018, 3010019, 3010022, 3010023, 3010024, 3010025,
            3010026, 3010028, 3010040, 3010041, 3010043, 3010045, 3010046, 3010047,
            3010057, 3010058, 3010060, 3010061, 3010062, 3010063, 3010064, 3010065,
            3010066, 3010067, 3010069, 3010071, 3010072, 3010073, 3010080, 3010081,
            3010082, 3010083, 3010084, 3010085, 3010092, 3010098, 3010099, 3010101,
            3010106, 3010111, 3010116, 3011000, 3012005, 3012010, 3012011
    );

    static int[][] v83_store_permit_ids = {
            {5140000, 85}, // 85% Chance for basic store
            {5140001, 3}, // 15% for other season style, 3% each
            {5140002, 3},
            {5140003, 3},
            {5140004, 3},
            {5140006, 3}
    };

    public static void EquipBot(Character fakechar, Integer itemId) {
        if (itemId == null) {
            return;
        }
        short dst = getDestinationEquipSlot(itemId);
//        if (dst == -12 || dst == -13 || dst == -15 || dst == -16 || dst == -112 || dst == -113 || dst == -115 | dst == -116) {
//            EquipBotRing(fakechar, itemId, dst);
//            return;
//        }
        EquipItem(fakechar, itemId, dst);
    }

    public static short getDestinationEquipSlot(int itemID) {
        int itemSlot = ItemConstants.getEquipSlotType(itemID);
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        boolean isCash = ii.getEquipStats(itemID).get("cash") == 1;

        // Upstream ItemConstants.isWeapon() only recognises the regular 1302000-1493000
        // range, so cash weapons (v83 "170xxxx") fall through and get mis-slotted into
        // CASH_BASE (-100) instead of CASH_WEAPON (-111). Detect and correct here,
        // scoped to bot equipping so we don't touch upstream Cosmic code.
        if (isCash && isCashWeaponId(itemID)) {
            return (short) -BodyPart.CASH_WEAPON.getValue();
        }

        if (isCash) {
            if (itemSlot == BodyPart.WEAPON.getValue()) {
                itemSlot = BodyPart.CASH_WEAPON.getValue();
            } else {
                itemSlot = itemSlot + BodyPart.CASH_BASE.getValue();
            }
        }
        return (short) -itemSlot;
    }

    /**
     * Matches the v83 cash-weapon ID range. Cash weapons use the 170xxxx prefix
     * (1702xxx sword overrides, 1703xxx axe, etc.), which upstream ItemConstants
     * doesn't know about. Range is kept broad to cover every cash weapon subtype
     * without enumerating each prefix.
     */
    private static boolean isCashWeaponId(int itemId) {
        return itemId >= 1700000 && itemId < 1800000;
    }

    public static void EquipItem(Character fakechar, Integer itemId, short dst) {
        if (itemId == null) {
            return;
        }
        Equip source = (Equip) BotLogic.generateCleanItemEquip(itemId); // Item you are equipping, clean stats
        Equip target = (Equip) fakechar.getInventory(InventoryType.EQUIPPED).getItem(dst); // Currently equipped item
        if (target != null) { // if equip already in, remove that.
            fakechar.getInventory(InventoryType.EQUIPPED).removeSlot(dst);
        }
        source.setPosition(dst); // set the position of Equip to dst (see body part)
        fakechar.getInventory(InventoryType.EQUIPPED).addItemFromDB(source); // Actually equip the item
        fakechar.equipChanged(); // Update fakechar avatar, doesn't work if they're not on screen
    }

    public static int getRandomChairId() {
        return getRandomNumber(v83_chair_ids);
    }

    public static int getRandomStorePermitId() {
        return pickWeighted(v83_store_permit_ids);
    }

    public static int pickWeighted(int[][] items) {
        int total = 0;

        // sum weights
        for (int[] pair : items) {
            total += pair[1];
        }

        int random = new Random().nextInt(total);
        int running = 0;

        for (int[] pair : items) {
            running += pair[1];
            if (random < running) {
                return pair[0]; // return the ID
            }
        }

        throw new IllegalStateException("Should not happen");
    }

}
