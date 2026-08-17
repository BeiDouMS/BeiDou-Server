package soloMapling.ArtificialPlayer.BotTradeSystem;

import org.gms.client.Character;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BotTradeQueue {
    private static final BotTradeQueue INSTANCE = new BotTradeQueue();

    private final Map<Character, Character> queues = new ConcurrentHashMap<>();

    private BotTradeQueue() {
    }

    public static BotTradeQueue getInstance() {
        return INSTANCE;
    }

    public void addTradeRequest(Character bot, Character partner) {
        queues.putIfAbsent(bot, partner);
    }

    public Character getTradeRequest(Character bot) {
        return queues.get(bot);
    }

    public boolean hasPendingTrades(Character bot) {
        return queues.containsKey(bot);
    }

    public void removeTradeRequest(Character bot) {
        queues.remove(bot);
    }
}
