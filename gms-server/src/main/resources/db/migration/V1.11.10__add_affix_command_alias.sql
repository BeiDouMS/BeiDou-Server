INSERT INTO `command_info` (`syntax`, `level`, `enabled`, `clazz`, `default_level`)
SELECT 'affix', 0, 1, 'InspectCommand', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `command_info` WHERE `syntax` = 'affix'
);
