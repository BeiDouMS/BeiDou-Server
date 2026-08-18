package org.gms.client.inventory;

import org.gms.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class EquipmentAffixConfigLoader {
    private EquipmentAffixConfigLoader() {
    }

    public static EquipmentAffixConfig load() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return new EquipmentAffixConfig(
                    loadRarities(connection),
                    loadDefinitions(connection),
                    loadRanges(connection),
                    loadPoolEntries(connection),
                    loadNames(connection)
            );
        }
    }

    private static List<EquipmentAffixConfig.Rarity> loadRarities(Connection connection) throws SQLException {
        List<EquipmentAffixConfig.Rarity> rarities = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT rarity, code, name_key, drop_weight, boss_drop_weight, dungeon_drop_weight,
                       gachapon_drop_weight,
                       affix_count, value_multiplier, max_affix_tier
                FROM equipment_rarity_config
                WHERE enabled = 1
                ORDER BY rarity
                """);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rarities.add(new EquipmentAffixConfig.Rarity(
                        resultSet.getByte("rarity"),
                        resultSet.getString("code"),
                        resultSet.getString("name_key"),
                        resultSet.getInt("drop_weight"),
                        resultSet.getInt("boss_drop_weight"),
                        resultSet.getInt("dungeon_drop_weight"),
                        resultSet.getInt("gachapon_drop_weight"),
                        resultSet.getByte("affix_count"),
                        resultSet.getInt("value_multiplier"),
                        resultSet.getByte("max_affix_tier")
                ));
            }
        }
        return rarities;
    }

    private static List<EquipmentAffixConfig.Definition> loadDefinitions(Connection connection) throws SQLException {
        List<EquipmentAffixConfig.Definition> definitions = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT affix_code, name_key, value_type, effect_type, max_per_item
                FROM equipment_affix_definition
                WHERE enabled = 1
                ORDER BY display_order, affix_code
                """);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                definitions.add(new EquipmentAffixConfig.Definition(
                        resultSet.getString("affix_code"),
                        resultSet.getString("name_key"),
                        resultSet.getString("value_type"),
                        resultSet.getString("effect_type"),
                        resultSet.getByte("max_per_item")
                ));
            }
        }
        return definitions;
    }

    private static List<EquipmentAffixConfig.Range> loadRanges(Connection connection) throws SQLException {
        List<EquipmentAffixConfig.Range> ranges = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT affix_code, affix_tier, min_value, max_value, weight, allow_duplicate
                FROM equipment_affix_range
                WHERE enabled = 1
                ORDER BY affix_tier, affix_code
                """);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ranges.add(new EquipmentAffixConfig.Range(
                        resultSet.getString("affix_code"),
                        resultSet.getByte("affix_tier"),
                        resultSet.getInt("min_value"),
                        resultSet.getInt("max_value"),
                        resultSet.getInt("weight"),
                        resultSet.getBoolean("allow_duplicate")
                ));
            }
        }
        return ranges;
    }

    private static List<EquipmentAffixConfig.PoolEntry> loadPoolEntries(Connection connection) throws SQLException {
        List<EquipmentAffixConfig.PoolEntry> poolEntries = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT equip_type, affix_code, weight
                FROM equipment_affix_pool
                WHERE enabled = 1
                ORDER BY equip_type, affix_code
                """);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                poolEntries.add(new EquipmentAffixConfig.PoolEntry(
                        resultSet.getString("equip_type"),
                        resultSet.getString("affix_code"),
                        resultSet.getInt("weight")
                ));
            }
        }
        return poolEntries;
    }

    private static List<EquipmentAffixConfig.Name> loadNames(Connection connection) throws SQLException {
        List<EquipmentAffixConfig.Name> names = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT affix_code, affix_tier, name_key, priority
                FROM equipment_affix_name
                WHERE enabled = 1
                """);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                names.add(new EquipmentAffixConfig.Name(
                        resultSet.getString("affix_code"),
                        resultSet.getByte("affix_tier"),
                        resultSet.getString("name_key"),
                        resultSet.getInt("priority")
                ));
            }
        }
        return names;
    }
}
