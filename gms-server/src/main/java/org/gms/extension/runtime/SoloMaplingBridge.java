package org.gms.extension.runtime;

import org.gms.client.Character;

import java.util.function.BiConsumer;

/**
 * Host-side hooks invoked by gms-server when the SoloMapling plugin is loaded.
 * No-op until {@link #install} is called from the plugin {@code onLoad}.
 */
public final class SoloMaplingBridge {

    private static volatile BiConsumer<Character, Integer> mapEnteredHandler = noopMap();
    private static volatile BiConsumer<Character, String> chatGeneralHandler = noopChat();
    private static volatile BiConsumer<Character, Character> botTradeRequestHandler = noopTrade();

    private SoloMaplingBridge() {
    }

    public static void install(
            BiConsumer<Character, Integer> onMapEntered,
            BiConsumer<Character, String> onChatGeneral,
            BiConsumer<Character, Character> onBotTradeRequest) {
        mapEnteredHandler = onMapEntered != null ? onMapEntered : noopMap();
        chatGeneralHandler = onChatGeneral != null ? onChatGeneral : noopChat();
        botTradeRequestHandler = onBotTradeRequest != null ? onBotTradeRequest : noopTrade();
    }

    public static void clear() {
        install(null, null, null);
    }

    public static void onMapEntered(Character chr, int mapId) {
        mapEnteredHandler.accept(chr, mapId);
    }

    public static void onChatGeneral(Character chr, String message) {
        chatGeneralHandler.accept(chr, message);
    }

    public static void addBotTradeRequest(Character bot, Character partner) {
        botTradeRequestHandler.accept(bot, partner);
    }

    private static BiConsumer<Character, Integer> noopMap() {
        return (chr, mapId) -> {
        };
    }

    private static BiConsumer<Character, String> noopChat() {
        return (chr, message) -> {
        };
    }

    private static BiConsumer<Character, Character> noopTrade() {
        return (bot, partner) -> {
        };
    }
}
