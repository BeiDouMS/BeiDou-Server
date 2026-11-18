-- 世界级任务击杀计数倍率，默认保持原行为
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'world', '0', 'java.lang.Integer', 'quest_mob_kill_rate', '1', 'quest_mob_kill_rate', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM `game_config`
    WHERE `config_type` = 'world' AND `config_sub_type` = '0' AND `config_code` = 'quest_mob_kill_rate'
);

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'quest_mob_kill_rate', '任务击杀计数倍率（正整数；默认 1）', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_base` = 'game_config' AND `lang_code` = 'quest_mob_kill_rate'
);

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'quest_mob_kill_rate', 'Quest monster kill-count multiplier (positive integer; default 1)', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_base` = 'game_config' AND `lang_code` = 'quest_mob_kill_rate'
);
