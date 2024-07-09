package org.gms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChrOnlineListReqDTO {
    private int page;
    private int size;
    private int id;
    private String name;
    private int map;
}
