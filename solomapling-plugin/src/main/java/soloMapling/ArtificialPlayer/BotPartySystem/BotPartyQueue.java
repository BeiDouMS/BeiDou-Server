package soloMapling.ArtificialPlayer.BotPartySystem;

import org.gms.client.Character;

import java.util.concurrent.ConcurrentHashMap;

import static soloMapling.DebugUtilities.debugprint;

public class BotPartyQueue {

    public static final class PartyInviteEntry {
        private final Character inviter;
        private final int partyId;

        public PartyInviteEntry(Character inviter, int partyId) {
            this.inviter = inviter;
            this.partyId = partyId;
        }

        public Character getInviter() {
            return inviter;
        }

        public int getPartyId() {
            return partyId;
        }
    }

    private final ConcurrentHashMap<Character, PartyInviteEntry> queues;
    private static final BotPartyQueue instance = new BotPartyQueue();

    private BotPartyQueue() {
        queues = new ConcurrentHashMap<>();
    }

    public static BotPartyQueue getInstance() {
        return instance;
    }

    // Last-wins: the entry always mirrors the LATEST invite the engine actually created.
    // Concurrent-invite serialization is already the InviteCoordinator's job (its putIfAbsent
    // refuses a second live invite), so first-wins here only ever preserved STALE entries:
    // the coordinator expires an unanswered invite after ~3 min but this queue never did, and
    // a bot answering with the stale entry's old partyId hit NOT_FOUND at the coordinator -
    // leaving the player's live invite wedged ("taking care of another invitation") for 3 min.
    public void addPartyInvite(Character fakechar, Character inviter, int partyId) {
        debugprint("addPartyInvite: bot=" + fakechar.getName() + ", inviter=" + inviter.getName() + ", partyId=" + partyId);
        queues.put(fakechar, new PartyInviteEntry(inviter, partyId));
        // Wake the bot's macro brain now so pollInvites drains this on the next ~immediate tick,
        // rather than waiting out its slow scheduled cadence while the armed window ticks away.
        BotRecruitManager.wakeBotForInvite(fakechar);
    }

    public PartyInviteEntry getPartyInvite(Character fakechar) {
        return queues.get(fakechar);
    }

    public boolean hasPendingInvite(Character fakechar) {
        return queues.containsKey(fakechar);
    }

    public void removePartyInvite(Character fakechar) {
        queues.remove(fakechar);
    }
}
