package org.gms.dao.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serial;

/**
 *  实体类。
 *
 * @author sleep
 * @since 2024-05-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("inventoryitems")
public class InventoryitemsDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long inventoryitemid;

    private Integer type;

    private Integer characterid;

    private Integer accountid;

    private Integer itemid;

    private Integer inventorytype;

    private Integer position;

    private Integer quantity;

    private String owner;

    private Integer petid;

    private Integer flag;

    private Long expiration;

    @Column("giftFrom")
    private String giftFrom;

}
