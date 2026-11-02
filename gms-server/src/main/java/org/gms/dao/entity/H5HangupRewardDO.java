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
 * H5 挂机待发放收益（每个角色一行，多次结算累加）。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("h5_hangup_reward")
public class H5HangupRewardDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Integer characterId;

    private Integer accountId;

    private Integer world;

    private Long meso;

    private Long exp;

    /** 待发放物品明细 JSON */
    private String itemsJson;

    private Timestamp updateAt;
}
