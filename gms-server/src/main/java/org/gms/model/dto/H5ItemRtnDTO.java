package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * H5 挂机物品收益。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class H5ItemRtnDTO {
    private Integer itemId;
    private String itemName;
    private Long quantity;
}
