package org.gms.model.dto;

import lombok.Getter;
import lombok.Setter;
import org.gms.dao.entity.ModifiedCashItemDO;

@Getter
@Setter
public class CashShopBatchOnSaleReqDTO {
    private ModifiedCashItemDO[] data;
    private String type;
    private Integer value;
}
