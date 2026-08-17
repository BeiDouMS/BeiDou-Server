package org.gms.extension.api;

/**
 * Typed config bridge. BeiDou maps GameConfig / application properties;
 * Cosmic maps YamlConfig.
 */
public interface HostConfig {

    boolean getBool(String key, boolean defaultValue);

    int getInt(String key, int defaultValue);

    String getString(String key, String defaultValue);
}
