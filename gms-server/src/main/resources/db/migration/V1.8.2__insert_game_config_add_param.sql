-- 第一条记录
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'Game Mechanics', 'java.lang.Boolean', 'use_equipment_level_up_vicious', 'true', 'use_equipment_level_up_vicious', '2025-03-13 17:43:45'
WHERE NOT EXISTS (
    SELECT 1 FROM `game_config` WHERE `config_code` = 'use_equipment_level_up_vicious'
);

-- 第二条记录
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'Game Mechanics', 'java.lang.String', 'use_equipment_level_up_vicious_levelrange_chance', '[[0,129,0.3],[130,255,0.9]]', 'use_equipment_level_up_vicious_LevelRange_chance', '2025-03-13 17:53:59'
WHERE NOT EXISTS (
    SELECT 1 FROM `game_config` WHERE `config_code` = 'use_equipment_level_up_vicious_levelrange_chance'
);

-- 第三条记录
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'Game Mechanics', 'java.lang.Boolean', 'use_equipment_level_up_continuous', 'false', 'use_equipment_level_up_continuous', '2025-03-13 23:13:38'
WHERE NOT EXISTS (
    SELECT 1 FROM `game_config` WHERE `config_code` = 'use_equipment_level_up_continuous'
);

-- 第一条记录
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'use_equipment_level_up_vicious', '装备升级是否减少金锤子已使用次数', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'use_equipment_level_up_vicious'
);

-- 第二条记录
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'use_equipment_level_up_vicious', 'Equipment upgrade reduces vicious numbers', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'use_equipment_level_up_vicious'
);

-- 第三条记录
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'use_equipment_level_up_vicious_levelrange_chance', '装备升级减少金锤子次数的装备要求等级范围和概率。\n配置方法为数组，举例：\n配置两条概率判断，\n1、装备要求等级0~129，概率30%，\n2、装备要求等级130~255，概率90%\n配置如下：\n[[0,129,0.3],[130,255,0.9]]', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'use_equipment_level_up_vicious_levelrange_chance'
);

-- 第四条记录
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'use_equipment_level_up_vicious_levelrange_chance', 'Equipment upgrade reduces the required level range and probability of the Golden Hammer frequency.\r\nThe configuration method is an array, for example:\r\nConfigure two probability judgments,\r\n1. EquipRepLv 0~129, chance 30%,\r\n2. EquipRepLv 130-255, chance 90%\r\nThe configuration is as follows:\r\n[[0,129,0.3],[130,255,0.9]]', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'use_equipment_level_up_vicious_levelrange_chance'
);

-- 第五条记录
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'use_equipment_level_up_continuous', '是否允许装备连续升级', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'use_equipment_level_up_continuous'
);

-- 第六条记录
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en_US', 'game_config', 'use_equipment_level_up_continuous', 'Do you allow continuous equipment upgrades', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en_US' AND `lang_code` = 'use_equipment_level_up_continuous'
);