package org.gms.extension.api;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry of {@link TradeParticipantHook}s. Engine trade code must use this (via HostHooks)
 * instead of importing plugin packages.
 */
public final class TradeParticipants {

    private static final CopyOnWriteArrayList<TradeParticipantHook> HOOKS = new CopyOnWriteArrayList<>();

    private TradeParticipants() {
    }

    public static void register(TradeParticipantHook hook) {
        if (hook != null) {
            HOOKS.addIfAbsent(hook);
        }
    }

    public static void unregister(TradeParticipantHook hook) {
        HOOKS.remove(hook);
    }

    public static void clear() {
        HOOKS.clear();
    }

    public static List<TradeParticipantHook> hooks() {
        return List.copyOf(HOOKS);
    }

    public static boolean autoAcceptVisit(int visitorId, int partnerId) {
        for (TradeParticipantHook hook : HOOKS) {
            if (hook.autoAcceptVisit(visitorId, partnerId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean relaxInventoryChecks(int characterId) {
        for (TradeParticipantHook hook : HOOKS) {
            if (hook.relaxInventoryChecks(characterId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean suppressTradePackets(int characterId) {
        for (TradeParticipantHook hook : HOOKS) {
            if (hook.suppressTradePackets(characterId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if a hook claimed exchange completion for this character
     */
    public static boolean onExchangeSuccess(int characterId) {
        boolean claimed = false;
        for (TradeParticipantHook hook : HOOKS) {
            if (hook.onExchangeSuccess(characterId)) {
                claimed = true;
            }
        }
        return claimed;
    }
}
