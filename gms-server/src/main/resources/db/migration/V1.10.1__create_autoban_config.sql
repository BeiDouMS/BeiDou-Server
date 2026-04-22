CREATE TABLE IF NOT EXISTS `autoban_config`
(
    `id`          INT(11)      NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `type`        VARCHAR(32)  NOT NULL COMMENT '封禁类型，对应AutobanFactory枚举名',
    `disabled`    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否禁用，1=禁用该类型封禁检测',
    `points`      INT(11)      NULL     COMMENT '触发封禁所需积分，NULL使用枚举默认值',
    `expire_time` BIGINT       NULL     COMMENT '积分过期时间(毫秒)，NULL使用枚举默认值',
    `description` VARCHAR(128) NULL     COMMENT '描述说明',
    `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP    NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type` (`type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  AUTO_INCREMENT = 1
  COMMENT '自动封禁配置表';
