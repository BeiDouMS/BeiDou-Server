-- 是否解除组队副本默认等级范围限制，开启时允许任意等级的玩家进入副本
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'Game Mechanics', 'java.lang.Boolean', 'use_enable_party_level_limit_lift', 'true', 'use_enable_party_level_limit_lift', '2025-04-02 16:56:57'
WHERE NOT EXISTS (
    SELECT 1 FROM `game_config` WHERE `config_code` = 'use_enable_party_level_limit_lift'
);

-- 中文内容
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'use_enable_party_level_limit_lift', '是否解除组队副本默认等级范围限制，开启时允许任意等级的玩家进入副本', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'use_enable_party_level_limit_lift'
);

-- 英文内容
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'use_enable_party_level_limit_lift', 'Should the default level range limit for teaming up dungeons be lifted, allowing players of any level to enter the dungeon when enableds', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'use_enable_party_level_limit_lift'
);