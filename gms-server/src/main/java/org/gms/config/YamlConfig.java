package org.gms.config;

import org.gms.manager.ServerManager;
import org.gms.property.ServerProperty;
import org.gms.property.WorldProperty;
import org.gms.service.ConfigService;

import java.util.List;

public class YamlConfig {
    public static final ConfigService configService = ServerManager.getApplicationContext().getBean(ConfigService.class);
    public static final YamlConfig config = new YamlConfig();
    public List<WorldProperty.WorldsConfig> worlds;
    public ServerProperty server;

    private YamlConfig() {
        loadConfig();
    }

    public void loadConfig() {
        this.worlds = configService.loadWorldProperty();
        this.server = configService.loadServerProperty();
    }
}
