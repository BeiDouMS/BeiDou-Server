package soloMapling.ArtificialPlayer;

import org.gms.client.BotClient;
import org.gms.client.Client;
import soloMapling.server.SoloMaplingConstants.GameConstants;

/**
 * Single source of truth for the shared headless bot {@link Client}.
 */
public class BotClientHandler {

    private static volatile Client botClient = null;

    /**
     * Constructs the one shared headless bot client. Idempotent — safe to call
     * more than once (only the first call builds the instance). Must run after
     * the channels exist, since the client reports {@code WORLD_SCANIA} /
     * {@code CHANNEL_1} for routing.
     */
    public static synchronized void initHeadlessBotClient() {
        if (botClient == null) {
            botClient = new BotClient(GameConstants.WORLD_SCANIA, GameConstants.CHANNEL_1);
        }
    }

    /** The shared headless client every bot routes through. Null until {@link #initHeadlessBotClient()} runs. */
    public static Client getBotClient() {
        return botClient;
    }
}
