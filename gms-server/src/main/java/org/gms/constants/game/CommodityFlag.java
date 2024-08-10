package org.gms.constants.game;

import lombok.Getter;

/**
 * 这个枚举类不用汉化，字段名就是英文的
 */
@Getter
public enum CommodityFlag {
    // 固有部分
    SN(0, 0, "SN"),
    FLAG(0, 1, "FLAG"),

    // 自定义部分
    ITEM_ID(1, 2, "物品ID"),
    COUNT(1 << 1, 3, "数量"),
    PRICE(1 << 2, 5, "价格"),
    BONUS(1 << 3, 6, "属性奖励"),
    PRIORITY(1 << 4, 4, "优先级"),
    PERIOD(1 << 5, 7, "有效期"),
    MAPLE_POINT(1 << 6, 10, "抵用券"),
    MESO(1 << 7, 11, "金币"),
    FOR_PREMIUM_USER(1 << 8, 12, "高级用户"),
    COMMODITY_GENDER(1 << 9, 13, "性别"),
    ON_SALE(1 << 10, 14, "是否销售"),
    CLASS(1 << 11, 15, "Unknown"),
    LIMIT(1 << 12, 16, "Unknown"),
    PB_CASH(1 << 13, 17, "Unknown"),
    PB_POINT(1 << 14, 18, "Unknown"),
    PB_GIFT(1 << 15, 19, "Unknown"),
    PACKAGE_SN(1 << 16, 20, "礼包SN"),

    // 以下83不支持
    REQ_POP(1 << 17, 8, "Unknown83"),
    REQ_LEVEL(1 << 18, 9, "Unknown83"),
    TERM_START(1 << 19, -1, "Unknown83"),
    TERM_END(1 << 20, -1, "Unknown83"),
    REFUNDABLE(1 << 21, -1, "Unknown83"),
    BOMB_SALE(1 << 22, -1, "Unknown83"),
    FORCED_CATEGORY(1 << 23, -1, "Unknown83"),
    GAME_WORLD(1 << 24, -1, "Unknown83"),
    TOKEN(1 << 25, -1, "Unknown83"),
    LIMIT_MAX(1 << 26, -1, "Unknown83"),
    LIMIT_QUEST_ID(1 << 27, -1, "Unknown83"),
    ORIGINAL_PRICE(1 << 28, -1, "Unknown83"),
    DISCOUNT(1 << 29, -1, "Unknown83"),
    DISCOUNT_RATE(1 << 30, -1, "Unknown83"),
    MILEAGE_RATE(1 << 31, -1, "Unknown83"),
    ALL(-1, -1, "Unknown83");

    private final long flag;
    private final int sort;
    private final String desc;

    CommodityFlag(int flag, int sort, String desc) {
        this.flag = flag;
        this.sort = sort;
        this.desc = desc;
    }


}
