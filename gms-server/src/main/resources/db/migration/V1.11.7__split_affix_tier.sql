ALTER TABLE `equipment_rarity_config`
    ADD COLUMN `max_affix_tier` TINYINT UNSIGNED NOT NULL DEFAULT 0
        COMMENT '该装备品质允许出现的最高词条品质';

UPDATE `equipment_rarity_config`
SET `max_affix_tier` = CASE `rarity`
    WHEN 0 THEN 0
    WHEN 1 THEN 2
    WHEN 2 THEN 4
    WHEN 3 THEN 5
    WHEN 4 THEN 6
    ELSE 0
END;

INSERT INTO `equipment_rarity_config`
    (`rarity`, `code`, `name_key`, `drop_weight`, `affix_count`, `max_affix_tier`)
VALUES
    (5, 'ANCIENT', 'equipment.rarity.ancient', 8, 5, 7),
    (6, 'MYTHIC', 'equipment.rarity.mythic', 2, 6, 8);

ALTER TABLE `equipment_affix_range`
    ADD COLUMN `affix_tier` TINYINT UNSIGNED NOT NULL DEFAULT 1
        COMMENT '词条自身品质';

UPDATE `equipment_affix_range`
SET `affix_tier` = `rarity`;

ALTER TABLE `equipment_affix_range`
    DROP INDEX `uk_affix_rarity`,
    ADD UNIQUE KEY `uk_affix_tier` (`affix_code`, `affix_tier`);

ALTER TABLE `inventory_equipment_affix`
    ADD COLUMN `affix_tier` TINYINT UNSIGNED NOT NULL DEFAULT 1
        COMMENT '词条自身品质';

UPDATE `inventory_equipment_affix`
SET `affix_tier` = 1
WHERE `affix_tier` = 0;
