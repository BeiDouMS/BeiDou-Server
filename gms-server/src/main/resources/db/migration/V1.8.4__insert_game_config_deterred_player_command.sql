-- 是否禁用玩家@指令（true=禁用，GM不受影响）
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'Safe', 'java.lang.Boolean', 'deterred_player_command', 'false', 'deterred_player_command', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `game_config` WHERE `config_code` = 'deterred_player_command'
);

-- 中文内容
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'deterred_player_command', '是否禁用玩家@指令（true=禁用，GM不受影响）', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'deterred_player_command'
);

-- 英文内容
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'deterred_player_command', 'Disable player @ commands (true=disabled, GM unaffected).', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'deterred_player_command'
);
