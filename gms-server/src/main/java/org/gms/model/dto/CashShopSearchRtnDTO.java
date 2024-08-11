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
    private Integer price;
    private Integer defaultPrice;
    private Long period;
    private Long defaultPeriod;
    private Integer priority;
    private Integer defaultPriority;
    private Short count;
    private Short defaultCount;
    private Boolean onSale;
    private boolean defaultOnSale;
}
