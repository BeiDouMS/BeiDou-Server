package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * H5 挂机地图列表项（含按角色战斗力估算的收益）。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class H5MapRtnItemDTO {
    private Integer mapId;
    private String name;
    private String streetName;
    private String region;
    private String regionName;
    /** 二级分区标识（归属城镇地图 ID） */
    private String subRegion;
    /** 二级分区名称（城镇名） */
    private String subRegionName;
    private Integer subRegionOrder;
    private Integer minLevel;
    private Integer maxLevel;
    private Integer spawnCount;
    private Integer respawnSeconds;
    private Boolean hasBoss;
    /** 怪物等级与角色等级的差值 */
    private Integer levelGap;
    /** 等级差收益衰减系数（0.1~1.0） */
    private Double decay;
    private Double killsPerHour;
    private Long expPerHour;
    private Long mesoPerHour;
    private Boolean selected;
}
