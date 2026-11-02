-- 校准客户端伤害倍率：权威公式 per-hit = ceil((magic*ceil(magic/1000)+magic)/30) + ceil(int/200)（int<200 时 ceil 加 1）
-- luckyS 实测：总魔法力 7 -> 权威 40/段，客户端实际 80/段，倍率修正为 2
UPDATE `game_config` SET `config_value` = '2.0' WHERE `config_code` = 'h5_hangup_damage_multiplier';
