package org.gms.dao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@Table("gachapon_reward_pool")
public class GachaponRewardPoolDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
    @Id(keyType = KeyType.Auto)
    private Integer id;

    /**
     * 转蛋机奖池名称
     */
    private String name;

    /**
     * 绑定转蛋机ID
     */
    private Integer gachaponId;

    /**
     * 转蛋机名称
     */
    @Column(ignore = true)
    private String gachaponName;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 是否公共奖池 0为否 1为是
     */
    private Boolean isPublic;

    /**
     * 概率
     */
    private Integer prob;

    /**
     * 奖池的启用日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 奖池的结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 是否喇叭通知 0为否 1为是
     */
    private Boolean notification;

    /**
     * 备注
     */
    private String comment;

}
