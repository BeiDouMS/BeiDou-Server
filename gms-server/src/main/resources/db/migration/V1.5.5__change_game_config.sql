-- 这几个配置不能实时重载，已放回到yml中配置
delete from game_config where config_type = 'server' and config_sub_type = 'Net' and config_code in ('wan_host', 'lan_host', 'localhost', 'login_port');