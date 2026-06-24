INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'Debug', 'java.lang.Boolean', 'use_enable_gachapon_log', 'false', 'use_enable_gachapon_log', '2026-06-23 20:25:36'
WHERE NOT EXISTS (
    SELECT 1 FROM `game_config` WHERE `config_code` = 'use_enable_gachapon_log'
);

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'use_enable_gachapon_log', '日志是否打印转蛋机抽奖信息', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'use_enable_gachapon_log'
);

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'use_enable_gachapon_log', 'Whether print gachapon log.', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'use_enable_gachapon_log'
);
