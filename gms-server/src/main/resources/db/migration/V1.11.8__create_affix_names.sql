CREATE TABLE IF NOT EXISTS `equipment_affix_name`
(
    `affix_code` VARCHAR(32)      NOT NULL,
    `affix_tier` TINYINT UNSIGNED NOT NULL,
    `name_key`   VARCHAR(96)      NOT NULL,
    `priority`   INT              NOT NULL DEFAULT 0,
    `enabled`    TINYINT UNSIGNED NOT NULL DEFAULT 1,
    PRIMARY KEY (`affix_code`, `affix_tier`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO `equipment_affix_name`
    (`affix_code`, `affix_tier`, `name_key`, `priority`)
VALUES
    ('WATK', 1, 'equipment.prefix.watk.t1', 80),
    ('WATK', 2, 'equipment.prefix.watk.t2', 80),
    ('WATK', 3, 'equipment.prefix.watk.t3', 80),
    ('WATK', 4, 'equipment.prefix.watk.t4', 80),
    ('WATK', 5, 'equipment.prefix.watk.t5', 80),
    ('WATK', 6, 'equipment.prefix.watk.t6', 80),
    ('WATK', 7, 'equipment.prefix.watk.t7', 80),
    ('WATK', 8, 'equipment.prefix.watk.t8', 80),
    ('MATK', 1, 'equipment.prefix.matk.t1', 80),
    ('MATK', 2, 'equipment.prefix.matk.t2', 80),
    ('MATK', 3, 'equipment.prefix.matk.t3', 80),
    ('MATK', 4, 'equipment.prefix.matk.t4', 80),
    ('MATK', 5, 'equipment.prefix.matk.t5', 80),
    ('MATK', 6, 'equipment.prefix.matk.t6', 80),
    ('MATK', 7, 'equipment.prefix.matk.t7', 80),
    ('MATK', 8, 'equipment.prefix.matk.t8', 80),
    ('BOSS_DAMAGE', 1, 'equipment.prefix.boss_damage.t1', 100),
    ('BOSS_DAMAGE', 2, 'equipment.prefix.boss_damage.t2', 100),
    ('BOSS_DAMAGE', 3, 'equipment.prefix.boss_damage.t3', 100),
    ('BOSS_DAMAGE', 4, 'equipment.prefix.boss_damage.t4', 100),
    ('BOSS_DAMAGE', 5, 'equipment.prefix.boss_damage.t5', 100),
    ('BOSS_DAMAGE', 6, 'equipment.prefix.boss_damage.t6', 100),
    ('BOSS_DAMAGE', 7, 'equipment.prefix.boss_damage.t7', 100),
    ('BOSS_DAMAGE', 8, 'equipment.prefix.boss_damage.t8', 100),
    ('IGNORE_DEFENSE', 1, 'equipment.prefix.ignore_defense.t1', 95),
    ('IGNORE_DEFENSE', 2, 'equipment.prefix.ignore_defense.t2', 95),
    ('IGNORE_DEFENSE', 3, 'equipment.prefix.ignore_defense.t3', 95),
    ('IGNORE_DEFENSE', 4, 'equipment.prefix.ignore_defense.t4', 95),
    ('IGNORE_DEFENSE', 5, 'equipment.prefix.ignore_defense.t5', 95),
    ('IGNORE_DEFENSE', 6, 'equipment.prefix.ignore_defense.t6', 95),
    ('IGNORE_DEFENSE', 7, 'equipment.prefix.ignore_defense.t7', 95),
    ('IGNORE_DEFENSE', 8, 'equipment.prefix.ignore_defense.t8', 95),
    ('BOSS_DAMAGE_REDUCTION', 1, 'equipment.prefix.boss_damage_reduction.t1', 90),
    ('BOSS_DAMAGE_REDUCTION', 2, 'equipment.prefix.boss_damage_reduction.t2', 90),
    ('BOSS_DAMAGE_REDUCTION', 3, 'equipment.prefix.boss_damage_reduction.t3', 90),
    ('BOSS_DAMAGE_REDUCTION', 4, 'equipment.prefix.boss_damage_reduction.t4', 90),
    ('BOSS_DAMAGE_REDUCTION', 5, 'equipment.prefix.boss_damage_reduction.t5', 90),
    ('BOSS_DAMAGE_REDUCTION', 6, 'equipment.prefix.boss_damage_reduction.t6', 90),
    ('BOSS_DAMAGE_REDUCTION', 7, 'equipment.prefix.boss_damage_reduction.t7', 90),
    ('BOSS_DAMAGE_REDUCTION', 8, 'equipment.prefix.boss_damage_reduction.t8', 90),
    ('HP', 1, 'equipment.prefix.hp.t1', 60),
    ('HP', 2, 'equipment.prefix.hp.t2', 60),
    ('HP', 3, 'equipment.prefix.hp.t3', 60),
    ('HP', 4, 'equipment.prefix.hp.t4', 60),
    ('HP', 5, 'equipment.prefix.hp.t5', 60),
    ('HP', 6, 'equipment.prefix.hp.t6', 60),
    ('HP', 7, 'equipment.prefix.hp.t7', 60),
    ('HP', 8, 'equipment.prefix.hp.t8', 60),
    ('MP', 1, 'equipment.prefix.mp.t1', 60),
    ('MP', 2, 'equipment.prefix.mp.t2', 60),
    ('MP', 3, 'equipment.prefix.mp.t3', 60),
    ('MP', 4, 'equipment.prefix.mp.t4', 60),
    ('MP', 5, 'equipment.prefix.mp.t5', 60),
    ('MP', 6, 'equipment.prefix.mp.t6', 60),
    ('MP', 7, 'equipment.prefix.mp.t7', 60),
    ('MP', 8, 'equipment.prefix.mp.t8', 60),
    ('DROP_RATE', 1, 'equipment.prefix.drop_rate.t1', 50),
    ('DROP_RATE', 2, 'equipment.prefix.drop_rate.t2', 50),
    ('DROP_RATE', 3, 'equipment.prefix.drop_rate.t3', 50),
    ('DROP_RATE', 4, 'equipment.prefix.drop_rate.t4', 50),
    ('DROP_RATE', 5, 'equipment.prefix.drop_rate.t5', 50),
    ('DROP_RATE', 6, 'equipment.prefix.drop_rate.t6', 50),
    ('DROP_RATE', 7, 'equipment.prefix.drop_rate.t7', 50),
    ('DROP_RATE', 8, 'equipment.prefix.drop_rate.t8', 50),
    ('EXP_RATE', 1, 'equipment.prefix.exp_rate.t1', 50),
    ('EXP_RATE', 2, 'equipment.prefix.exp_rate.t2', 50),
    ('EXP_RATE', 3, 'equipment.prefix.exp_rate.t3', 50),
    ('EXP_RATE', 4, 'equipment.prefix.exp_rate.t4', 50),
    ('EXP_RATE', 5, 'equipment.prefix.exp_rate.t5', 50),
    ('EXP_RATE', 6, 'equipment.prefix.exp_rate.t6', 50),
    ('EXP_RATE', 7, 'equipment.prefix.exp_rate.t7', 50),
    ('EXP_RATE', 8, 'equipment.prefix.exp_rate.t8', 50);

INSERT IGNORE INTO `equipment_affix_name`
    (`affix_code`, `affix_tier`, `name_key`, `priority`)
SELECT codes.affix_code,
       tiers.affix_tier,
       CONCAT('equipment.prefix.', LOWER(codes.affix_code), '.t', tiers.affix_tier),
       codes.priority
FROM (
    SELECT 'STR' AS affix_code, 40 AS priority
    UNION ALL SELECT 'DEX', 40
    UNION ALL SELECT 'INT', 40
    UNION ALL SELECT 'LUK', 40
    UNION ALL SELECT 'WDEF', 60
    UNION ALL SELECT 'MDEF', 60
    UNION ALL SELECT 'ACC', 30
    UNION ALL SELECT 'AVOID', 30
    UNION ALL SELECT 'SPEED', 30
    UNION ALL SELECT 'JUMP', 30
    UNION ALL SELECT 'MESO_RATE', 50
) codes
CROSS JOIN (
    SELECT 1 AS affix_tier
    UNION ALL SELECT 2
    UNION ALL SELECT 3
    UNION ALL SELECT 4
    UNION ALL SELECT 5
    UNION ALL SELECT 6
    UNION ALL SELECT 7
    UNION ALL SELECT 8
) tiers;
