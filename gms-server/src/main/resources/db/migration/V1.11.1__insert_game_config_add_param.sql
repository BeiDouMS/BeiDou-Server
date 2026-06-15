-- 是否开启范围宠吸功能
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'Game Mechanics', 'java.lang.Boolean', 'pet_itemvac', 'false', 'pet_itemvac', '2026-06-01 14:00:00'
WHERE NOT EXISTS (
    SELECT 1 FROM `game_config` WHERE `config_code` = 'pet_itemvac'
);

-- 中文内容
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'pet_itemvac', '是否开启范围宠吸功能', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'pet_itemvac'
);

-- 英文内容
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'use_enable_party_level_limit_lift', 'Whether to enable pets to vacuum items', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'pet_itemvac'
);