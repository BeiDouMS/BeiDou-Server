package soloMapling.ArtificialPlayer.BotTradeSystem;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotBlockList;

public class BotTradeLogic {

    public static boolean checkTradeQueue(Character fakechar) {
        BotTradeQueue queue = BotTradeQueue.getInstance();
        if (!queue.hasPendingTrades(fakechar)) {
            return false;
        }
        // The inviter may have cancelled before this tick ran — the trade object is already gone
        // but the queue entry survived. Treat it as stale and drop it instead of dereferencing.
        Character partner = queue.getTradeRequest(fakechar);
        if (fakechar.getTrade() == null || partner == null) {
            queue.removeTradeRequest(fakechar);
            return false;
        }
        if (BotBlockList.getInstance().isBlocked(fakechar.getId(), partner.getId())) {
            queue.removeTradeRequest(fakechar);
            BotTradeCommands.declineTradeInvite(fakechar);
            return false;
        }
        acceptTradeRequest(fakechar);
        return true;
    }

    private static void acceptTradeRequest(Character fakechar) {
        BotTradeCommands.acceptTradeInvite(fakechar);
        BotTradeQueue.getInstance().removeTradeRequest(fakechar);
    }

    private static void rejectTradeRequest(Character fakechar) {
        BotTradeCommands.declineTradeInvite(fakechar);
        BotTradeQueue.getInstance().removeTradeRequest(fakechar);
    }

    public static void clearTradeRequest(Character fakechar) {
        BotTradeQueue.getInstance().removeTradeRequest(fakechar);
    }


}
