-- H5 挂机收益模型改用服务端权威伤害公式后，新增客户端伤害倍率配置
-- （私服客户端实际伤害高于权威公式，按实测校准：公式 20/段 -> 客户端 80/段，倍率 4）
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'H5 Hangup', 'java.lang.Double', 'h5_hangup_damage_multiplier', '4.0', 'h5_hangup_damage_multiplier', '2026-11-01 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `game_config` WHERE `config_code` = 'h5_hangup_damage_multiplier');

-- 旧法师系数配置已废弃，清理
DELETE FROM `lang_resources` WHERE `lang_base` = 'game_config' AND `lang_code` = 'h5_hangup_mage_multiplier';
DELETE FROM `game_config` WHERE `config_code` = 'h5_hangup_mage_multiplier';

-- 中文说明
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'h5_hangup_damage_multiplier', 'H5 挂机客户端伤害倍率', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'h5_hangup_damage_multiplier');

-- 英文说明
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'h5_hangup_damage_multiplier', 'H5 hangup client damage multiplier', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'h5_hangup_damage_multiplier');
