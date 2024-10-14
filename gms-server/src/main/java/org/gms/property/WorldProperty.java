package org.gms.property;

import lombok.Data;

import java.util.List;

@Data
public class WorldProperty {
    private List<WorldsConfig> worlds;

    @Data
    public static class WorldsConfig {
        public int id = 0;
        public int flag = 0;
        public String server_message = "Welcome!";
        public String event_message = "";
        public String why_am_i_recommended = "";
        public float channels = 1;
        public float exp_rate = 1;
        public float meso_rate = 1;
        public float drop_rate = 1;
        public float boss_drop_rate = 1;
        public float quest_rate = 1;
        public float travel_rate = 1;
        public float fishing_rate = 1;
        public float level_exp_rate = 0;
        public int quick_level = 0;
        public float quick_level_exp_rate = 0;
    }
}


