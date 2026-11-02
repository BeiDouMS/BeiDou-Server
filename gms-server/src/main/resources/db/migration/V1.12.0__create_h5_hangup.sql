-- H5 挂机收益：结算状态表（每个角色一行）
CREATE TABLE IF NOT EXISTS `h5_hangup_state`
(
    `character_id`     INT      NOT NULL,
    `account_id`       INT      NOT NULL,
    `world`            INT      NOT NULL DEFAULT 0,
    `last_settle_time` DATETIME NOT NULL COMMENT '上次结算到的时刻',
    `daily_date`       DATE     NOT NULL COMMENT '日上限统计日期',
    `daily_seconds`    INT      NOT NULL DEFAULT 0 COMMENT '当日已结算挂机秒数',
    `total_seconds`    BIGINT   NOT NULL DEFAULT 0 COMMENT '累计挂机秒数',
    `update_at`        DATETIME NOT NULL,
    PRIMARY KEY (`character_id`),
    INDEX `idx_h5_state_account` (`account_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- H5 挂机收益：待发放收益表（每个角色一行，多次结算累加）
CREATE TABLE IF NOT EXISTS `h5_hangup_reward`
(
    `character_id` INT      NOT NULL,
    `account_id`   INT      NOT NULL,
    `world`        INT      NOT NULL DEFAULT 0,
    `meso`         BIGINT   NOT NULL DEFAULT 0 COMMENT '待发放金币',
    `exp`          BIGINT   NOT NULL DEFAULT 0 COMMENT '待发放经验',
    `update_at`    DATETIME NOT NULL,
    PRIMARY KEY (`character_id`),
    INDEX `idx_h5_reward_account` (`account_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 挂机收益参数（走 game_config，支持管理后台热重载）
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'H5 Hangup', 'java.lang.Integer', 'h5_hangup_daily_cap_hours', '8', 'h5_hangup_daily_cap_hours', '2026-11-01 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `game_config` WHERE `config_code` = 'h5_hangup_daily_cap_hours');

INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'H5 Hangup', 'java.lang.Integer', 'h5_hangup_max_gap_hours', '12', 'h5_hangup_max_gap_hours', '2026-11-01 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `game_config` WHERE `config_code` = 'h5_hangup_max_gap_hours');

INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'H5 Hangup', 'java.lang.Integer', 'h5_hangup_meso_per_hour_base', '100000', 'h5_hangup_meso_per_hour_base', '2026-11-01 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `game_config` WHERE `config_code` = 'h5_hangup_meso_per_hour_base');

INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'H5 Hangup', 'java.lang.Integer', 'h5_hangup_meso_per_hour_per_level', '15000', 'h5_hangup_meso_per_hour_per_level', '2026-11-01 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `game_config` WHERE `config_code` = 'h5_hangup_meso_per_hour_per_level');

INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'H5 Hangup', 'java.lang.Integer', 'h5_hangup_exp_per_hour_base', '5000', 'h5_hangup_exp_per_hour_base', '2026-11-01 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `game_config` WHERE `config_code` = 'h5_hangup_exp_per_hour_base');

INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'H5 Hangup', 'java.lang.Integer', 'h5_hangup_exp_per_hour_per_level', '2000', 'h5_hangup_exp_per_hour_per_level', '2026-11-01 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM `game_config` WHERE `config_code` = 'h5_hangup_exp_per_hour_per_level');

-- 中文说明
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'h5_hangup_daily_cap_hours', 'H5 挂机每日收益上限（小时）', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'h5_hangup_daily_cap_hours');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'h5_hangup_max_gap_hours', 'H5 挂机单次结算最大间隔（小时）', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'h5_hangup_max_gap_hours');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'h5_hangup_meso_per_hour_base', 'H5 挂机每小时基础金币', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'h5_hangup_meso_per_hour_base');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'h5_hangup_meso_per_hour_per_level', 'H5 挂机每小时金币（每级加成）', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'h5_hangup_meso_per_hour_per_level');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'h5_hangup_exp_per_hour_base', 'H5 挂机每小时基础经验', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'h5_hangup_exp_per_hour_base');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'h5_hangup_exp_per_hour_per_level', 'H5 挂机每小时经验（每级加成）', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'h5_hangup_exp_per_hour_per_level');

-- 英文说明
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'h5_hangup_daily_cap_hours', 'H5 hangup daily reward cap (hours)', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'h5_hangup_daily_cap_hours');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'h5_hangup_max_gap_hours', 'H5 hangup max gap per settlement (hours)', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'h5_hangup_max_gap_hours');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'h5_hangup_meso_per_hour_base', 'H5 hangup base mesos per hour', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'h5_hangup_meso_per_hour_base');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'h5_hangup_meso_per_hour_per_level', 'H5 hangup mesos per hour per level', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'h5_hangup_meso_per_hour_per_level');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'h5_hangup_exp_per_hour_base', 'H5 hangup base exp per hour', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'h5_hangup_exp_per_hour_base');

INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'h5_hangup_exp_per_hour_per_level', 'H5 hangup exp per hour per level', NULL
WHERE NOT EXISTS (SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'h5_hangup_exp_per_hour_per_level');
