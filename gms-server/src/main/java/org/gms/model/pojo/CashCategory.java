package org.gms.model.pojo;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gms.model.dto.BasePageDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CashCategory extends BasePageDTO {
    private Integer id;
    private String name;
    private Integer subId;
    private String subName;
    private Boolean onSale;
    private Integer itemId;
}
