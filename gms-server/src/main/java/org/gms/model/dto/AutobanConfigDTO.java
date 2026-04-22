package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自动封禁配置 DTO。
 *
 * @author Nap
 * @since 2026-04-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutobanConfigDTO {

    /**
     * 自增id
     */
    private Integer id;

    /**
     * 封禁类型，对应AutobanFactory枚举名
     */
    private String type;

    /**
     * 封禁类型名称（i18n）
     */
    private String name;

    /**
     * 是否禁用，true=禁用该类型封禁检测
     */
    private Boolean disabled;

    /**
     * 触发封禁所需积分，NULL使用枚举默认值
     */
    private Integer points;

    /**
     * 检测周期(秒)，后端存储时会转换为毫秒
     */
    private Long expireTimeSeconds;

    /**
     * 描述说明
     */
    private String description;

    /**
     * 枚举默认积分值（仅用于前端展示）
     */
    private Integer defaultPoints;

    /**
     * 枚举默认检测周期(秒)（仅用于前端展示）
     */
    private Long defaultExpireTimeSeconds;

    /**
     * 控制字段：true=更新points，false=置为null
     */
    private boolean changePoints;

    /**
     * 控制字段：true=更新expireTime，false=置为null
     */
    private boolean changeExpireTime;
}
