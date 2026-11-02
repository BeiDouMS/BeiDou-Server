package org.gms.dao.entity;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * H5 挂机地图刷怪点。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("h5_hangup_map_mob")
public class H5HangupMapMobDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer mapId;

    private Integer mobId;

    private Integer spawnCount;

    /** 0 表示使用地图默认重生间隔 */
    private Integer mobTime;

    private Integer hide;
}
