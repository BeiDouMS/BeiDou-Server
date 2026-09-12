CREATE TABLE IF NOT EXISTS `autoban_log` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `world`            INT          NOT NULL DEFAULT 0,
  `channel`          INT          NOT NULL DEFAULT 0,
  `account_id`       INT          NULL,
  `account_name`     VARCHAR(13)  NULL,
  `character_id`     INT          NULL,
  `character_name`   VARCHAR(13)  NULL,
  `map_id`           INT          NULL,
  `ip`               VARCHAR(45)  NULL,
  `type`             VARCHAR(32)  NOT NULL COMMENT 'AutobanFactory 枚举名，或 TIMESTAMP_SPAM',
  `action`           VARCHAR(16)  NOT NULL COMMENT 'ALERT / POINT / AUTOBAN / DISCONNECT',
  `points`           INT          NULL COMMENT 'POINT/AUTOBAN 时的当前累计分',
  `threshold`        INT          NULL COMMENT '该类型生效阈值',
  `reason`           VARCHAR(512) NULL,
  `auto_ban_enabled` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '触发时 use_auto_ban 的值：0=只留痕未处置，1=已实际封号/断线',
  `create_time`      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_autoban_log_time` (`create_time`),
  KEY `idx_autoban_log_chr` (`character_id`, `create_time`),
  KEY `idx_autoban_log_acc` (`account_id`),
  KEY `idx_autoban_log_type` (`type`),
  KEY `idx_autoban_log_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反作弊触发事件流水';

-- 反作弊事件日志保留天数，0 为不清理
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`, `update_time`)
SELECT 'server', 'Safe', 'java.lang.Integer', 'autoban_log_keep_days', '90', 'autoban_log_keep_days', '2026-09-12 00:00:00'
WHERE NOT EXISTS (
    SELECT 1 FROM `game_config` WHERE `config_code` = 'autoban_log_keep_days'
);

-- 中文
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'zh-CN', 'game_config', 'autoban_log_keep_days', '反作弊事件日志(autoban_log)保留天数，每日零点清理更早的记录，0为不清理', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'autoban_log_keep_days'
);

-- 英文
INSERT INTO `lang_resources`(`lang_type`, `lang_base`, `lang_code`, `lang_value`, `lang_extend`)
SELECT 'en-US', 'game_config', 'autoban_log_keep_days', 'Retention days of anti-cheat event log (autoban_log); older rows are purged daily at midnight, 0 = keep forever', NULL
WHERE NOT EXISTS (
    SELECT 1 FROM `lang_resources` WHERE `lang_type` = 'en-US' AND `lang_code` = 'autoban_log_keep_days'
);
