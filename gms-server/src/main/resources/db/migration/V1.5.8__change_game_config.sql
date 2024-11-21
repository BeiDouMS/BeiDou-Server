-- 新增使用装备进行性别校验参数
INSERT INTO `game_config`(`config_type`, `config_sub_type`, `config_clazz`, `config_code`, `config_value`, `config_desc`) VALUES ('server', 'Game Mechanics', 'java.lang.Boolean', 'use_equipment_gender_limit', 'true', '是否使用装备时进行性别校验，需要客户端支持混用。(Whether to perform gender verification when using equipment requires client support for mixed use.)');
