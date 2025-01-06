package org.gms.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class InventorySearchReqDTO extends BasePageDTO {
    private Byte inventoryType;
    private Integer characterId;
    private String characterName;
    private boolean onlineStatus;
}
