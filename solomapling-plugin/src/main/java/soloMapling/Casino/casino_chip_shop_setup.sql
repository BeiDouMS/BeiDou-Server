-- Casino Chip Shop Setup
-- NOTE: This now runs automatically at server startup via Liquibase
-- (src/main/resources/db/data/171-casino-shop-data.sql). This copy is kept
-- only for reference / manual runs — keep both files in sync if prices change.
-- Shop ID 9999001 is used by NPC 9000055 (see CasinoChipConfig.java).
--
-- These values must match CasinoChipConfig.java:
--   4002000 Snail Stamp      = 10,000 mesos
--   4002001 Blue Snail Stamp  = 50,000 mesos
--   4002002 Stump Stamp       = 250,000 mesos
--   4002003 Slime Stamp       = 1,000,000 mesos

-- Create the shop and link it to the NPC
INSERT INTO shops (shopid, npcid) VALUES (9999001, 9000055)
ON DUPLICATE KEY UPDATE npcid = 9000055;

-- Add chip items to the shop (position = display order, lower = bottom)
INSERT INTO shopitems (shopid, itemid, price, pitch, position) VALUES
(9999001, 4002000, 10000,    0, 4),
(9999001, 4002001, 50000,    0, 3),
(9999001, 4002002, 250000,   0, 2),
(9999001, 4002003, 1000000,  0, 1)
ON DUPLICATE KEY UPDATE price = VALUES(price), position = VALUES(position);

-- OPTIONAL: Permanent NPC spawn via plife table.
-- Uncomment and fill in YOUR_MAP_ID, X, Y, FOOTHOLD_ID for a permanent spawn.
-- Otherwise use the in-game command: !env spawncasinonpc
--
-- INSERT INTO plife (world, map, life, type, cy, f, fh, rx0, rx1, x, y, hide, mobtime, team)
-- VALUES (0, YOUR_MAP_ID, 9201066, 'n', Y_COORD, 0, FOOTHOLD_ID, X-50, X+50, X_COORD, Y_COORD, 0, -1, -1);
