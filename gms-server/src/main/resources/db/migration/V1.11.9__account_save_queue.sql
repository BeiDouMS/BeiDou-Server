-- 同账号串行，普通磁盘默认仅 2 个共享 + 2 个实时专用名额；按实测 I/O 调整，不沿用 SSD 的 8 并发默认值。
-- 开发交付仅提供迁移；由下次获准部署时的 Flyway 执行，不在运行中提前手工改库。
INSERT INTO game_config(config_type,config_sub_type,config_clazz,config_code,config_value,config_desc,update_time)
SELECT 'server','Performance','java.lang.Integer','character_save_realtime_concurrency','2','character_save_realtime_concurrency',NOW()
WHERE NOT EXISTS(SELECT 1 FROM game_config WHERE config_type='server' AND config_code='character_save_realtime_concurrency');
INSERT INTO lang_resources(lang_type,lang_base,lang_code,lang_value,lang_extend)
SELECT 'zh-CN','game_config','character_save_realtime_concurrency','角色实时存档专用并发数（0-8，默认2；总并发为实时专用与共享并发数之和）',NULL
WHERE NOT EXISTS(SELECT 1 FROM lang_resources WHERE lang_type='zh-CN' AND lang_base='game_config' AND lang_code='character_save_realtime_concurrency');
INSERT INTO lang_resources(lang_type,lang_base,lang_code,lang_value,lang_extend)
SELECT 'en-US','game_config','character_save_realtime_concurrency','Dedicated realtime save concurrency (0-8, default 2; total concurrency is realtime plus shared)',NULL
WHERE NOT EXISTS(SELECT 1 FROM lang_resources WHERE lang_type='en-US' AND lang_base='game_config' AND lang_code='character_save_realtime_concurrency');
INSERT INTO game_config(config_type,config_sub_type,config_clazz,config_code,config_value,config_desc,update_time)
SELECT 'server','Performance','java.lang.Integer','character_save_shared_concurrency','2','character_save_shared_concurrency',NOW()
WHERE NOT EXISTS(SELECT 1 FROM game_config WHERE config_type='server' AND config_code='character_save_shared_concurrency');
INSERT INTO lang_resources(lang_type,lang_base,lang_code,lang_value,lang_extend)
SELECT 'zh-CN','game_config','character_save_shared_concurrency','角色共享存档并发数（1-8，默认2；自动与实时存档共用，总并发为实时专用与共享并发数之和）',NULL
WHERE NOT EXISTS(SELECT 1 FROM lang_resources WHERE lang_type='zh-CN' AND lang_base='game_config' AND lang_code='character_save_shared_concurrency');
INSERT INTO lang_resources(lang_type,lang_base,lang_code,lang_value,lang_extend)
SELECT 'en-US','game_config','character_save_shared_concurrency','Shared save concurrency (1-8, default 2; used by autosaves and realtime saves; total is realtime plus shared)',NULL
WHERE NOT EXISTS(SELECT 1 FROM lang_resources WHERE lang_type='en-US' AND lang_base='game_config' AND lang_code='character_save_shared_concurrency');

-- area_info 随角色大存档提交/回滚；charid 索引避免按角色清理时扫描并锁住整表。
-- DDL 隐式提交，迁移日志写入失败后重跑也必须安全。
SET @area_info_is_innodb = (
    SELECT UPPER(ENGINE) = 'INNODB'
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'area_info'
);
SET @area_info_has_charid_index = EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'area_info'
      AND COLUMN_NAME = 'charid' AND SEQ_IN_INDEX = 1
);
SET @area_info_transaction_sql = CASE
    WHEN @area_info_is_innodb AND @area_info_has_charid_index THEN 'DO 1'
    WHEN @area_info_is_innodb THEN 'ALTER TABLE `area_info` ADD INDEX `idx_area_info_charid` (`charid`)'
    WHEN @area_info_has_charid_index THEN 'ALTER TABLE `area_info` ENGINE=InnoDB'
    ELSE 'ALTER TABLE `area_info` ENGINE=InnoDB, ADD INDEX `idx_area_info_charid` (`charid`)'
END;
PREPARE area_info_transaction_statement FROM @area_info_transaction_sql;
EXECUTE area_info_transaction_statement;
DEALLOCATE PREPARE area_info_transaction_statement;

-- 并发存档时，按角色删除不能全表扫描，否则会等待其他角色尚未提交的行。
-- 已有以 characterid 开头的可见 BTREE 索引时跳过，允许重复执行。
SET @save_skillmacros_index_sql := IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'skillmacros'
          AND COLUMN_NAME = 'characterid' AND SEQ_IN_INDEX = 1
          AND INDEX_TYPE = 'BTREE' AND IS_VISIBLE = 'YES'),
    'DO 0',
    'ALTER TABLE `skillmacros` ADD INDEX `idx_characterid` (`characterid`)'
);
PREPARE save_skillmacros_index_stmt FROM @save_skillmacros_index_sql;
EXECUTE save_skillmacros_index_stmt;
DEALLOCATE PREPARE save_skillmacros_index_stmt;

SET @save_savedlocations_index_sql := IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'savedlocations'
          AND COLUMN_NAME = 'characterid' AND SEQ_IN_INDEX = 1
          AND INDEX_TYPE = 'BTREE' AND IS_VISIBLE = 'YES'),
    'DO 0',
    'ALTER TABLE `savedlocations` ADD INDEX `idx_characterid` (`characterid`)'
);
PREPARE save_savedlocations_index_stmt FROM @save_savedlocations_index_sql;
EXECUTE save_savedlocations_index_stmt;
DEALLOCATE PREPARE save_savedlocations_index_stmt;

SET @save_trocklocations_index_sql := IF(
    EXISTS(SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'trocklocations'
          AND COLUMN_NAME = 'characterid' AND SEQ_IN_INDEX = 1
          AND INDEX_TYPE = 'BTREE' AND IS_VISIBLE = 'YES'),
    'DO 0',
    'ALTER TABLE `trocklocations` ADD INDEX `idx_characterid` (`characterid`)'
);
PREPARE save_trocklocations_index_stmt FROM @save_trocklocations_index_sql;
EXECUTE save_trocklocations_index_stmt;
DEALLOCATE PREPARE save_trocklocations_index_stmt;
