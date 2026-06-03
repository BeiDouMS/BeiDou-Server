-- 光之祭典（Festival of Lights）任务掉落修复
-- 相关任务 / 道具 / 怪物：
--   8295 / 8829 : 建造祭坛
--   8830        : 点亮祭坛
--   4031445     : 祭坛零件
--   4031444     : 安息日蜡烛
--   1130100     : 斧木妖
--   2130100     : 黑斧木妖
--   2110200     : 刺蘑菇
--
-- 说明：
-- 1. 当前 WZ 数据里“建造祭坛”同时存在 8295 与 8829 两套任务 ID。
-- 2. `drop_data` 表对 `(dropperid, itemid)` 有唯一索引，无法为同一怪物 + 同一道具同时绑定两个 questid。
-- 3. 因此“祭坛零件”这里使用 `questid = 0`，让 8295 / 8829 两套任务都能正常拾取。
-- 4. “安息日蜡烛”只对应第二环 8830，因此仍然绑定到该任务。
-- 5. 掉率先给 300000（约 30% 基础概率，实际还会乘频道掉率），后续可按服内体验再调整。

INSERT INTO `drop_data` (`dropperid`, `itemid`, `minimum_quantity`, `maximum_quantity`, `questid`, `chance`)
VALUES
    (1130100, 4031445, 1, 1, 0, 300000),
    (2130100, 4031445, 1, 1, 0, 300000),
    (2110200, 4031444, 1, 1, 8830, 300000)
ON DUPLICATE KEY UPDATE
    `minimum_quantity` = VALUES(`minimum_quantity`),
    `maximum_quantity` = VALUES(`maximum_quantity`),
    `questid` = VALUES(`questid`),
    `chance` = VALUES(`chance`);
