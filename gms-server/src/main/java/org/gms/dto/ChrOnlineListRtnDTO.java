package org.gms.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChrOnlineListRtnDTO {
    private int total;
    private List<Chr> data;

    @Getter
    @Setter
    public static class Chr {
        private int world;
        private int id;
        private String name;
        private int map;
        private int job;
        private int level;
        private int gm;
    }
}
