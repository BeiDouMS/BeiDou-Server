package org.gms.client.inventory;

import org.gms.constants.inventory.ItemConstants;
import org.gms.util.Randomizer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EquipmentAffixGenerator {
    private static volatile EquipmentAffixConfig config;

    private EquipmentAffixGenerator() {
    }

    public static Equip generate(Equip equip) {
        return generate(equip, EquipmentDropSource.NORMAL);
    }

    public static Equip generate(Equip equip, EquipmentDropSource source) {
        if (equip.getRarity() != 0 || !equip.getAffixes().isEmpty()) {
            return equip;
        }

        String equipType = resolveEquipType(equip.getItemId());
        if (equipType == null) {
            return equip;
        }

        EquipmentAffixConfig loadedConfig = getConfig();
        EquipmentAffixConfig.Rarity rarity = chooseRarity(loadedConfig.rarities(), source);
        equip.setRarity(rarity.rarity());
        if (rarity.affixCount() == 0) {
            return equip;
        }

        equip.setAffixes(generateAffixes(equip, rarity, List.of()));
        return equip;
    }

    public static Equip reroll(Equip equip, boolean preserveLocked) {
        EquipmentAffixConfig.Rarity rarity = getConfig().rarities().stream()
                .filter(item -> item.rarity() == equip.getRarity())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown equipment rarity: " + equip.getRarity()));
        List<EquipmentAffix> locked = preserveLocked
                ? equip.getAffixes().stream().filter(EquipmentAffix::isLocked).toList()
                : List.of();
        equip.setAffixes(generateAffixes(equip, rarity, locked));
        return equip;
    }

    private static List<EquipmentAffix> generateAffixes(
            Equip equip,
            EquipmentAffixConfig.Rarity rarity,
            List<EquipmentAffix> preserved
    ) {
        String equipType = resolveEquipType(equip.getItemId());
        if (equipType == null || rarity.affixCount() == 0) {
            return List.of();
        }

        EquipmentAffixConfig loadedConfig = getConfig();
        Map<String, EquipmentAffixConfig.Definition> definitions = new HashMap<>();
        for (EquipmentAffixConfig.Definition definition : loadedConfig.definitions()) {
            definitions.put(definition.affixCode(), definition);
        }
        List<EquipmentAffixConfig.PoolEntry> candidates = loadedConfig.poolEntries().stream()
                .filter(entry -> entry.equipType().equals(equipType))
                .filter(entry -> loadedConfig.ranges().stream()
                        .anyMatch(range -> range.affixCode().equals(entry.affixCode())
                                && range.affixTier() <= rarity.maxAffixTier()))
                .filter(entry -> definitions.containsKey(entry.affixCode()))
                .toList();
        Set<String> selectedCodes = new HashSet<>();
        List<EquipmentAffix> affixes = new ArrayList<>(preserved);
        preserved.forEach(affix -> selectedCodes.add(affix.getAffixCode()));

        for (int slot = affixes.size(); slot < rarity.affixCount() && !candidates.isEmpty(); slot++) {
            EquipmentAffixConfig.PoolEntry selected = choosePoolEntry(
                    candidates, selectedCodes, loadedConfig.ranges(), rarity.maxAffixTier(), definitions);
            if (selected == null) {
                break;
            }
            EquipmentAffixConfig.Range range = chooseRange(
                    loadedConfig.ranges(), selected.affixCode(), rarity.maxAffixTier());
            int value = Randomizer.nextInt(range.maxValue() - range.minValue() + 1) + range.minValue();
            affixes.add(EquipmentAffix.builder()
                    .slotIndex(slot)
                    .affixCode(selected.affixCode())
                    .affixTier(range.affixTier())
                    .value(value)
                    .rollSeed(Randomizer.nextInt(Integer.MAX_VALUE))
                    .build());
            selectedCodes.add(selected.affixCode());
        }
        return affixes;
    }

    public static void reload() {
        config = null;
    }

    public static String getAffixNameKey(String affixCode, int affixTier) {
        return getConfig().names().stream()
                .filter(name -> name.affixCode().equals(affixCode) && name.affixTier() == affixTier)
                .map(EquipmentAffixConfig.Name::nameKey)
                .findFirst()
                .orElse(null);
    }

    private static EquipmentAffixConfig getConfig() {
        EquipmentAffixConfig loaded = config;
        if (loaded != null) {
            return loaded;
        }
        synchronized (EquipmentAffixGenerator.class) {
            if (config == null) {
                try {
                    config = EquipmentAffixConfigLoader.load();
                } catch (SQLException e) {
                    throw new IllegalStateException("Unable to load equipment affix configuration.", e);
                }
            }
            return config;
        }
    }

    private static EquipmentAffixConfig.Rarity chooseRarity(
            List<EquipmentAffixConfig.Rarity> rarities,
            EquipmentDropSource source
    ) {
        int totalWeight = rarities.stream().mapToInt(rarity -> getDropWeight(rarity, source)).sum();
        if (totalWeight <= 0) {
            throw new IllegalStateException("Equipment rarity configuration has no positive drop weight.");
        }
        int roll = Randomizer.nextInt(totalWeight);
        for (EquipmentAffixConfig.Rarity rarity : rarities) {
            roll -= getDropWeight(rarity, source);
            if (roll < 0) {
                return rarity;
            }
        }
        throw new IllegalStateException("Equipment rarity selection failed.");
    }

    private static int getDropWeight(EquipmentAffixConfig.Rarity rarity, EquipmentDropSource source) {
        return switch (source) {
            case NORMAL -> rarity.dropWeight();
            case BOSS -> rarity.bossDropWeight();
            case DUNGEON -> rarity.dungeonDropWeight();
            case GACHAPON -> rarity.gachaponDropWeight();
        };
    }

    private static EquipmentAffixConfig.PoolEntry choosePoolEntry(
            List<EquipmentAffixConfig.PoolEntry> candidates,
            Set<String> selectedCodes,
            List<EquipmentAffixConfig.Range> ranges,
            int maxAffixTier,
            Map<String, EquipmentAffixConfig.Definition> definitions
    ) {
        List<EquipmentAffixConfig.PoolEntry> available = candidates.stream()
                .filter(entry -> !selectedCodes.contains(entry.affixCode())
                        || ranges.stream().anyMatch(range -> range.affixCode().equals(entry.affixCode())
                        && range.affixTier() <= maxAffixTier && range.allowDuplicate())
                        || definitions.get(entry.affixCode()).maxPerItem() > 1)
                .toList();
        if (available.isEmpty()) {
            return null;
        }

        int totalWeight = available.stream().mapToInt(EquipmentAffixConfig.PoolEntry::weight).sum();
        if (totalWeight <= 0) {
            throw new IllegalStateException("Equipment affix pool has no positive weight.");
        }
        int roll = Randomizer.nextInt(totalWeight);
        for (EquipmentAffixConfig.PoolEntry entry : available) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry;
            }
        }
        throw new IllegalStateException("Equipment affix selection failed.");
    }

    private static EquipmentAffixConfig.Range chooseRange(
            List<EquipmentAffixConfig.Range> ranges,
            String affixCode,
            int maxAffixTier
    ) {
        List<EquipmentAffixConfig.Range> available = ranges.stream()
                .filter(range -> range.affixCode().equals(affixCode))
                .filter(range -> range.affixTier() <= maxAffixTier)
                .toList();
        int totalWeight = available.stream().mapToInt(EquipmentAffixConfig.Range::weight).sum();
        if (available.isEmpty() || totalWeight <= 0) {
            throw new IllegalStateException("Equipment affix has no valid tier range: " + affixCode);
        }
        int roll = Randomizer.nextInt(totalWeight);
        for (EquipmentAffixConfig.Range range : available) {
            roll -= range.weight();
            if (roll < 0) {
                return range;
            }
        }
        throw new IllegalStateException("Equipment affix tier selection failed: " + affixCode);
    }

    private static String resolveEquipType(int itemId) {
        if (ItemConstants.isWeapon(itemId)) {
            return "WEAPON";
        }
        int category = itemId / 10000;
        return switch (category) {
            case 100 -> "HAT";
            case 104, 105 -> "TOP";
            case 106 -> "BOTTOM";
            case 107 -> "SHOES";
            case 108 -> "GLOVE";
            case 110 -> "CAPE";
            case 111 -> "RING";
            case 112 -> "PENDANT";
            case 101, 102, 103 -> "ACCESSORY";
            default -> null;
        };
    }
}
