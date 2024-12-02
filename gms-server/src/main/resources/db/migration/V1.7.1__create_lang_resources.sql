drop table if exists lang_resources;
create table if not exists lang_resources
(
    id          bigint auto_increment primary key comment '自增id',
    lang_type   varchar(32) comment '语言类型，zh-CN，en-US',
    lang_base   varchar(32) comment '预留，当存在2个一样的code，不一样的value，需要用base来区分',
    lang_code   varchar(128) not null comment 'i18n编码',
    lang_value  varchar(512) not null comment 'i18n值',
    lang_extend varchar(512) comment '预留扩展字段',
    index idx_lang_code (lang_code)
    ) comment '数据库i18n表';

insert into lang_resources(lang_type, lang_base, lang_code, lang_value, lang_extend)
select 'zh-CN', 'game_config', config_code, substring_index(substring_index(config_desc, '(', 1), ')', -1), null
from game_config;

insert into lang_resources(lang_type, lang_base, lang_code, lang_value, lang_extend)
select 'en-US', 'game_config', config_code, substring_index(substring_index(config_desc, '(', -1), ')', 1), null
from game_config;

update game_config set config_desc = config_code;
