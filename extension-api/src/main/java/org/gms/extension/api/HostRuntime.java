package org.gms.extension.api;

import java.util.Optional;

/**
 * Facade exposed to plugins. Implementations live in host runtimes
 * (e.g. beidou-extension-runtime); plugins must not depend on engine types.
 */
public interface HostRuntime {

    HostConfig config();

    HostEventBus events();

    HostCommandRegistry commands();

    /**
     * Optional atomic native-character provisioning capability. The default
     * keeps existing host implementations binary/source compatible.
     */
    default Optional<HostCharacterProvisioner> characterProvisioner() {
        return Optional.empty();
    }

    default Optional<HostItemActions> itemActions() {
        return Optional.empty();
    }

    default Optional<HostMonsterDrops> monsterDrops() {
        return Optional.empty();
    }

    /** Host identifier: {@code beidou}, {@code cosmic}, … */
    String hostId();
}
