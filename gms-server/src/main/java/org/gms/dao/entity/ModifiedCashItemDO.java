package org.gms.dao.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.Pet;
import org.gms.constants.id.ItemId;
import org.gms.constants.inventory.ItemConstants;
import org.gms.net.server.Server;
import org.gms.server.ItemInformationProvider;

import java.io.Serial;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;

/**
 * 商城物品修改表 实体类。
 *
 * @author CN
 * @since 2024-08-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("modified_cash_item")
public class ModifiedCashItemDO implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * sn码
     */
    @Id
    private Integer sn;

    /**
     * 物品id
     */
    private Integer itemId;

    /**
     * 数量
     */
    private Short count;

    /**
     * 价格
     */
    private Integer price;

    /**
     * 属性奖励
     */
    private Integer bonus;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 有效期
     */
    private Long period;

    /**
     * 抵用券
     */
    private Integer maplePoint;

    /**
     * 金币
     */
    private Integer meso;

    /**
     * 高级用户
     */
    private Integer forPremiumUser;

    /**
     * 性别
     */
    private Integer commodityGender;

    /**
     * 是否销售
     */
    private Integer onSale;
    @Column("class")
    private Integer clz;

    private Integer limit;

    private Integer pbCash;

    private Integer pbPoint;

    private Integer pbGift;

    /**
     * 礼包SN
     */
    private Integer packageSn;


    public boolean isSelling() {
        return onSale != null && onSale == 1;
    }

    public Item toItem() {
        Item item;

        int petid = -1;
        if (ItemConstants.isPet(itemId)) {
            petid = Pet.createPet(itemId);
        }

        if (ItemConstants.getInventoryType(itemId).equals(InventoryType.EQUIP)) {
            item = ItemInformationProvider.getInstance().getEquipById(itemId);
        } else {
            item = new Item(itemId, (byte) 0, count, petid);
        }

        if (period == 1) {
            switch (itemId) {
                case ItemId.DROP_COUPON_2X_4H,
                     ItemId.EXP_COUPON_2X_4H: // 4 Hour 2X coupons, the period is 1, but we don't want them to last a day.
                    item.setExpiration(Server.getInstance().getCurrentTime() + HOURS.toMillis(4));
                            /*
                            } else if(itemId == 5211047 || itemId == 5360014) { // 3 Hour 2X coupons, unused as of now
                                    item.setExpiration(Server.getInstance().getCurrentTime() + HOURS.toMillis(3));
                            */
                    break;
                case ItemId.EXP_COUPON_3X_2H:
                    item.setExpiration(Server.getInstance().getCurrentTime() + HOURS.toMillis(2));
                    break;
                default:
                    item.setExpiration(Server.getInstance().getCurrentTime() + DAYS.toMillis(1));
                    break;
            }
        } else if (period == -1) {
            item.setExpiration(-1);
        } else {
            item.setExpiration(Server.getInstance().getCurrentTime() + DAYS.toMillis(period));
        }

        item.setSN(sn);
        return item;
    }

    @Override
    public ModifiedCashItemDO clone() {
        try {
            return (ModifiedCashItemDO) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
