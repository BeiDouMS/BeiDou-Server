-- SoloMapling: bot-only account + template character for bot cloning.
-- Login: fmbot / password (bcrypt hash from SoloMapling 162-fmbot-data.sql)
-- Do NOT force character id=2 (BeiDou DBs may already use that for a player).
-- BotGeneration resolves the template by characters.name = 'fmbot'.

INSERT IGNORE INTO accounts (`name`, password, pin, pic, birthday, nxCredit, maplePoint, nxPrepaid, characterslots,
                             gender, tos)
VALUES ('fmbot', '$2y$12$xS3xZTX5hSU8v0SvC4h1FewFeK4Lx0q6kXoqv/bFJu6Hr3Wuimr9q', '0000', '000000',
        '2005-05-11', 0, 0, 0, 3, 0, 1);

INSERT INTO characters (accountid, world, `name`, level, exp,
                               str, dex, luk, `int`, hp, mp, maxhp, maxmp, meso, job, skincolor, gender,
                               hair, face, ap, map, spawnpoint, gm, equipslots, useslots,
                               setupslots, etcslots)
SELECT a.id, 0, 'fmbot', 1, 0,
        12, 5, 4, 4, 50, 5, 50, 5, 0, 0, 0, 0,
        30030, 20000, 0, 10000, 0, 0, 96, 96,
        96, 96
FROM accounts a
WHERE a.name = 'fmbot'
  AND NOT EXISTS (SELECT 1 FROM characters c WHERE c.name = 'fmbot');
