package org.gms.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "gms.world")
@Component
@Data
public class WorldProperty {
    private List<WorldsConfig> worlds;

    @Data
    public static class WorldsConfig {
        public int flag = 0;
        public String server_message = "Welcome!";
        public String event_message = "";
        public String why_am_i_recommended = "";
        public int channels = 1;
        public int exp_rate = 1;
        public int meso_rate = 1;
        public int drop_rate = 1;
        public int boss_drop_rate = 1;
        public int quest_rate = 1;
        public int travel_rate = 1;
        public int fishing_rate = 1;
    }
}


