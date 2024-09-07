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
    private Byte upgradeSlots;
    /**
     * 装备等级，对应level
     */
    private Byte level;
    /**
     * 力量，对应str
     */
    private Short attStr;
    /**
     * 敏捷，对应dex
     */
    private Short attDex;
    /**
     * 智力，对应int
     */
    private Short attInt;
    /**
     * 运气，对应luk
     */
    private Short attLuk;
    /**
     * 血量，对应hp
     */
    private Short hp;
    /**
     * 蓝量，对应mp
     */
    private Short mp;
    /**
     * 物理攻击，对应watk
     */
    private Short pAtk;
    /**
     * 魔法攻击，对应matk
     */
    private Short mAtk;
    /**
     * 物理防御，对应wdef
     */
    private Short pDef;
    /**
     * 魔法防御，对应mDef
     */
    private Short mDef;
    /**
     * 命中，对应acc
     */
    private Short acc;
    /**
     * 回避，对应avoid
     */
    private Short avoid;
    /**
     * 攻速，对应hands
     */
    private Short hands;
    /**
     * 移速，对应speed
     */
    private Short speed;
    /**
     * 跳跃力，对应jump
     */
    private Short jump;
    /**
     * 锁定，对应locked
     */
    private Integer locked;
    /**
     * 锤子次数，对应vicious
     */
    private Short vicious;
    /**
     * 装备升级等级，对应itemlevel
     */
    private Byte itemLevel;
    /**
     * 装备升级经验，对应itemexp
     */
    private Integer itemExp;
    /**
     * 戒指id，对应ringid
     */
    private Integer ringId;
}
