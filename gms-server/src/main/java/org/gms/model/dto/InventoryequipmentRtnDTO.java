package org.gms.model.dto;

import com.mybatisflex.annotation.Column;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryequipmentRtnDTO {

    private Long inventoryequipmentid;

    private Long inventoryitemid;

    private Integer upgradeslots;

    private Integer level;

    private Integer str;

    private Integer dex;

    @Column("int")
    private Integer inte;

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
}