package org.gms.client.inventory;

import java.util.List;

public record EquipmentAffixConfig(
        List<Rarity> rarities,
        List<Definition> definitions,
        List<Range> ranges,
        List<PoolEntry> poolEntries,
        List<Name> names
) {
    public record Rarity(
            byte rarity,
            String code,
            String nameKey,
            int dropWeight,
            int bossDropWeight,
            int dungeonDropWeight,
            int gachaponDropWeight,
            byte affixCount,
            int valueMultiplier,
            byte maxAffixTier
    ) {
    }

    public record Definition(
            String affixCode,
            String nameKey,
            String valueType,
            String effectType,
            byte maxPerItem
    ) {
    }

    public record Range(
            String affixCode,
            byte affixTier,
            int minValue,
            int maxValue,
            int weight,
            boolean allowDuplicate
    ) {
    }

    public record PoolEntry(
            String equipType,
            String affixCode,
            int weight
    ) {
    }

    public record Name(
            String affixCode,
            byte affixTier,
            String nameKey,
            int priority
    ) {
    }
}
