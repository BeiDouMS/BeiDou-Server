package org.gms.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 反作弊日志查询入参。
 *
 * @author Nap
 * @since 2026-09-12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AutobanLogSearchReqDTO extends BasePageDTO {
    /**
     * 角色名（模糊）
     */
    private String characterName;
    private Integer accountId;
    private Integer characterId;
    /**
     * AutobanFactory 枚举名，或 TIMESTAMP_SPAM
     */
    private String type;
    /**
     * ALERT / POINT / AUTOBAN / DISCONNECT
     */
    private String action;
    /**
     * 最小累计分
     */
    private Integer minPoints;
    private Date startTime;
    private Date endTime;
}
