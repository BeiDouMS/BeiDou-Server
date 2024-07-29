package org.gms.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ShopSearchReqDTO extends BasePageDTO {
    private Long shopId;
    private Integer npcId;
    private String npcName;
    private Integer itemId;
    private String itemName;
}
