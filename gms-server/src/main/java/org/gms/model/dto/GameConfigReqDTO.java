package org.gms.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GameConfigReqDTO extends BasePageDTO {
    /**
     * 参数类型
     */
    private String type;

    /**
     * 参数子类型
     */
    private String subType;

    /**
     * 搜索文本：名称、描述
     */
    private String filter;
}
