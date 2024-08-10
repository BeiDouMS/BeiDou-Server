drop table if exists modified_cash_item;
create table modified_cash_item (
    `sn` int(11) not null comment 'sn码',
    `item_id` int(11) comment '物品id',
    `count` int(11) comment '数量',
    `price` int(11) comment '价格',
    `bonus` int(11) comment '属性奖励',
    `priority` int(11) comment '优先级',
    `period` bigint(20) comment '有效期',
    `maple_point` int(11) comment '抵用券',
    `meso` int(11) comment '金币',
    `for_premium_user` int(11) comment '高级用户',
    `commodity_gender` int(11) comment '性别',
    `on_sale` int(1) comment '是否销售',
    `class` int(11),
    `limit` int(11),
    `pb_cash` int(11),
    `pb_point` int(11),
    `pb_gift` int(11),
    `package_sn` int(11) comment '礼包SN',
    primary key (`sn`)  using btree
) engine = innodb comment '商城物品修改表';

