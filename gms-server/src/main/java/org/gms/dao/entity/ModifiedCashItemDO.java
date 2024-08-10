package org.gms.dao.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 * 商城物品修改表 实体类。
 *
 * @author CN
 * @since 2024-08-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("modified_cash_item")
public class ModifiedCashItemDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * sn码
     */
    @Id
    private Integer sn;

    /**
     * 物品id
     */
    private Integer itemId;

    /**
     * 数量
     */
    private Integer count;

    /**
     * 价格
     */
    private Integer price;

    /**
     * 属性奖励
     */
    private Integer bonus;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 有效期
     */
    private Long period;

    /**
     * 抵用券
     */
    private Integer maplePoint;

    /**
     * 金币
     */
    private Integer meso;

    /**
     * 高级用户
     */
    private Integer forPremiumUser;

    /**
     * 性别
     */
    private Integer commodityGender;

    /**
     * 是否销售
     */
    private Integer onSale;
    @Column("class")
    private Integer clz;

    private Integer limit;

    private Integer pbCash;

    private Integer pbPoint;

    private Integer pbGift;

    /**
     * 礼包SN
     */
    private Integer packageSn;

}
