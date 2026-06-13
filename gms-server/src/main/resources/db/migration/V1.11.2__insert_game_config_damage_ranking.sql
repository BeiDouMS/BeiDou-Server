-- 是否开启副本伤害排名功能
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'Game Mechanics', 'java.lang.Boolean', 'damage_ranking', 'false', 'damage_ranking', '2026-06-09 00:00:00'
WHERE NOT EXISTS (
    SELECT 1 FROM `game_config` WHERE `config_code` = 'damage_ranking'
);

-- 中文内容
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'damage_ranking', '是否开启副本伤害排名', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'damage_ranking'
);

-- 英文内容
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'damage_ranking', 'Whether to enable damage ranking', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'damage_ranking'
);