package org.gms.server;

import org.gms.client.Client;
import org.gms.config.GameConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatLogger {
    private static final Logger log = LoggerFactory.getLogger(ChatLogger.class);

    /**
     * Log a chat message (if enabled in the config)
     */
    public static void log(Client c, String chatType, String message) {
        if (GameConfig.getServerBoolean("use_enable_chat_log")) {
            log.info("({}) {}: {}", chatType, c.getPlayer().getName(), message);
        }
    }
}
