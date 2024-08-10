package org.gms.model.pojo;

import lombok.*;
import org.gms.model.dto.BasePageDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CashCategory extends BasePageDTO {
    private Integer id;
    private String name;
    private Integer subId;
    private String subName;
    private Boolean onSale;
}
