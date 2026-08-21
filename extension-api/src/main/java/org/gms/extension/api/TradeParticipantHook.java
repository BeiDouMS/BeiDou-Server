package org.gms.extension.api;

/**
 * Plugin-registered trade participation rules for headless / artificial characters.
 * Host {@code Trade} must consult this instead of hard-coding bot semantics.
 */
public interface TradeParticipantHook {

    /**
     * When {@code visitTrade(visitor, partner)} runs, should the visit proceed even if
     * InviteCoordinator did not return {@code ACCEPTED}? Typical: partner (or visitor) is
     * plugin-owned and has no client to answer the invite packet.
     */
    default boolean autoAcceptVisit(int visitorId, int partnerId) {
        return false;
    }

    /**
     * Skip inventory ownership / slot checks for this participant (bots often hold virtual stock).
     */
    default boolean relaxInventoryChecks(int characterId) {
        return false;
    }

    /**
     * If true, host skips sending trade UI packets to this character (no real client).
     */
    default boolean suppressTradePackets(int characterId) {
        return false;
    }

    /**
     * Called instead of the normal {@code completeTrade()} item/meso delivery path when the
     * exchange succeeds for this participant. Return {@code true} if this hook handled the
     * character (host must not also run {@code completeTrade()}).
     */
    default boolean onExchangeSuccess(int characterId) {
        return false;
    }
}
