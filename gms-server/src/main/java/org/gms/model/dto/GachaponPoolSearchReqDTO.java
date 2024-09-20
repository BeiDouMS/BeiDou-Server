package org.gms.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GachaponPoolSearchReqDTO extends BasePageDTO {
    private Integer gachaponId;
}
