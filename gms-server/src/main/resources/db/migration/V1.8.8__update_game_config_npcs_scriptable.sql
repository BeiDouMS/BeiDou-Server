UPDATE game_config
SET config_value = CASE
    WHEN config_value LIKE '%1300005%' THEN config_value
    WHEN TRIM(config_value) = '{}' THEN '{9001105:"Rescue Gaga!",1300005:"蘑菇王国"}'
    WHEN RIGHT(TRIM(config_value), 1) = '}' THEN CONCAT(LEFT(TRIM(config_value), LENGTH(TRIM(config_value)) - 1), ',1300005:"蘑菇王国"}')
    ELSE '{9001105:"Rescue Gaga!",1300005:"蘑菇王国"}'
END
WHERE config_code = 'npcs_scriptable';
