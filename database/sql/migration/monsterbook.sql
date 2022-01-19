ALTER TABLE cosmic.`monsterbook`
CHANGE COLUMN `charid` `charid` INT(11) NOT NULL,
ADD PRIMARY KEY (`charid`, `cardid`),
ADD CONSTRAINT `FK_monsterbook_1` FOREIGN KEY (`charid`) REFERENCES `characters` (`id`) ON UPDATE CASCADE ON DELETE CASCADE;