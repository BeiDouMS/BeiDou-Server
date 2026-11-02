-- 本次挂机击杀数（展示用，开始挂机时清零）
ALTER TABLE h5_hangup_state
    ADD COLUMN session_kills BIGINT NOT NULL DEFAULT 0 COMMENT '本次挂机已击杀怪物数';
