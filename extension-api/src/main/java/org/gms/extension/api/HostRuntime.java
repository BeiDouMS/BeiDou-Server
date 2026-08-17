package org.gms.extension.api;

/**
 * Facade exposed to plugins. Implementations live in host runtimes
 * (e.g. beidou-extension-runtime); plugins must not depend on engine types.
 */
public interface HostRuntime {

    HostConfig config();

    HostEventBus events();

    HostCommandRegistry commands();

    /** Host identifier: {@code beidou}, {@code cosmic}, … */
    String hostId();
}
