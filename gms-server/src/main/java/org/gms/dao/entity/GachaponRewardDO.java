package org.gms.dao.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 *  实体类。
 *
 * @author lee
 * @since 2024-09-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("gachapon_reward")
public class GachaponRewardDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 绑定奖池ID
     */
    private Integer poolId;

    /**
     * 道具ID
     */
    private Integer itemId;

    /**
     * 道具名称
     */
    @Column(ignore = true)
    private String itemName;

    /**
     * 单次抽取数量
     */
    private Short quantity;

    /**
     * 创建日期
     */
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String comment;

}
