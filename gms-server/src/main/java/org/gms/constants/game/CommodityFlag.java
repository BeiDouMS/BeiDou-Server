package org.gms.constants.game;

import lombok.Getter;
import org.gms.client.inventory.Item;
import org.gms.net.packet.OutPacket;
import org.gms.server.CashShop;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 这个枚举类无需多语言，字段名就是英文的，desc只是作为参考
 */
@Getter
public enum CommodityFlag {
    // 固有部分
    SN(0, 0, "SN", (p, n)-> p.writeInt(n.intValue())),
    FLAG(0, 1, "FLAG", (p, n)-> p.writeInt(n.intValue())),

    // 自定义部分
    ITEM_ID(1, 2, "物品ID", (p, n)-> p.writeInt(n.intValue())),
    COUNT(1 << 1, 3, "数量", (p, n)-> p.writeShort(n.intValue())),
    PRICE(1 << 2, 5, "价格", (p, n)-> p.writeInt(n.intValue())),
    BONUS(1 << 3, 6, "属性奖励", (p, n)-> p.writeByte(n.intValue() - 1)),
    PRIORITY(1 << 4, 4, "优先级", (p, n)-> p.writeByte(n.intValue())),
    PERIOD(1 << 5, 7, "有效期", (p, n)-> p.writeShort(n.intValue())),
    MAPLE_POINT(1 << 6, 8, "抵用券", (p, n)-> p.writeInt(n.intValue())),
    MESO(1 << 7, 9, "金币", (p, n)-> p.writeInt(n.intValue())),
    FOR_PREMIUM_USER(1 << 8, 10, "高级用户", (p, n)-> p.writeInt(n.intValue() - 1)),
    COMMODITY_GENDER(1 << 9, 11, "性别", (p, n)-> p.writeByte(n.intValue())),
    ON_SALE(1 << 10, 12, "是否销售", (p, n)-> p.writeByte(n.intValue())),
    CLASS(1 << 11, 13, "Unknown", (p, n)-> p.writeByte(n.intValue())),
    LIMIT(1 << 12, 14, "Unknown", (p, n)-> p.writeByte(n.intValue())),
    PB_CASH(1 << 13, 15, "Unknown", (p, n)-> p.writeShort(n.intValue())),
    PB_POINT(1 << 14, 16, "Unknown", (p, n)-> p.writeShort(n.intValue())),
    PB_GIFT(1 << 15, 17, "Unknown", (p, n)-> p.writeShort(n.intValue())),
    PACKAGE_SN(1 << 16, 18, "礼包SN", (p, n)-> {
        List<Item> itemList = CashShop.CashItemFactory.getPackage(n.intValue());
        if (itemList.isEmpty()) {
            p.writeByte(0);
        } else {
            p.writeByte(itemList.size());
            itemList.forEach(item -> p.writeInt(item.getSN()));
        }
    }),

    // 以下83不支持
    REQ_POP(1 << 17, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    REQ_LEVEL(1 << 18, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    TERM_START(1 << 19, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    TERM_END(1 << 20, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    REFUNDABLE(1 << 21, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    BOMB_SALE(1 << 22, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    FORCED_CATEGORY(1 << 23, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    GAME_WORLD(1 << 24, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    TOKEN(1 << 25, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    LIMIT_MAX(1 << 26, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    LIMIT_QUEST_ID(1 << 27, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    ORIGINAL_PRICE(1 << 28, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    DISCOUNT(1 << 29, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    DISCOUNT_RATE(1 << 30, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    MILEAGE_RATE(1 << 31, -1, "Unknown83", (p, n)-> p.writeByte(0)),
    ALL(-1, -1, "Unknown83", (p, n)-> p.writeByte(0));

    private final long flag;
    private final int sort;
    private final String desc;
    private final BiConsumer<OutPacket, Number> writeMapper;

    CommodityFlag(int flag, int sort, String desc, BiConsumer<OutPacket, Number> writeMapper) {
        this.flag = flag;
        this.sort = sort;
        this.desc = desc;
        this.writeMapper = writeMapper;
    }

    public static List<CommodityFlag> getAvailableSortedValues() {
        List<CommodityFlag> result = new ArrayList<>();
        for (CommodityFlag value : values()) {
            if (value.sort == -1 || "Unknown83".equals(value.desc)) {
                continue;
            }
            result.add(value);
        }
        result.sort(Comparator.comparing(CommodityFlag::getSort));
        return result;
    }
}
