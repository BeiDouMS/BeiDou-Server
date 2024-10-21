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
 * 大区配置 实体类。
 *
 * @author CN
 * @since 2024-10-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("world_prop")
public class WorldPropDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 大区id
     */
    @Id
    private Integer id;

    /**
     * 0=非特殊，1=活动大区，2=新区，3=热门大区
     */
    private Integer flag;

    /**
     * 顶部滚动信息
     */
    private String serverMessage;

    /**
     * 大区描述
     */
    private String eventMessage;

    /**
     * 大区推荐信息
     */
    @Column("recommend_message")
    private String whyAmIRecommended;

    /**
     * 频道数
     */
    @Column("channel_size")
    private Integer channels;

    /**
     * 经验倍率
     */
    private Float expRate;

    /**
     * 金币倍率
     */
    private Float mesoRate;

    /**
     * 掉落倍率
     */
    private Float dropRate;

    /**
     * BOSS掉落倍率
     */
    private Float bossDropRate;

    /**
     * 任务倍率
     */
    private Float questRate;

    /**
     * 钓鱼倍率
     */
    private Float fishingRate;

    /**
     * 旅行倍率
     */
    private Float travelRate;

    /**
     * 等级经验倍率，0为不启用
     */
    private Float levelExpRate;

    /**
     * 冲刺等级，0为不启用
     */
    private Integer quickLevel;

    /**
     * 冲刺等级经验倍率
     */
    private Float quickLevelExpRate;

    /**
     * 大区是否启用，0-不启用，1-启用
     */
    private Integer enabled;

}
