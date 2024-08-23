package org.gms.model.dto;

import com.mybatisflex.annotation.Column;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gms.dao.entity.InventoryequipmentDO;


@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class InventoryequipmentReqDTO  extends BasePageDTO{

    private Long inventoryequipmentid;

    private Long inventoryitemid;



}