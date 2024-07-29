package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopItemSearchRtnDTO {
    private Long id;
    private Long shopId;
    private Integer itemId;
    private Integer price;
    private Integer pitch;
    private Integer position;
    private String itemName;
    private String itemDesc;
}
