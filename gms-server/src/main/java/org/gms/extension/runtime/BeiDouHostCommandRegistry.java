package org.gms.extension.runtime;

import org.gms.extension.api.HostCommandHandler;
import org.gms.extension.api.HostCommandRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory command table for plugin-registered commands.
 * Full command_info DB wiring can be added later; for now GM dispatch can consult this map.
 */
public final class BeiDouHostCommandRegistry implements HostCommandRegistry {

    private static final Logger log = LoggerFactory.getLogger(BeiDouHostCommandRegistry.class);

    public record RegisteredCommand(String syntax, int level, String description, HostCommandHandler handler) {
    }

    private final Map<String, RegisteredCommand> commands = new ConcurrentHashMap<>();

    @Override
    public void register(String syntax, int level, String description, HostCommandHandler handler) {
        String key = syntax.toLowerCase();
        commands.put(key, new RegisteredCommand(key, level, description, handler));
        log.info("Registered extension command '!{}' (gm level {})", key, level);
    }

    public RegisteredCommand get(String syntax) {
        return commands.get(syntax.toLowerCase());
    }

    public Map<String, RegisteredCommand> snapshot() {
        return Map.copyOf(commands);
    }
}
