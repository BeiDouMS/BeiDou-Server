package org.gms.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChrOnlineListReqDTO extends BasePageDTO {
    private Integer id;
    private String name;
    private Integer map;
    private int world;
}
