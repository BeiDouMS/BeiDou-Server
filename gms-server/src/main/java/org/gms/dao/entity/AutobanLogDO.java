package org.gms.dao.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 反作弊触发事件流水 实体类。
 *
 * @author Nap
 * @since 2026-09-12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("autoban_log")
public class AutobanLogDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Integer world;

    private Integer channel;

    private Integer accountId;

    private String accountName;

    private Integer characterId;

    private String characterName;

    private Integer mapId;

    private String ip;

    /**
     * AutobanFactory 枚举名，或 TIMESTAMP_SPAM
     */
    private String type;

    /**
     * ALERT / POINT / AUTOBAN / DISCONNECT
     */
    private String action;

    /**
     * POINT/AUTOBAN 时的当前累计分
     */
    private Integer points;

    /**
     * 该类型生效阈值
     */
    private Integer threshold;

    private String reason;

    /**
     * 触发时 use_auto_ban 的值：false=只留痕未处置，true=已实际封号/断线
     */
    private Boolean autoBanEnabled;

    private Timestamp createTime;

}
