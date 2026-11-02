-- 击杀数改为小数累积：挂机速率通常 <1 只/秒，整数列轮询窗口内 round 会丢失累积
ALTER TABLE h5_hangup_state
    MODIFY COLUMN session_kills DOUBLE NOT NULL DEFAULT 0 COMMENT '本次挂机已击杀怪物数（小数累积）';
