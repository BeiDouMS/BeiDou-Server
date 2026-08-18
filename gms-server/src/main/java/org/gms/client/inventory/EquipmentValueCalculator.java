package org.gms.client.inventory;

import org.gms.server.ItemInformationProvider;

public final class EquipmentValueCalculator {
    private static final double[] RARITY_MULTIPLIERS = {
            1.00, 1.15, 1.35, 1.70, 2.20, 3.00, 4.00
    };
    private static final double[] TIER_PREMIUMS = {
            0.05, 0.08, 0.12, 0.17, 0.24, 0.33, 0.45, 0.60
    };

    private EquipmentValueCalculator() {
    }

    public static int getSalvagePrice(Equip equip) {
        long shopPrice = ItemInformationProvider.getInstance().getWholePrice(equip.getItemId());
        long baseValue = Math.max(1_000L, shopPrice > 0 ? shopPrice * 60L / 100L : 1_000L);
        int rarity = Math.max(0, Math.min(equip.getRarity(), RARITY_MULTIPLIERS.length - 1));
        double value = baseValue * RARITY_MULTIPLIERS[rarity];
        for (EquipmentAffix affix : equip.getAffixes()) {
            int tier = Math.max(1, Math.min(affix.getAffixTier(), TIER_PREMIUMS.length));
            value += baseValue * TIER_PREMIUMS[tier - 1];
        }
        long rounded = Math.round(value / 100.0) * 100L;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1_000L, rounded));
    }
}
