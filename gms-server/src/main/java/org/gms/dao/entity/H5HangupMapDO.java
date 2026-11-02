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
 * H5 挂机野外地图目录。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("h5_hangup_map")
public class H5HangupMapDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Integer mapId;

    private String region;

    private String regionName;

    /** 二级分区：归属城镇的地图 ID */
    private String subRegion;

    /** 二级分区名称（城镇名） */
    private String subRegionName;

    private Integer subRegionOrder;

    private Integer regionOrder;

    private String name;

    private String streetName;

    private Integer minLevel;

    private Integer maxLevel;

    private Integer spawnCount;

    private Integer respawnSeconds;

    private Integer hasBoss;
}
