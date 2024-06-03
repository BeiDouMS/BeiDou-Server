package org.gms.config;

import org.gms.manager.ServerManager;
import org.gms.property.ServerProperty;
import org.gms.property.WorldProperty;
import org.springframework.context.ApplicationContext;

import java.util.List;


public class YamlConfig {
    public static final YamlConfig config = new YamlConfig();

    public List<WorldProperty.WorldsConfig> worlds;
    public ServerProperty server;

    private YamlConfig() {
        ApplicationContext applicationContext = ServerManager.getApplicationContext();
        this.server = applicationContext.getBean(ServerProperty.class);
        this.worlds = applicationContext.getBean(WorldProperty.class).getWorlds();
    }
}
