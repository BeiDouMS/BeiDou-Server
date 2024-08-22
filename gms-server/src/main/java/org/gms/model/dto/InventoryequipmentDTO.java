package org.gms.model.dto;

import com.mybatisflex.annotation.Column;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gms.dao.entity.InventoryequipmentDO;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryequipmentDTO extends BasePageDTO   {

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


        public InventoryequipmentDTO(InventoryequipmentDO inventoryequipmentDO) {

            this.inventoryequipmentid = inventoryequipmentDO.getInventoryequipmentid();
            this.inventoryitemid = inventoryequipmentDO.getInventoryitemid();
            this.upgradeslots = inventoryequipmentDO.getUpgradeslots();
            this.level = inventoryequipmentDO.getLevel();
            this.str = inventoryequipmentDO.getStr();
            this.dex = inventoryequipmentDO.getDex();
            this.inte = inventoryequipmentDO.getInte();
            this.luk = inventoryequipmentDO.getLuk();
            this.hp = inventoryequipmentDO.getHp();
            this.mp = inventoryequipmentDO.getMp();
            this.watk = inventoryequipmentDO.getWatk();
            this.matk = inventoryequipmentDO.getMatk();
            this.wdef = inventoryequipmentDO.getWdef();
            this.mdef = inventoryequipmentDO.getMdef();
            this.acc = inventoryequipmentDO.getAcc();
            this.avoid = inventoryequipmentDO.getAvoid();
            this.hands = inventoryequipmentDO.getHands();
            this.speed = inventoryequipmentDO.getSpeed();
            this.jump = inventoryequipmentDO.getJump();
            this.locked = inventoryequipmentDO.getLocked();
            this.vicious = inventoryequipmentDO.getVicious();
            this.itemlevel = inventoryequipmentDO.getItemlevel();
            this.itemexp = inventoryequipmentDO.getItemexp();

    }
}