drop table if exists world_prop;
create table if not exists world_prop
(
    `id`                   bigint primary key not null comment '大区id',
    `flag`                 tinyint default 0 comment '0=非特殊，1=活动大区，2=新区，3=热门大区',
    `server_message`       varchar(255) comment '顶部滚动信息',
    `event_message`        varchar(255) comment '大区描述',
    `recommend_message`    varchar(255) comment '大区推荐信息',
    `channel_size`         int comment '频道数',
    `exp_rate`             decimal(40, 3) comment '经验倍率',
    `meso_rate`            decimal(40, 3) comment '金币倍率',
    `drop_rate`            decimal(40, 3) comment '掉落倍率',
    `boss_drop_rate`       decimal(40, 3) comment 'BOSS掉落倍率',
    `quest_rate`           decimal(40, 3) comment '任务倍率',
    `fishing_rate`         decimal(40, 3) comment '钓鱼倍率',
    `travel_rate`          decimal(40, 3) comment '旅行倍率',
    `level_exp_rate`       decimal(40, 3) comment '等级经验倍率，0为不启用',
    `quick_level`          decimal(40, 3) comment '冲刺等级，0为不启用',
    `quick_level_exp_rate` decimal(40, 3) comment '冲刺等级经验倍率',
    `enabled`              tinyint default 0 comment '大区是否启用，0-不启用，1-启用'
) comment '大区配置';