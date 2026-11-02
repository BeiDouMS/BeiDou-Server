package org.gms.dao.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 登出时的角色战斗力快照，供离线挂机结算使用。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("h5_hangup_stat")
public class H5HangupStatDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Integer characterId;

    private Integer level;

    private Integer job;

    private Integer str;

    private Integer dex;

    @com.mybatisflex.annotation.Column("int")
    private Integer intValue;

    private Integer luk;

    private Integer watk;

    private Integer matk;

    private Timestamp updateAt;
}
