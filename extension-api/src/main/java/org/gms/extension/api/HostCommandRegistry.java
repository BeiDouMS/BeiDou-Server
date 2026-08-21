package org.gms.extension.api;

/**
 * Registers in-game commands. BeiDou persists via command_info when possible;
 * Cosmic registers into CommandsExecutor.
 */
public interface HostCommandRegistry {

    /**
     * @param syntax      command name without bang (e.g. {@code bot})
     * @param level       required GM level
     * @param description help text
     * @param handler     invocation callback
     */
    void register(String syntax, int level, String description, HostCommandHandler handler);
}
