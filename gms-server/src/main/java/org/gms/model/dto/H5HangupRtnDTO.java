package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * H5 挂机结算结果。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class H5HangupRtnDTO {
    private List<H5HangupCharRtnDTO> characters;
    private Long pendingMeso;
    private Long pendingExp;

    /**
     * 单个角色的挂机状态与本次结算收益。
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class H5HangupCharRtnDTO {
        private Integer characterId;
        private String characterName;
        private Integer level;
        private Integer world;
        private Boolean online;
        /** 本次结算覆盖的离线秒数 */
        private Long settledSeconds;
        /** 当日已用挂机秒数 */
        private Long dailyUsedSeconds;
        /** 当日剩余可挂机秒数 */
        private Long dailyRemainSeconds;
        /** 本次新增金币 */
        private Long gainedMeso;
        /** 本次新增经验 */
        private Long gainedExp;
        /** 待发放金币（累积） */
        private Long pendingMeso;
        /** 待发放经验（累积） */
        private Long pendingExp;
        /** 结算使用的挂机地图 */
        private Integer mapId;
        private String mapName;
        /** 本次新增物品 */
        private List<H5ItemRtnDTO> gainedItems;
        /** 待发放物品（累积） */
        private List<H5ItemRtnDTO> pendingItems;
        /** 本次新增击杀数（小数累积） */
        private Double gainedKills;
        /** 本次挂机累计击杀数（小数累积） */
        private Double sessionKills;
        /** 是否正在挂机 */
        private Boolean active;
        /** 本次挂机开始时间（毫秒） */
        private Long sessionStartTime;
    }
}
