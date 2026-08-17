package soloMapling.ArtificialPlayer.BotPartySystem;

import org.gms.client.Character;
import org.gms.net.server.coordinator.world.InviteCoordinator;
import org.gms.net.server.coordinator.world.InviteCoordinator.InviteResult;
import org.gms.net.server.coordinator.world.InviteCoordinator.InviteResultType;
import org.gms.net.server.coordinator.world.InviteCoordinator.InviteType;
import org.gms.net.server.world.Party;
import org.gms.net.server.world.PartyCharacter;
import org.gms.net.server.world.PartyOperation;
import org.gms.net.server.world.World;
import org.gms.server.maps.MapleMap;
import org.gms.util.PacketCreator;

import static soloMapling.DebugUtilities.debugprint;

public class BotPartyCommands {

    public static boolean botMakeParty(Character fakechar) {
        if (fakechar.getParty() != null) {
            debugprint("botMakeParty: bot already in a party, skipping.");
            return false;
        }
        boolean created = Party.createParty(fakechar, false);
        debugprint("botMakeParty: created=" + created);
        return created;
    }

    // Server-side only leave. Skips player-client concerns (MCPQ, EventInstance,
    // MatchChecker, partySearch) since bots don't participate in any of those.
    public static void botLeaveParty(Character fakechar) {
        Party party = fakechar.getParty();
        if (party == null) {
            debugprint("botLeaveParty: no party, skipping.");
            return;
        }

        PartyCharacter botPC = fakechar.getMPC();
        if (botPC == null) {
            botPC = new PartyCharacter(fakechar);
        }

        World world = fakechar.getWorldServer();
        int partyId = party.getId();

        if (botPC.getId() == party.getLeaderId()) {
            world.removeMapPartyMembers(partyId);
            world.updateParty(partyId, PartyOperation.DISBAND, botPC);
            debugprint("botLeaveParty: bot=" + fakechar.getName() + " disbanded party " + partyId);
        } else {
            MapleMap map = fakechar.getMap();
            if (map != null) {
                map.removePartyMember(fakechar, partyId);
            }
            world.updateParty(partyId, PartyOperation.LEAVE, botPC);
            debugprint("botLeaveParty: bot=" + fakechar.getName() + " left party " + partyId);
        }

        fakechar.setParty(null);
    }

    public static boolean botAcceptPartyInvite(Character fakechar) {
        BotPartyQueue.PartyInviteEntry entry = BotPartyQueue.getInstance().getPartyInvite(fakechar);
        if (entry == null) {
            debugprint("botAcceptPartyInvite: no pending invite for " + fakechar.getName());
            return false;
        }

        int partyId = entry.getPartyId();
        InviteResult res = InviteCoordinator.answerInvite(InviteType.PARTY, fakechar.getId(), partyId, true);
        BotPartyQueue.getInstance().removePartyInvite(fakechar);

        if (res.result == InviteResultType.ACCEPTED) {
            boolean joined = Party.joinParty(fakechar, partyId, false);
            if (!joined) {
                // joinParty fails silently to the inviter (party disbanded / full / bot already
                // partied) - tell them so a clean re-invite is the obvious next move.
                Character inviter = entry.getInviter();
                if (inviter != null) {
                    inviter.sendPacket(PacketCreator.serverNotice(5, fakechar.getName()
                            + " couldn't join your party (it was full or disbanded) - try inviting again."));
                }
            }
            debugprint("botAcceptPartyInvite: joined=" + joined + " partyId=" + partyId);
            return joined;
        }
        debugprint("botAcceptPartyInvite: invite expired/invalid, result=" + res.result);
        return false;
    }

    public static boolean botRejectPartyInvite(Character fakechar) {
        BotPartyQueue.PartyInviteEntry entry = BotPartyQueue.getInstance().getPartyInvite(fakechar);
        if (entry == null) {
            debugprint("botRejectPartyInvite: no pending invite, no-op.");
            return false;
        }

        InviteResult res = InviteCoordinator.answerInvite(InviteType.PARTY, fakechar.getId(), entry.getPartyId(), false);
        BotPartyQueue.getInstance().removePartyInvite(fakechar);

        // Only tell the inviter the bot declined when the coordinator entry actually existed and was
        // denied (DENIED). A NOT_FOUND means the invite was already gone/superseded, so the notice
        // would be misleading - clear the queue entry either way.
        Character inviter = entry.getInviter();
        if (inviter != null && res.result == InviteResultType.DENIED) {
            inviter.sendPacket(PacketCreator.serverNotice(5, fakechar.getName() + " has declined your party request."));
        }
        debugprint("botRejectPartyInvite: result=" + res.result + " inviter=" + (inviter == null ? "?" : inviter.getName()));
        return true;
    }

    // Bot sends a party invite to a real player.
    // If bot has no party, creates one (bot becomes leader).
    // If bot is already in a party but NOT the leader, the invite is refused.
    public static boolean botInvitePlayer(Character fakechar, Character target) {
        if (target == null) {
            debugprint("botInvitePlayer: target null.");
            return false;
        }
        if (target.getParty() != null) {
            debugprint("botInvitePlayer: target already in a party.");
            return false;
        }

        Party party = fakechar.getParty();
        if (party == null) {
            if (!Party.createParty(fakechar, false)) {
                debugprint("botInvitePlayer: failed to create party for bot.");
                return false;
            }
            party = fakechar.getParty();
        } else if (party.getLeaderId() != fakechar.getId()) {
            debugprint("botInvitePlayer: bot is in a party but not the leader, cannot invite.");
            return false;
        }

        if (party.getMembers().size() >= 6) {
            debugprint("botInvitePlayer: party is full.");
            return false;
        }

        if (InviteCoordinator.createInvite(InviteType.PARTY, fakechar, party.getId(), target.getId())) {
            target.sendPacket(PacketCreator.partyInvite(fakechar));
            debugprint("botInvitePlayer: invite sent to " + target.getName() + " for partyId=" + party.getId());
            return true;
        }

        debugprint("botInvitePlayer: InviteCoordinator rejected (target already has pending invite).");
        return false;
    }
}
