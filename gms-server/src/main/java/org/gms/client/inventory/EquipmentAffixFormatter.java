package org.gms.client.inventory;

import org.gms.server.ItemInformationProvider;
import org.gms.util.I18nUtil;

import java.util.List;
import java.util.stream.Collectors;

public final class EquipmentAffixFormatter {
    private EquipmentAffixFormatter() {
    }

    public static String format(Equip equip) {
        String itemName = ItemInformationProvider.getInstance().getName(equip.getItemId());
        String rarity = I18nUtil.getMessage("EquipmentAffixFormatter.rarity." + equip.getRarity());
        List<String> lines = equip.getAffixes().stream()
                .map(EquipmentAffixFormatter::formatAffix)
                .collect(Collectors.toList());

        StringBuilder result = new StringBuilder();
        result.append(I18nUtil.getMessage("EquipmentAffixFormatter.title", itemName));
        result.append('\n').append(I18nUtil.getMessage("EquipmentAffixFormatter.rarity", rarity));
        if (lines.isEmpty()) {
            result.append('\n').append(I18nUtil.getMessage("EquipmentAffixFormatter.empty"));
        } else {
            lines.forEach(line -> result.append('\n').append(line));
        }
        return result.toString();
    }

    private static String formatAffix(EquipmentAffix affix) {
        String label = I18nUtil.getMessage("EquipmentAffixFormatter.affix." + affix.getAffixCode());
        String nameKey = EquipmentAffixGenerator.getAffixNameKey(affix.getAffixCode(), affix.getAffixTier());
        String configuredPrefix = nameKey == null ? null : I18nUtil.getMessage(nameKey);
        String prefix = configuredPrefix == null || configuredPrefix.equals(nameKey)
                ? I18nUtil.getMessage("EquipmentAffixFormatter.prefix." + affix.getAffixTier())
                : configuredPrefix;
        String unit = isRateAffix(affix.getAffixCode()) ? "%" : "";
        return I18nUtil.getMessage(
                "EquipmentAffixFormatter.line",
                prefix,
                label,
                affix.getValue(),
                unit,
                affix.getAffixTier()
        );
    }

    private static boolean isRateAffix(String affixCode) {
        return switch (affixCode) {
            case "BOSS_DAMAGE", "IGNORE_DEFENSE", "DROP_RATE", "EXP_RATE", "MESO_RATE",
                    "BOSS_DAMAGE_REDUCTION" -> true;
            default -> false;
        };
    }
}
