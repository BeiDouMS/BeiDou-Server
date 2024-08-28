package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * saverId是否为角色id，true角色id，false账号id
     */
    private boolean character;
    /**
     * 角色id or 账号id
     */
    private Integer saverId;
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
    private Integer inventoryType;
    /**
     * 物品位置，对应position
     */
    private Integer position;
    /**
     * 物品数量，对应quantity
     */
    private Integer quantity;
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
    private Integer flag;
    /**
     * 物品有效期，对应expiration
     */
    private Long expiration;
    /**
     * 送礼人，对应giftFrom
     */
    private String giftFrom;
    /**
     * 是否装备，判断子表inventoryequipment是否有数据
     */
    private boolean equipment;
    /**
     * 装备信息，equipment为true时有值
     */
    private InventoryEquipRtnDTO inventoryEquipment;
}
