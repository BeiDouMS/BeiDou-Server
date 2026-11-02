-- 封闭测试阶段：取消 H5 挂机每日时长上限，配置值 0 表示不限（代码按 <=0 视为不限）
UPDATE `game_config`
SET `config_value` = '0', `update_time` = NOW()
WHERE `config_code` = 'h5_hangup_daily_cap_hours';

UPDATE `lang_resources`
SET `lang_value` = 'H5 挂机每日收益上限（小时，0 = 不限）'
WHERE `lang_type` = 'zh-CN' AND `lang_code` = 'h5_hangup_daily_cap_hours';
