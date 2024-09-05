drop table if exists server_prop;
create table if not exists server_prop
(
    `id`         bigint primary key auto_increment comment '配置id',
    `prop_type`  int comment '配置分类',
    `prop_code`  varchar(32) comment '配置编码',
    `prop_class` varchar(32) comment '配置值数据类型',
    `prop_value` varchar(255) comment '配置值',
    `prop_desc`  varchar(500) comment '配置描述'
) comment '服务配置';