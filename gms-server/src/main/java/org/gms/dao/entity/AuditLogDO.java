package org.gms.dao.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 *  实体类。
 *
 * @author sleep
 * @since 2026-09-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("audit_log")
public class AuditLogDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Integer operatorId;

    private String operatorName;

    private String ip;

    private String httpMethod;

    private String uri;

    private String operation;

    private String params;

    private Integer result;

    private String errorMsg;

    private Integer costMs;

    private Timestamp createdAt;

}
