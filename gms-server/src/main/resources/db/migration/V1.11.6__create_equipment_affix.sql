ALTER TABLE `inventoryequipment`
    ADD COLUMN `rarity` TINYINT UNSIGNED NOT NULL DEFAULT 0
        COMMENT '0普通 1精良 2稀有 3史诗 4传说';

CREATE TABLE IF NOT EXISTS `equipment_rarity_config`
(
    `rarity`           TINYINT UNSIGNED NOT NULL,
    `code`             VARCHAR(32)      NOT NULL,
    `name_key`         VARCHAR(64)      NOT NULL,
    `drop_weight`      INT UNSIGNED     NOT NULL DEFAULT 0,
    `affix_count`      TINYINT UNSIGNED NOT NULL DEFAULT 0,
    `value_multiplier` INT UNSIGNED     NOT NULL DEFAULT 100,
    `enabled`          TINYINT UNSIGNED NOT NULL DEFAULT 1,
    PRIMARY KEY (`rarity`),
    UNIQUE KEY `uk_rarity_code` (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `equipment_affix_definition`
(
    `affix_code`   VARCHAR(32)      NOT NULL,
    `name_key`     VARCHAR(64)      NOT NULL,
    `value_type`   VARCHAR(16)      NOT NULL,
    `effect_type`  VARCHAR(32)      NOT NULL,
    `max_per_item` TINYINT UNSIGNED NOT NULL DEFAULT 1,
    `display_order` INT UNSIGNED    NOT NULL DEFAULT 0,
    `enabled`      TINYINT UNSIGNED NOT NULL DEFAULT 1,
    PRIMARY KEY (`affix_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `equipment_affix_range`
(
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `affix_code`      VARCHAR(32)      NOT NULL,
    `rarity`          TINYINT UNSIGNED NOT NULL,
    `min_value`       INT              NOT NULL,
    `max_value`       INT              NOT NULL,
    `weight`          INT UNSIGNED     NOT NULL DEFAULT 100,
    `allow_duplicate` TINYINT UNSIGNED NOT NULL DEFAULT 0,
    `enabled`         TINYINT UNSIGNED NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_affix_rarity` (`affix_code`, `rarity`),
    KEY `idx_range_rarity` (`rarity`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `equipment_affix_pool`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `equip_type` VARCHAR(32)      NOT NULL,
    `affix_code` VARCHAR(32)      NOT NULL,
    `weight`     INT UNSIGNED     NOT NULL DEFAULT 100,
    `enabled`    TINYINT UNSIGNED NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_equip_affix` (`equip_type`, `affix_code`),
    KEY `idx_pool_equip_type` (`equip_type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `inventory_equipment_affix`
(
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `inventoryitemid` INT(10) UNSIGNED NOT NULL,
    `slot_index`      TINYINT UNSIGNED NOT NULL,
    `affix_code`      VARCHAR(32)      NOT NULL,
    `affix_value`     INT              NOT NULL,
    `roll_seed`       BIGINT           NOT NULL,
    `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_item_slot` (`inventoryitemid`, `slot_index`),
    KEY `idx_affix_inventoryitem` (`inventoryitemid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO `equipment_rarity_config`
    (`rarity`, `code`, `name_key`, `drop_weight`, `affix_count`)
VALUES
    (0, 'NORMAL', 'equipment.rarity.normal', 7200, 0),
    (1, 'GOOD', 'equipment.rarity.good', 1600, 1),
    (2, 'RARE', 'equipment.rarity.rare', 800, 2),
    (3, 'EPIC', 'equipment.rarity.epic', 350, 3),
    (4, 'LEGENDARY', 'equipment.rarity.legendary', 50, 3);

INSERT INTO `equipment_affix_definition`
    (`affix_code`, `name_key`, `value_type`, `effect_type`, `max_per_item`, `display_order`)
VALUES
    ('STR', 'equipment.affix.str', 'FLAT', 'ATTRIBUTE', 1, 10),
    ('DEX', 'equipment.affix.dex', 'FLAT', 'ATTRIBUTE', 1, 20),
    ('INT', 'equipment.affix.int', 'FLAT', 'ATTRIBUTE', 1, 30),
    ('LUK', 'equipment.affix.luk', 'FLAT', 'ATTRIBUTE', 1, 40),
    ('HP', 'equipment.affix.hp', 'FLAT', 'ATTRIBUTE', 1, 50),
    ('MP', 'equipment.affix.mp', 'FLAT', 'ATTRIBUTE', 1, 60),
    ('WATK', 'equipment.affix.watk', 'FLAT', 'ATTRIBUTE', 1, 70),
    ('MATK', 'equipment.affix.matk', 'FLAT', 'ATTRIBUTE', 1, 80),
    ('WDEF', 'equipment.affix.wdef', 'FLAT', 'ATTRIBUTE', 1, 90),
    ('MDEF', 'equipment.affix.mdef', 'FLAT', 'ATTRIBUTE', 1, 100),
    ('ACC', 'equipment.affix.acc', 'FLAT', 'ATTRIBUTE', 1, 110),
    ('AVOID', 'equipment.affix.avoid', 'FLAT', 'ATTRIBUTE', 1, 120),
    ('SPEED', 'equipment.affix.speed', 'FLAT', 'ATTRIBUTE', 1, 130),
    ('JUMP', 'equipment.affix.jump', 'FLAT', 'ATTRIBUTE', 1, 140),
    ('BOSS_DAMAGE', 'equipment.affix.boss_damage', 'PERCENT', 'BOSS_DAMAGE', 1, 150),
    ('IGNORE_DEFENSE', 'equipment.affix.ignore_defense', 'PERCENT', 'IGNORE_DEFENSE', 1, 160),
    ('DROP_RATE', 'equipment.affix.drop_rate', 'PERCENT', 'DROP_RATE', 1, 170),
    ('EXP_RATE', 'equipment.affix.exp_rate', 'PERCENT', 'EXP_RATE', 1, 180),
    ('MESO_RATE', 'equipment.affix.meso_rate', 'PERCENT', 'MESO_RATE', 1, 190),
    ('BOSS_DAMAGE_REDUCTION', 'equipment.affix.boss_damage_reduction', 'PERCENT', 'BOSS_DAMAGE_REDUCTION', 1, 200);

INSERT INTO `equipment_affix_range`
    (`affix_code`, `rarity`, `min_value`, `max_value`, `weight`)
VALUES
    ('STR', 1, 3, 8, 100), ('STR', 2, 8, 18, 100), ('STR', 3, 18, 35, 100), ('STR', 4, 35, 60, 100),
    ('DEX', 1, 3, 8, 100), ('DEX', 2, 8, 18, 100), ('DEX', 3, 18, 35, 100), ('DEX', 4, 35, 60, 100),
    ('INT', 1, 3, 8, 100), ('INT', 2, 8, 18, 100), ('INT', 3, 18, 35, 100), ('INT', 4, 35, 60, 100),
    ('LUK', 1, 3, 8, 100), ('LUK', 2, 8, 18, 100), ('LUK', 3, 18, 35, 100), ('LUK', 4, 35, 60, 100),
    ('HP', 1, 80, 180, 100), ('HP', 2, 180, 400, 100), ('HP', 3, 400, 800, 100), ('HP', 4, 800, 1500, 100),
    ('MP', 1, 80, 180, 100), ('MP', 2, 180, 400, 100), ('MP', 3, 400, 800, 100), ('MP', 4, 800, 1500, 100),
    ('WATK', 1, 2, 5, 100), ('WATK', 2, 5, 10, 100), ('WATK', 3, 10, 18, 100), ('WATK', 4, 18, 30, 100),
    ('MATK', 1, 2, 5, 100), ('MATK', 2, 5, 10, 100), ('MATK', 3, 10, 18, 100), ('MATK', 4, 18, 30, 100),
    ('WDEF', 1, 15, 40, 100), ('WDEF', 2, 40, 90, 100), ('WDEF', 3, 90, 180, 100), ('WDEF', 4, 180, 320, 100),
    ('MDEF', 1, 15, 40, 100), ('MDEF', 2, 40, 90, 100), ('MDEF', 3, 90, 180, 100), ('MDEF', 4, 180, 320, 100),
    ('ACC', 1, 3, 8, 100), ('ACC', 2, 8, 18, 100), ('ACC', 3, 18, 35, 100), ('ACC', 4, 35, 60, 100),
    ('AVOID', 1, 3, 8, 100), ('AVOID', 2, 8, 18, 100), ('AVOID', 3, 18, 35, 100), ('AVOID', 4, 35, 60, 100),
    ('SPEED', 1, 1, 2, 100), ('SPEED', 2, 2, 4, 100), ('SPEED', 3, 4, 7, 100), ('SPEED', 4, 7, 10, 100),
    ('JUMP', 1, 1, 2, 100), ('JUMP', 2, 2, 4, 100), ('JUMP', 3, 4, 7, 100), ('JUMP', 4, 7, 10, 100),
    ('BOSS_DAMAGE', 1, 3, 5, 80), ('BOSS_DAMAGE', 2, 6, 10, 80), ('BOSS_DAMAGE', 3, 12, 18, 80), ('BOSS_DAMAGE', 4, 20, 32, 80),
    ('IGNORE_DEFENSE', 1, 3, 5, 70), ('IGNORE_DEFENSE', 2, 6, 10, 70), ('IGNORE_DEFENSE', 3, 12, 18, 70), ('IGNORE_DEFENSE', 4, 20, 30, 70),
    ('DROP_RATE', 1, 5, 10, 70), ('DROP_RATE', 2, 10, 20, 70), ('DROP_RATE', 3, 20, 35, 70), ('DROP_RATE', 4, 35, 60, 70),
    ('EXP_RATE', 1, 5, 10, 70), ('EXP_RATE', 2, 10, 20, 70), ('EXP_RATE', 3, 20, 35, 70), ('EXP_RATE', 4, 35, 60, 70),
    ('MESO_RATE', 1, 5, 10, 70), ('MESO_RATE', 2, 10, 20, 70), ('MESO_RATE', 3, 20, 35, 70), ('MESO_RATE', 4, 35, 60, 70),
    ('BOSS_DAMAGE_REDUCTION', 1, 3, 5, 70), ('BOSS_DAMAGE_REDUCTION', 2, 6, 10, 70),
    ('BOSS_DAMAGE_REDUCTION', 3, 12, 18, 70), ('BOSS_DAMAGE_REDUCTION', 4, 20, 30, 70);

INSERT INTO `equipment_affix_pool`
    (`equip_type`, `affix_code`, `weight`)
VALUES
    ('WEAPON', 'WATK', 100), ('WEAPON', 'MATK', 100), ('WEAPON', 'BOSS_DAMAGE', 80),
    ('WEAPON', 'IGNORE_DEFENSE', 70), ('WEAPON', 'STR', 30), ('WEAPON', 'DEX', 30),
    ('WEAPON', 'INT', 30), ('WEAPON', 'LUK', 30),
    ('HAT', 'HP', 100), ('HAT', 'MP', 80), ('HAT', 'WDEF', 100), ('HAT', 'MDEF', 100),
    ('HAT', 'STR', 50), ('HAT', 'DEX', 50), ('HAT', 'INT', 50), ('HAT', 'LUK', 50),
    ('TOP', 'HP', 100), ('TOP', 'MP', 80), ('TOP', 'WDEF', 100), ('TOP', 'MDEF', 100), ('TOP', 'BOSS_DAMAGE_REDUCTION', 40),
    ('BOTTOM', 'HP', 100), ('BOTTOM', 'MP', 80), ('BOTTOM', 'WDEF', 100), ('BOTTOM', 'MDEF', 100), ('BOTTOM', 'BOSS_DAMAGE_REDUCTION', 40),
    ('SHOES', 'SPEED', 100), ('SHOES', 'JUMP', 100), ('SHOES', 'HP', 70), ('SHOES', 'MP', 70),
    ('GLOVE', 'WATK', 100), ('GLOVE', 'MATK', 100), ('GLOVE', 'BOSS_DAMAGE', 70),
    ('CAPE', 'HP', 80), ('CAPE', 'MP', 80), ('CAPE', 'WATK', 60), ('CAPE', 'MATK', 60),
    ('CAPE', 'BOSS_DAMAGE', 70), ('CAPE', 'IGNORE_DEFENSE', 50),
    ('RING', 'STR', 100), ('RING', 'DEX', 100), ('RING', 'INT', 100), ('RING', 'LUK', 100),
    ('RING', 'HP', 80), ('RING', 'MP', 80), ('RING', 'BOSS_DAMAGE', 60),
    ('RING', 'DROP_RATE', 60), ('RING', 'EXP_RATE', 60),
    ('PENDANT', 'HP', 100), ('PENDANT', 'MP', 100), ('PENDANT', 'WDEF', 80),
    ('PENDANT', 'MDEF', 80), ('PENDANT', 'BOSS_DAMAGE', 70), ('PENDANT', 'IGNORE_DEFENSE', 60),
    ('PENDANT', 'BOSS_DAMAGE_REDUCTION', 50),
    ('ACCESSORY', 'BOSS_DAMAGE', 100), ('ACCESSORY', 'IGNORE_DEFENSE', 90),
    ('ACCESSORY', 'DROP_RATE', 70), ('ACCESSORY', 'EXP_RATE', 70),
    ('ACCESSORY', 'MESO_RATE', 70),
    ('ACCESSORY', 'STR', 50), ('ACCESSORY', 'DEX', 50), ('ACCESSORY', 'INT', 50), ('ACCESSORY', 'LUK', 50);
