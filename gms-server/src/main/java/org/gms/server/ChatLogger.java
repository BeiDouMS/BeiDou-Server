package org.gms.server;

import org.gms.client.Client;
import org.gms.config.YamlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatLogger {
    private static final Logger log = LoggerFactory.getLogger(ChatLogger.class);

    /**
     * Log a chat message (if enabled in the config)
     */
    public static void log(Client c, String chatType, String message) {
        if (YamlConfig.config.server.USE_ENABLE_CHAT_LOG) {
            log.info("({}) {}: {}", chatType, c.getPlayer().getName(), message);
        }
    }
}
