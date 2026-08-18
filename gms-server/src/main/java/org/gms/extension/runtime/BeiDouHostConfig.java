package org.gms.extension.runtime;

import dev.maple.extension.api.HostConfig;
import org.springframework.core.env.Environment;

/**
 * Reads SoloMapling / extension keys from Spring Environment ({@code application.yml}).
 */
public final class BeiDouHostConfig implements HostConfig {

    public static final String SPAWN_BOTS_ON_STARTUP = "solomapling.spawn-bots-on-startup";
    public static final String PLUGINS_DIR = "solomapling.plugins-dir";
    public static final String PLUGINS_ENABLED = "solomapling.plugins-enabled";

    private final Environment environment;

    public BeiDouHostConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean getBool(String key, boolean defaultValue) {
        Boolean value = environment.getProperty(key, Boolean.class);
        return value != null ? value : defaultValue;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        Integer value = environment.getProperty(key, Integer.class);
        return value != null ? value : defaultValue;
    }

    @Override
    public String getString(String key, String defaultValue) {
        String value = environment.getProperty(key);
        return value != null && !value.isBlank() ? value : defaultValue;
    }
}
