ALTER TABLE `equipment_rarity_config`
    ADD COLUMN `boss_drop_weight` INT UNSIGNED NOT NULL DEFAULT 0,
    ADD COLUMN `dungeon_drop_weight` INT UNSIGNED NOT NULL DEFAULT 0;

UPDATE `equipment_rarity_config`
SET
    `boss_drop_weight` = CASE `rarity`
        WHEN 0 THEN 3000
        WHEN 1 THEN 3000
        WHEN 2 THEN 2000
        WHEN 3 THEN 1200
        WHEN 4 THEN 600
        WHEN 5 THEN 150
        WHEN 6 THEN 50
        ELSE 0
    END,
    `dungeon_drop_weight` = CASE `rarity`
        WHEN 0 THEN 1000
        WHEN 1 THEN 2500
        WHEN 2 THEN 3000
        WHEN 3 THEN 2000
        WHEN 4 THEN 1000
        WHEN 5 THEN 400
        WHEN 6 THEN 100
        ELSE 0
    END;
