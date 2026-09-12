package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 反作弊日志按角色汇总入参。
 *
 * @author Nap
 * @since 2026-09-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutobanLogSummaryReqDTO {
    private Date startTime;
    private Date endTime;
    /**
     * 返回条数，默认 20
     */
    private Integer limit;
    /**
     * 可空；ALERT / POINT / AUTOBAN / DISCONNECT
     */
    private String action;
}
