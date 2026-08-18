ALTER TABLE `inventory_equipment_affix`
    ADD COLUMN `locked` TINYINT UNSIGNED NOT NULL DEFAULT 0 AFTER `roll_seed`;
