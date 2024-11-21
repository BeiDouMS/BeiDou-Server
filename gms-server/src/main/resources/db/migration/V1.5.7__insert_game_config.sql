insert into game_config (config_type, config_sub_type, config_clazz, config_code, config_value, config_desc)
select 'server', 'Game Mechanics', 'java.lang.Boolean', 'show_coupon_buff', 'true',
       '原版游戏不展示双倍卡buff，HeavenMS支持了展示双倍卡的buff，但是会占用怪物卡buff，导致怪物卡buff和双倍卡buff同时展示会有挤兑问题。如果你在意该问题，可以关闭此参数不让双倍卡buff展示(The old gms does not display coupon buff, and HeavenMS supported it. But it will occupy mob card buff. If you want, you can turn off this config to make mob card buff display perfectly.)'
where not exists(
    select 1 from game_config where config_type = 'server' and config_code = 'show_coupon_buff'
);