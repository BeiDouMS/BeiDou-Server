package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * H5 挂机地图列表（按地区分类）。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class H5MapRtnDTO {
    private List<H5MapRegionRtnDTO> regions;
    private H5CharacterRtnDTO character;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class H5MapRegionRtnDTO {
        private String region;
        private String regionName;
        private Integer order;
        private Integer mapCount;
        /** 该地区下的全部地图（扁平列表，兼容旧调用） */
        private List<H5MapRtnItemDTO> maps;
        /** 二级分区（按归属城镇划分） */
        private List<H5MapSubRegionRtnDTO> subRegions;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class H5MapSubRegionRtnDTO {
        private String subRegion;
        private String subRegionName;
        private Integer order;
        private Integer mapCount;
        private List<H5MapRtnItemDTO> maps;
    }
}
