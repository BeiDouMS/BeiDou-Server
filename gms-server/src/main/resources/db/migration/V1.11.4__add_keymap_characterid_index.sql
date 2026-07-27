-- 修复角色保存时 keymap 表 DELETE/SELECT WHERE characterid = ?
-- 因 characterid 列无索引导致全表扫描加锁，多角色并发下线保存时
-- 偶发锁等待/回档。补上唯一复合索引（characterid, key）以避免全表扫描。
-- 已确认数据无 (characterid, key) 重复，可直接加 UNIQUE INDEX。
ALTER TABLE `keymap` ADD UNIQUE INDEX `idx_characterid_key` (`characterid`, `key`);
