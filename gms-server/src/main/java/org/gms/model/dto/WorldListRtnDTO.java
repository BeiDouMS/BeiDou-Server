package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorldListRtnDTO {
    private Integer id;
    private Integer expRate;
    private Integer dropRate;
    private Integer mesoRate;
    private Integer bossDropRate;
    private Integer questRate;
    private Integer travelRate;
    private Integer fishingRate;
}
