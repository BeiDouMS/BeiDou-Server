-- 是否允许跳过副本关卡谜题
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'Game Mechanics', 'java.lang.Boolean', 'use_enable_stage_skip', 'false', 'use_enable_stage_skip', '2026-06-09 14:00:00'
WHERE NOT EXISTS (
    SELECT 1 FROM `game_config` WHERE `config_code` = 'use_enable_stage_skip'
);

-- 中文
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'use_enable_stage_skip', '是否允许跳过副本关卡谜题', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'use_enable_stage_skip'
);

-- 英文
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'use_enable_stage_skip', 'Allow skipping stage puzzles in expeditions', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'use_enable_stage_skip'
);
