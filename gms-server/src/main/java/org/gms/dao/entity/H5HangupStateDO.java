package org.gms.dao.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * H5 挂机结算状态（每个角色一行）。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("h5_hangup_state")
public class H5HangupStateDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Integer characterId;

    private Integer accountId;

    private Integer world;

    /** 当前挂机地图 */
    private Integer mapId;

    private Timestamp lastSettleTime;

    private Date dailyDate;

    private Integer dailySeconds;

    private Long totalSeconds;

    /** 是否正在挂机（1 挂机中，0 已停止） */
    private Boolean active;

    /** 本次挂机开始时间（毫秒） */
    private Long sessionStartTime;

    /** 本次挂机已击杀怪物数（小数累积，开始挂机时清零） */
    private Double sessionKills;

    private Timestamp updateAt;
}
