package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import org.gms.net.server.world.Party;
import org.gms.net.server.world.PartyCharacter;
import soloMapling.ArtificialPlayer.BotDialogueHandler;
import soloMapling.ArtificialPlayer.BotOptionMenu;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotTypeManager;
import soloMapling.ArtificialPlayer.BotGrindSystem.MapMobIndex;
import soloMapling.ArtificialPlayer.BotPartySystem.BotPartyCommands;
import soloMapling.ArtificialPlayer.BotPartySystem.BotPartyQueue;
import soloMapling.ArtificialPlayer.BotPartySystem.BotRecruitManager;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;
import soloMapling.server.BotTickService;

import java.util.List;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.BotLogger.log;

// A recruited companion: follows one real player everywhere. The heavy lifting is the GC follow
// engine (50ms same-map tailing + 400ms cross-map session that travels portal chains and redirects
// mid-trip - see GCFollow); this FSM is only the supervisor: it tracks the leader BY ID so a relog
// re-attaches (a session holds a hard Character ref and dies with it), re-arms the session whenever
// it's down, watches party membership, and converts itself away when the ride ends.
//
// Cadence: pinned fast (~750ms) and governor-exempt - a follower is foreground content whether or
// not its current map is observed (it may be catching up through empty maps). The map it shares
// with its leader is FULL by definition, so this costs almost nothing in practice.
//
// Lifecycle in: SocialBot recruit (party join -> convert), TrainingBot "Follow me!", !bot followbot.
// Lifecycle out: "Train here with me!" -> TrainingBot (station-here handoff); leader gone past the
// grace / party dissolved -> TrainingBot on a mob map, SocialBot in a town.
public class FollowerBot extends BotSM {

    private static final long FOLLOW_TICK_MS = 750;
    private static final long LEADER_LOST_GRACE_MS = 90_000;

    private enum FollowPhase { INIT, FOLLOW, LEADER_LOST }

    // Written on the macro tick, read from menu callbacks - keep volatile.
    private volatile FollowPhase followPhase = FollowPhase.INIT;
    private volatile int leaderId = -1;
    private volatile long leaderLostSinceMs = 0;
    private volatile boolean wasPartied = false;
    private volatile boolean pausedForTrade = false;

    // NOTE: no "here" keyword - "there" contains "here", so a casual "hi there" would trigger it.
    private final BotOptionMenu menu = new BotOptionMenu(this,
            List.of("Train here with me!", "Nevermind"),
            List.of(List.of("train", "grind", "station"), List.of("nevermind", "bye", "nah", "nope")),
            this::onMenuSelect);

    public FollowerBot(Character character) {
        super(character);
        botType = "FollowerBot";
        dialoguePath = "FollowerBotDialogue.yaml";
    }

    // Pinned cadence, observed or not: supervision (re-arm/relog/party checks) must never sleep 9-12s.
    @Override
    public void checkPrioritySpeed() {
        updateScheduleDelay(FOLLOW_TICK_MS);
    }

    @Override
    public synchronized void startScheduledTask(long initialDelayMs) {
        super.startScheduledTask(initialDelayMs);
        int id = getChr().getId();
        BotTickService.setNoThrottle(id, true);
        // Conversions inherit the spawn-choreography stagger, but a converted follower is already
        // live in-world - pull the first tick to the follow cadence so it starts moving immediately.
        BotTickService.reschedule(id, FOLLOW_TICK_MS);
    }

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }
        Character chr = getChr();
        if (chr == null || chr.getMap() == null) {
            return;
        }
        if (getState() == BotState.TRADING) {
            // Trades are sacred: halt the follow session so the bot doesn't walk out of the trade.
            if (!pausedForTrade) {
                pausedForTrade = true;
                GCMovement.stop(chr);
            }
            return;
        }
        pausedForTrade = false;

        switch (followPhase) {
            case INIT -> doInit();
            case FOLLOW -> doFollow();
            case LEADER_LOST -> doLeaderLost();
        }

        if (!getRunning()) {
            return; // a phase converted this bot away mid-tick
        }
        pollLeaderInvite();
        menu.poll(); // last: a selection may also convert this bot away
    }

    // An unpartied follower (e.g. via !bot followbot) accepts a party invite from its OWN leader -
    // that's how the GM flow gets party EXP going - and politely rejects anyone else so the
    // first-wins-free queue never holds a rotting entry.
    private void pollLeaderInvite() {
        Character chr = getChr();
        if (!BotPartyQueue.getInstance().hasPendingInvite(chr)) {
            return;
        }
        BotPartyQueue.PartyInviteEntry entry = BotPartyQueue.getInstance().getPartyInvite(chr);
        Character inviter = entry == null ? null : entry.getInviter();
        if (inviter != null && inviter.getId() == leaderId && chr.getParty() == null) {
            if (BotPartyCommands.botAcceptPartyInvite(chr)) {
                wasPartied = true;
                sayNode("PartyJoined", inviter);
            }
            return;
        }
        BotPartyCommands.botRejectPartyInvite(chr);
    }

    // ── Phases ───────────────────────────────────────────────────────────────

    private void doInit() {
        Character chr = getChr();
        int pending = BotRecruitManager.consumePendingLeader(chr.getId());
        leaderId = pending > 0 ? pending : leaderIdFromParty();
        if (leaderId <= 0) {
            fallbackConvert();
            return;
        }
        Character leader = resolveLeader();
        if (leader == null || leader.getMap() == null) {
            leaderLostSinceMs = now();
            followPhase = FollowPhase.LEADER_LOST;
            return;
        }
        wasPartied = chr.getParty() != null;
        GCMovement.follow(chr, leader);
        sayNode("FollowStart", leader);
        followPhase = FollowPhase.FOLLOW;
    }

    private void doFollow() {
        Character chr = getChr();
        Character leader = resolveLeader();
        if (leader == null || leader.getMap() == null) {
            leaderLostSinceMs = now();
            followPhase = FollowPhase.LEADER_LOST;
            sayNode("LeaderLost", null);
            return;
        }
        if (chr.getParty() != null) {
            wasPartied = true;
        } else if (wasPartied) {
            // Kicked / disband: the ride is over.
            sayNode("PartyFarewell", leader);
            fallbackConvert();
            return;
        }
        if (!GCMovement.isFollowing(chr)) {
            // Initial arm, post-trade re-arm, and relog re-attach (a dead session cleared itself;
            // the freshly resolved leader Character makes the new session current).
            GCMovement.follow(chr, leader);
        }
    }

    private void doLeaderLost() {
        Character chr = getChr();
        Character leader = resolveLeader();
        if (leader != null && leader.getMap() != null) {
            followPhase = FollowPhase.FOLLOW;
            return;
        }
        if (wasPartied && chr.getParty() == null) {
            fallbackConvert();
            return;
        }
        if (now() - leaderLostSinceMs > LEADER_LOST_GRACE_MS) {
            sayNode("PartyFarewell", null);
            fallbackConvert();
        }
    }

    // ── Menu (Dispatcher routes a "botname" chat here via displayCommands) ───

    @Override
    public void displayCommands(Character chr) {
        menu.show(chr);
    }

    private void onMenuSelect(int idx, Character player) {
        Character chr = getChr();
        if (idx == 0) { // Train here with me!
            if (player.getId() != leaderId) {
                sayNode("LoyalToLeader", player);
                menu.close(player);
                return;
            }
            if (MapMobIndex.level(chr.getMapId()) < 0) {
                sayNode("NoMobsHere", player);
                menu.close(player);
                return;
            }
            sayNode("StationHere", player);
            menu.close(player);
            BotRecruitManager.markStationHere(chr.getId());
            GCMovement.stop(chr);
            BotTypeManager.convertBotType(chr, BotTypeManager.BotType.TRAINING_BOT);
            return;
        }
        sayNode("Goodbye", player);
        menu.close(player);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    // Resolve the live leader from the channel's player storage each use (OPQ pattern): an id
    // survives relog, a Character reference doesn't.
    private Character resolveLeader() {
        if (leaderId <= 0) {
            return null;
        }
        return getChr().getClient().getChannelServer().getPlayerStorage().getCharacterById(leaderId);
    }

    private int leaderIdFromParty() {
        Party party = getChr().getParty();
        if (party == null) {
            return -1;
        }
        Character lead = party.getLeader() != null ? party.getLeader().getPlayer() : null;
        if (lead != null && !isBot(lead)) {
            return lead.getId();
        }
        Character member = firstRealMember(party);
        return member == null ? -1 : member.getId();
    }

    private Character firstRealMember(Party party) {
        for (PartyCharacter pc : party.getMembers()) {
            Character p = pc == null ? null : pc.getPlayer();
            if (p != null && !isBot(p) && p.getMap() != null) {
                return p;
            }
        }
        return null;
    }

    // The ride ended: become a TrainingBot on a mob map, a SocialBot in a town. Callers speak
    // their own farewell first; this only re-types.
    private void fallbackConvert() {
        Character chr = getChr();
        BotRecruitManager.clearArmed(chr.getId());
        GCMovement.stop(chr);
        boolean grindable = MapMobIndex.level(chr.getMapId()) >= 0;
        BotTypeManager.convertBotType(chr,
                grindable ? BotTypeManager.BotType.TRAINING_BOT : BotTypeManager.BotType.SOCIAL_BOT);
    }

    private void sayNode(String node, Character player) {
        Character chr = getChr();
        if (chr == null || chr.getMap() == null || !GCMovement.isMapObserved(chr.getMapId())) {
            return;
        }
        try {
            String line = BotDialogueHandler.getRandomResolvedLine(dialoguePath, botType, node, chr, player);
            if (line != null) {
                BotSpeak(chr, line);
            }
        } catch (Exception e) {
            // a missing dialogue node must never break the follow loop
        }
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    @Override
    public synchronized void stopScheduledTask() {
        Character chr = getChr();
        if (chr != null) {
            BotRecruitManager.clearArmed(chr.getId()); // pending station/leader handoffs survive on purpose
            GCMovement.disable(chr); // ends the follow session + releases the shared movement lock
        }
        super.stopScheduledTask();
        log("[FollowerBot] stopped: " + (chr != null ? chr.getName() : "?"));
    }
}
