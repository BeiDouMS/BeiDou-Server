package org.gms.model.dto;

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
    private String jobName;
    private int level;
    private int gm;

}
