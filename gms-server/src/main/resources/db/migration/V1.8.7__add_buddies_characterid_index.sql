-- 修复角色保存时 buddies 表 DELETE WHERE characterid = ?
-- 因 characterid 列无索引导致全表扫描加锁，多角色并发下线（尤其互为好友）时
-- 偶发 1213 Deadlock。补上索引以避免死锁。
ALTER TABLE `buddies` ADD INDEX `idx_characterid` (`characterid`);
