package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 反作弊日志按角色汇总出参。
 *
 * @author Nap
 * @since 2026-09-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutobanLogSummaryRtnDTO {
    private Integer characterId;
    private String characterName;
    private Integer accountId;
    private String accountName;
    private Long total;
    private Long alertCount;
    private Long pointCount;
    private Long autobanCount;
    private Long disconnectCount;
    /**
     * 去重后的类型，逗号拼接
     */
    private String types;
    private Timestamp lastTime;
    private String lastIp;
}
