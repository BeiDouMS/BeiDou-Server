package org.gms.extension.api;

/**
 * Plugin entry discovered via {@link java.util.ServiceLoader} /
 * {@code META-INF/services/org.gms.extension.api.ServerExtension}.
 */
public interface ServerExtension {

    String id();

    default String version() {
        return "0.0.0";
    }

    /**
     * Called once after the host runtime is constructed. Plugins should retain
     * {@code runtime}, register commands, and subscribe to events here.
     */
    void onLoad(HostRuntime runtime);

    /**
     * Called when the game server has finished {@code Server.init()} and channels are online.
     */
    default void onServerReady() {
    }

    /**
     * Called on host shutdown. Release schedulers, shops, and tick wheels.
     */
    default void onUnload() {
    }
}
