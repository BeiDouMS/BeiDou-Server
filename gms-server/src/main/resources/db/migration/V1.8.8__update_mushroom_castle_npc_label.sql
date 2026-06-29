UPDATE game_config
SET config_value = REPLACE(config_value, '1300005:"The Test"', '1300005:"蘑菇王国"')
WHERE config_code = 'npcs_scriptable'
  AND config_value LIKE '%1300005:"The Test"%';
