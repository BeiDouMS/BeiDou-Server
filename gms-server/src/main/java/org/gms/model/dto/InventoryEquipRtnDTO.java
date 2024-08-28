package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryEquipRtnDTO {
    /**
     * 自增id，对应inventoryequipmentid
     */
    private Long id;
    /**
     * 外键，关联inventoryitems表id
     */
    private Long inventoryItemId;
    /**
     * 砸券次数，对应upgradeslots
     */
    private Integer upgradeSlots;
    /**
     * 装备等级，对应level
     */
    private Integer level;
    /**
     * 力量，对应str
     */
    private Integer attStr;
    /**
     * 敏捷，对应dex
     */
    private Integer attDex;
    /**
     * 智力，对应int
     */
    private Integer attInt;
    /**
     * 运气，对应luk
     */
    private Integer attLuk;
    /**
     * 血量，对应hp
     */
    private Integer hp;
    /**
     * 蓝量，对应mp
     */
    private Integer mp;
    /**
     * 物理攻击，对应watk
     */
    private Integer pAtk;
    /**
     * 魔法攻击，对应matk
     */
    private Integer mAtk;
    /**
     * 物理防御，对应wdef
     */
    private Integer pDef;
    /**
     * 魔法防御，对应mDef
     */
    private Integer mdef;
    /**
     * 命中，对应acc
     */
    private Integer acc;
    /**
     * 回避，对应avoid
     */
    private Integer avoid;
    /**
     * 攻速，对应hands
     */
    private Integer hands;
    /**
     * 移速，对应speed
     */
    private Integer speed;
    /**
     * 跳跃力，对应jump
     */
    private Integer jump;
    /**
     * 锁定，对应locked
     */
    private Integer locked;
    /**
     * 锤子次数，对应vicious
     */
    private Long vicious;
    /**
     * 装备升级等级，对应itemlevel
     */
    private Integer itemLevel;
    /**
     * 装备升级经验，对应itemexp
     */
    private Long itemExp;
    /**
     * 戒指id，对应ringid
     */
    private Integer ringId;
}
