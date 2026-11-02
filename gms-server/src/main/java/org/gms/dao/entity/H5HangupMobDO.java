package org.gms.dao.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * H5 挂机怪物属性（来自客户端 Mob.wz）。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("h5_hangup_mob")
public class H5HangupMobDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Integer mobId;

    private String name;

    private Integer level;

    private Integer hp;

    private Integer exp;

    private Integer boss;
}
