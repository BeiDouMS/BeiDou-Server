CREATE TABLE IF NOT EXISTS `audit_log`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT,
    `operator_id`   INT          NULL DEFAULT NULL COMMENT '操作人账号id，登录失败时为NULL',
    `operator_name` VARCHAR(64)  NULL DEFAULT NULL COMMENT '操作人用户名',
    `ip`            VARCHAR(64)  NULL DEFAULT NULL COMMENT '来源IP',
    `http_method`   VARCHAR(8)   NULL DEFAULT NULL COMMENT 'HTTP方法',
    `uri`           VARCHAR(255) NULL DEFAULT NULL COMMENT '请求路径',
    `operation`     VARCHAR(255) NULL DEFAULT NULL COMMENT '操作描述',
    `params`        TEXT         NULL COMMENT '请求参数（脱敏后JSON）',
    `result`        TINYINT      NOT NULL DEFAULT 1 COMMENT '1成功 0失败',
    `error_msg`     TEXT         NULL COMMENT '失败原因',
    `cost_ms`       INT          NULL DEFAULT NULL COMMENT '耗时毫秒',
    `created_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_operator` (`operator_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  AUTO_INCREMENT = 1;
