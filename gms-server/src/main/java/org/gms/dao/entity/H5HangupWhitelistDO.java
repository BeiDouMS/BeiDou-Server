package org.gms.dao.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * H5 挂机掉落白名单（每个角色可勾选任意数量物品）。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("h5_hangup_whitelist")
public class H5HangupWhitelistDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Integer characterId;

    @Id
    private Integer itemId;

    private Timestamp updateAt;
}
