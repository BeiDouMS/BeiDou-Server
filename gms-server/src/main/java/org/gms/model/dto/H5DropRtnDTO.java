package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * H5 挂机地图可掉落物品。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class H5DropRtnDTO {
    private Integer itemId;
    private String itemName;
    /** 每次击杀的综合掉落概率（0~100） */
    private Double chancePercent;
    private Integer minQuantity;
    private Integer maxQuantity;
    /** 按当前战力估算的每小时期望数量 */
    private Double expectedPerHour;
    private Boolean whitelisted;
}
