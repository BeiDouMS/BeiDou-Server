-- H5 挂机改为显式开始/结算会话：active 表示是否挂机中，session_start_time 记录本次开始时间（毫秒）
ALTER TABLE h5_hangup_state
    ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN session_start_time BIGINT NULL;
