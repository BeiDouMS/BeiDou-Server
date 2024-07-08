drop table if exists extend_value;
create table if not exists extend_value(
    extend_id varchar(50) not null comment '扩展字段id',
    extend_type int not null comment '扩展字段类型，11-账号，12-账号日清，13-账号周清；21-角色，22-角色日清，23-角色周清',
    extend_name varchar(50) not null comment '扩展字段名称',
    extend_value varchar(255) comment '扩展字段值',
    create_time datetime not null default current_timestamp comment '创建时间',
    update_time datetime comment '更新时间',
    primary key (extend_id, extend_type, extend_name)
) engine = innodb default charset = utf8mb4 comment '扩展字段表';