-- H5 挂机：地图目录、怪物属性、掉落白名单，扩展结算收益结构

-- 角色当前选择的挂机地图
ALTER TABLE `h5_hangup_state`
    ADD COLUMN `map_id` INT NOT NULL DEFAULT 0 COMMENT '当前挂机地图' AFTER `world`;

-- 待发放物品明细（JSON 数组：[{itemId, quantity}]）
ALTER TABLE `h5_hangup_reward`
    ADD COLUMN `items_json` TEXT NULL COMMENT '待发放物品 JSON' AFTER `exp`;

-- 野外地图目录（由客户端 WZ 解析生成，见 h5/tools/gen-catalog.js）
CREATE TABLE IF NOT EXISTS `h5_hangup_map`
(
    `map_id`           INT          NOT NULL,
    `region`           VARCHAR(32)  NOT NULL COMMENT '地区 key（String.wz/Map.img 一级节点）',
    `region_name`      VARCHAR(32)  NOT NULL COMMENT '地区中文名',
    `region_order`     INT          NOT NULL DEFAULT 0,
    `name`             VARCHAR(64)  NOT NULL COMMENT '地图名',
    `street_name`      VARCHAR(64)  NOT NULL DEFAULT '',
    `min_level`        INT          NOT NULL DEFAULT 0 COMMENT '最低怪物等级',
    `max_level`        INT          NOT NULL DEFAULT 0 COMMENT '最高怪物等级',
    `spawn_count`      INT          NOT NULL DEFAULT 0 COMMENT '场内同时存在怪物数',
    `respawn_seconds`  INT          NOT NULL DEFAULT 0 COMMENT '加权重生秒数',
    `has_boss`         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`map_id`),
    INDEX `idx_h5_map_region` (`region`, `region_order`),
    INDEX `idx_h5_map_level` (`min_level`, `max_level`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 地图刷怪点
CREATE TABLE IF NOT EXISTS `h5_hangup_map_mob`
(
    `map_id`      INT     NOT NULL,
    `mob_id`      INT     NOT NULL,
    `spawn_count` INT     NOT NULL DEFAULT 1,
    `mob_time`    INT     NOT NULL DEFAULT 0 COMMENT '0 表示使用地图默认重生间隔',
    `hide`        TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`map_id`, `mob_id`, `mob_time`),
    INDEX `idx_h5_map_mob_mob` (`mob_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 怪物属性（客户端 Mob.wz 解析）
CREATE TABLE IF NOT EXISTS `h5_hangup_mob`
(
    `mob_id` INT          NOT NULL,
    `name`   VARCHAR(64)  NOT NULL DEFAULT '',
    `level`  INT          NOT NULL DEFAULT 1,
    `hp`     INT          NOT NULL DEFAULT 1,
    `exp`    INT          NOT NULL DEFAULT 0,
    `boss`   TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`mob_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 登出时的角色战斗力快照（离线结算用）
CREATE TABLE IF NOT EXISTS `h5_hangup_stat`
(
    `character_id` INT      NOT NULL,
    `level`        INT      NOT NULL DEFAULT 1,
    `job`          INT      NOT NULL DEFAULT 0,
    `str`          INT      NOT NULL DEFAULT 0,
    `dex`          INT      NOT NULL DEFAULT 0,
    `int`          INT      NOT NULL DEFAULT 0,
    `luk`          INT      NOT NULL DEFAULT 0,
    `watk`         INT      NOT NULL DEFAULT 0,
    `matk`         INT      NOT NULL DEFAULT 0,
    `update_at`    DATETIME NOT NULL,
    PRIMARY KEY (`character_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 掉落白名单：玩家勾选希望自动拾取的物品，勾选数量不设上限
CREATE TABLE IF NOT EXISTS `h5_hangup_whitelist`
(
    `character_id` INT      NOT NULL,
    `item_id`      INT      NOT NULL,
    `update_at`    DATETIME NOT NULL,
    PRIMARY KEY (`character_id`, `item_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 收益模型参数（服务端权威）
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'H5 Hangup', 'java.lang.Double', 'h5_hangup_eta', '0.65', 'h5_hangup_eta', '2026-11-01 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `game_config` WHERE `config_code` = 'h5_hangup_eta');

INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'H5 Hangup', 'java.lang.Double', 'h5_hangup_attack_speed', '1.6', 'h5_hangup_attack_speed', '2026-11-01 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `game_config` WHERE `config_code` = 'h5_hangup_attack_speed');

INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'H5 Hangup', 'java.lang.Double', 'h5_hangup_mage_multiplier', '1.2', 'h5_hangup_mage_multiplier', '2026-11-01 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `game_config` WHERE `config_code` = 'h5_hangup_mage_multiplier');

-- 中文说明
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'h5_hangup_eta', 'H5 挂机有效击杀系数', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'h5_hangup_eta');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'h5_hangup_attack_speed', 'H5 挂机每秒攻击次数', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'h5_hangup_attack_speed');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'h5_hangup_mage_multiplier', 'H5 挂机法师战斗力系数', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'h5_hangup_mage_multiplier');

-- 英文说明
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'h5_hangup_eta', 'H5 hangup effective kill ratio', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'h5_hangup_eta');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'h5_hangup_attack_speed', 'H5 hangup attacks per second', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'h5_hangup_attack_speed');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'h5_hangup_mage_multiplier', 'H5 hangup mage power multiplier', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'h5_hangup_mage_multiplier');

-- 错误与提示文案
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'exception', 'H5_HANGUP_MAP_NOT_FOUND', '挂机地图不存在', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'H5_HANGUP_MAP_NOT_FOUND');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'exception', 'H5_HANGUP_MAP_NOT_FOUND', 'Hangup map not found', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'H5_HANGUP_MAP_NOT_FOUND');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'exception', 'H5_HANGUP_MAP_SELECT_REQUIRED', '请先选择挂机地图', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'H5_HANGUP_MAP_SELECT_REQUIRED');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'exception', 'H5_HANGUP_MAP_SELECT_REQUIRED', 'Please select a hangup map first', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'H5_HANGUP_MAP_SELECT_REQUIRED');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'message', 'H5.Hangup.ClaimWithItems', '收到 H5 挂机收益：%d 金币、%d 经验', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'H5.Hangup.ClaimWithItems');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'message', 'H5.Hangup.ClaimWithItems', 'H5 hangup reward: %d mesos, %d exp', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'H5.Hangup.ClaimWithItems');
