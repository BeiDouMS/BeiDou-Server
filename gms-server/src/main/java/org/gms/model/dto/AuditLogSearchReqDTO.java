package org.gms.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AuditLogSearchReqDTO extends BasePageDTO {
    /**
     * 操作人用户名
     */
    private String operatorName;

    /**
     * 请求路径
     */
    private String uri;

    /**
     * 操作项
     */
    private String operation;

    /**
     * 操作结果：1成功 0失败
     */
    private Integer result;

    /**
     * 起始时间
     */
    private Timestamp startTime;

    /**
     * 结束时间
     */
    private Timestamp endTime;
}
