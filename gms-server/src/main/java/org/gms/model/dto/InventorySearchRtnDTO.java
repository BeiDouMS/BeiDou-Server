package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.Item;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventorySearchRtnDTO {
    /**
     * 自增id，对应inventoryitemid
     */
    private Long id;
    /**
     * 角色id
     */
    private Integer characterId;
    /**
     * 物品id，对应itemid
     */
    private Integer itemId;
    /**
     * 物品类型，对应type
     * @see org.gms.client.inventory.ItemFactory
     */
    private Integer itemType;
    /**
     * 背包栏类型，对应inventorytype
     * @see org.gms.client.inventory.InventoryType
     */
    private Byte inventoryType;
    /**
     * 物品位置，对应position
     */
    private Short position;
    /**
     * 物品数量，对应quantity
     */
    private Short quantity;
    /**
     * 制作者，对应owner
     */
    private String owner;
    /**
     * 宠物id，对应petid
     */
    private Integer petId;
    /**
     * 物品标记，对应flag
     */
    private Short flag;
    /**
     * 物品有效期，对应expiration
     */
    private Long expiration;
    /**
     * 送礼人，对应giftFrom
     */
    private String giftFrom;
    /**
     * 是否在线
     */
    private boolean online;
    /**
     * 是否装备，判断子表inventoryequipment是否有数据
     */
    private boolean equipment;
    /**
     * 装备信息，equipment为true时有值
     */
    private InventoryEquipRtnDTO inventoryEquipment;

    public Item toItem() {
        Item item;
        if (isEquipment()) {
            Equip equip = new Equip(getItemId(), getPosition());
            equip.setUpgradeSlots(getInventoryEquipment().getUpgradeSlots());
            equip.setLevel(getInventoryEquipment().getLevel());
            equip.setStr(getInventoryEquipment().getAttStr());
            equip.setDex(getInventoryEquipment().getAttDex());
            equip.setInt(getInventoryEquipment().getAttInt());
            equip.setLuk(getInventoryEquipment().getAttLuk());
            equip.setHp(getInventoryEquipment().getHp());
            equip.setMp(getInventoryEquipment().getMp());
            equip.setWatk(getInventoryEquipment().getPAtk());
            equip.setMatk(getInventoryEquipment().getMAtk());
            equip.setWdef(getInventoryEquipment().getPDef());
            equip.setMdef(getInventoryEquipment().getMDef());
            equip.setAcc(getInventoryEquipment().getAcc());
            equip.setAvoid(getInventoryEquipment().getAvoid());
            equip.setHands(getInventoryEquipment().getHands());
            equip.setSpeed(getInventoryEquipment().getSpeed());
            equip.setJump(getInventoryEquipment().getJump());
            equip.setVicious(getInventoryEquipment().getVicious());
            equip.setItemLevel(getInventoryEquipment().getItemLevel());
            equip.setItemExp(getInventoryEquipment().getItemExp());
            equip.setRingId(getInventoryEquipment().getRingId());
            item = equip;
        } else {
            item = new Item(getItemId(), getPosition(), getQuantity(), getPetId());
        }
        item.setOwner(getOwner());
        item.setExpiration(getExpiration());
        item.setGiftFrom(getGiftFrom());
        item.setFlag(getFlag());
        return item;
    }
}
