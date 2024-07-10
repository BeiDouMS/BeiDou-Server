package org.gms.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChrOnlineListRtnDTO {
    private int world;
    private int id;
    private String name;
    private int map;
    private int job;
    private int level;
    private int gm;

}
