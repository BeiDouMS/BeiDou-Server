package soloMapling.Casino;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Configuration for casino chip items and their NPC shop.
 * Chips are ETC stamp items repurposed as casino currency,
 * buyable and sellable at equal prices for lossless meso exchange.
 *
 * To change chip values or add new chips, edit the CHIPS map below.
 */
public class CasinoChipConfig {

    // NPC ID for the casino chip exchange NPC.
    // 9201066 = an unused NPC in GMS v83 (Wedding NPC variant).
    // Change this to any NPC ID that exists in the client WZ but isn't used on your maps.
    public static final int CASINO_NPC_ID = 9000055;

    // Shop ID for the casino chip shop (must not collide with existing shops in DB).
    public static final int CASINO_SHOP_ID = 9999001;

    // Chip definitions: itemId -> meso value (buy AND sell price)
    private static final Map<Integer, Integer> CHIPS = new LinkedHashMap<>();

    static {
        CHIPS.put(4002000, 10_000);     // Snail Stamp      = 10k   chip
        CHIPS.put(4002001, 50_000);     // Blue Snail Stamp  = 50k   chip
        CHIPS.put(4002002, 250_000);    // Stump Stamp       = 250k  chip
        CHIPS.put(4002003, 1_000_000);  // Slime Stamp       = 1m    chip
    }

    public static Map<Integer, Integer> getChips() {
        return CHIPS;
    }

    public static Set<Integer> getChipItemIds() {
        return CHIPS.keySet();
    }

    public static boolean isCasinoChip(int itemId) {
        return CHIPS.containsKey(itemId);
    }

    public static int getChipPrice(int itemId) {
        return CHIPS.getOrDefault(itemId, -1);
    }
}
