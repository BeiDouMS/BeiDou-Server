package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * H5 挂机地图详情：怪物、掉落、收益估算。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class H5MapDetailRtnDTO {
    private H5MapRtnItemDTO map;
    private H5CharacterRtnDTO character;
    private List<MobInfo> mobs;
    private List<H5DropRtnDTO> drops;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MobInfo {
        private Integer mobId;
        private String name;
        private Integer level;
        private Integer hp;
        private Integer exp;
        private Integer spawnCount;
        private Boolean boss;
    }
}
