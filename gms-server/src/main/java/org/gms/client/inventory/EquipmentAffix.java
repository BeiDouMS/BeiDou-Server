package org.gms.client.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentAffix {
    private int slotIndex;
    private String affixCode;
    private int affixTier;
    private int value;
    private long rollSeed;
    private boolean locked;
}
