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
@Table("inventoryequipment")
public class InventoryequipmentDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long inventoryequipmentid;

    private Long inventoryitemid;

    private Integer upgradeslots;

    private Integer level;

    private Integer str;

    private Integer dex;

    private Integer int;

    private Integer luk;

    private Integer hp;

    private Integer mp;

    private Integer watk;

    private Integer matk;

    private Integer wdef;

    private Integer mdef;

    private Integer acc;

    private Integer avoid;

    private Integer hands;

    private Integer speed;

    private Integer jump;

    private Integer locked;

    private Long vicious;

    private Integer itemlevel;

    private Long itemexp;

    private Integer ringid;

}
