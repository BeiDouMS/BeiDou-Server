package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CashShopSearchRtnDTO {
    private Integer categoryId;
    private String categoryName;
    private Integer subcategoryId;
    private String subcategoryName;
    private Integer sn;
    private Integer itemId;
    private String itemName;
    private Integer price;
    private Integer defaultPrice;
    private Long period;
    private Long defaultPeriod;
    private Integer priority;
    private Integer defaultPriority;
    private Short count;
    private Short defaultCount;
    private Integer onSale;
    private Integer defaultOnSale;
    private Integer bonus;
    private Integer defaultBonus;
    private Integer maplePoint;
    private Integer defaultMaplePoint;
    private Integer meso;
    private Integer defaultMeso;
    private Integer forPremiumUser;
    private Integer defaultForPremiumUser;
    private Integer gender;
    private Integer defaultGender;
    private Integer clz;
    private Integer defaultClz;
    private Integer limit;
    private Integer defaultLimit;
    private Integer pbCash;
    private Integer defaultPBCash;
    private Integer pbPoint;
    private Integer defaultPBPoint;
    private Integer pbGift;
    private Integer defaultPBGift;
    private Integer packageSn;
    private Integer defaultPackageSn;
}
