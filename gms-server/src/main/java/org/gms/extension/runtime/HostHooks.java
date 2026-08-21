package org.gms.extension.runtime;

import org.gms.client.Character;
import org.gms.extension.api.ArtificialCharacters;
import org.gms.extension.api.HostEvent;
import org.gms.extension.api.HostRuntime;
import org.gms.extension.api.TradeParticipants;

/**
 * Convenience accessors for engine code: artificial-character checks, trade hooks, and host events.
 * Keeps {@code org.gms} free of plugin package imports.
 */
public final class HostHooks {

    private HostHooks() {
    }

    public static boolean isArtificial(Character character) {
        return character != null && ArtificialCharacters.isArtificial(character.getId());
    }

    public static boolean isArtificial(int characterId) {
        return ArtificialCharacters.isArtificial(characterId);
    }

    public static boolean tradeAutoAcceptVisit(int visitorId, int partnerId) {
        return TradeParticipants.autoAcceptVisit(visitorId, partnerId);
    }

    public static boolean tradeRelaxInventoryChecks(int characterId) {
        return TradeParticipants.relaxInventoryChecks(characterId);
    }

    public static boolean tradeSuppressPackets(int characterId) {
        return TradeParticipants.suppressTradePackets(characterId);
    }

    /** @return true if a plugin hook claimed exchange completion for this character */
    public static boolean tradeOnExchangeSuccess(int characterId) {
        return TradeParticipants.onExchangeSuccess(characterId);
    }

    public static void publish(HostEvent event) {
        if (event == null) {
            return;
        }
        HostRuntime runtime = ExtensionLoader.getInstance().getRuntime();
        if (runtime != null) {
            runtime.events().publish(event);
        }
    }
}
