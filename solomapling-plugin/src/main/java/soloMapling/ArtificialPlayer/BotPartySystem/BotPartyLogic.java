package soloMapling.ArtificialPlayer.BotPartySystem;

import org.gms.client.Character;

public class BotPartyLogic {

    // Poll method for a bot's state machine tick. Accepts any pending invite.
    // Later we can layer blocklist / whitelist / leader-only checks here, mirroring BotTradeLogic.
    public static boolean checkPartyQueue(Character fakechar) {
        if (!BotPartyQueue.getInstance().hasPendingInvite(fakechar)) {
            return false;
        }
        return BotPartyCommands.botAcceptPartyInvite(fakechar);
    }

    public static void clearPartyInvite(Character fakechar) {
        BotPartyQueue.getInstance().removePartyInvite(fakechar);
    }
}
