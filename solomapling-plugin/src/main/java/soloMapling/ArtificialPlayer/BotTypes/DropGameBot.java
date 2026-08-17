package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import org.gms.client.Skill;
import org.gms.client.SkillFactory;
import soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecording;
import soloMapling.ArtificialPlayer.BotPartySystem.BotPartyCommands;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeCommands;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeQueue;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeSM;
import soloMapling.server.BotTiming;
import soloMapling.server.ExecutorServiceManager;

import java.awt.*;
import java.util.List;
import java.util.concurrent.Future;

import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.getMovementRecording;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.BotMoveStream;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.BotLogger.log;
import static soloMapling.server.SoloMaplingUtilities.random;

public class DropGameBot extends BotSM {

    // --- Configuration ---
    private static final int MEDIUM_TIER_COST = 10_000_000;
    private static final int ELITE_TIER_COST  = 50_000_000;
    private static final int GAME_DURATION_MS  = 120_000; // 2 minutes
    private static final int DROP_INTERVAL_MIN_MS = 4000;
    private static final int DROP_INTERVAL_MAX_MS = 6000;
    private static final int PARTY_TIMEOUT_MS = 15_000;
    private static final int HASTE_SKILL_ID = 4101004;
    private static final String MOVEMENT_RECORDING_NAME = "dg_potshop_1";
    private static final int DROP_INITIAL_DELAY_MIN_MS = 3000;
    private static final int DROP_INITIAL_DELAY_MAX_MS = 6000;
    private static final int MEDIUM_DESPAWN_DELAY_MS = 1400;
    private static final int ELITE_DESPAWN_DELAY_MS = 1150;

    private static final int[] FALLBACK_DROP_ITEM_IDS = {
            2000004, 2000005, 2022000, 2022179, 2040002, 2040505, 2049000, 2070005
    };

    // --- State ---
    // selectedTier/player are volatile: the drop and haste timers read them off-tick
    private DropGameState dropGameState = DropGameState.RESET;
    private volatile String selectedTier; // "medium" or "elite"
    private volatile Character player;   // the participating player
    private DropGameLootPool lootPool;

    // --- Timers ---
    private volatile long stateStartTime;
    private volatile long stateEndTime; // read by drop timers off-tick

    // --- Drop game async ---
    // drops ride self-re-arming BotTiming one-shots; dropsActive gates stragglers
    private volatile boolean dropsActive = false;

    // --- Movement playback async ---
    // `dg_potshop_1` is ~2 minutes long and BotMoveStream blocks for the full
    // duration, so it runs on the shared virtual-thread executor; cancel(true)
    // interrupts the stream.
    private Future<?> movementTask;

    // --- Trade handshake ---
    private boolean tradeDetected = false;

    private List<String> hint;

    public DropGameBot(Character character) {
        super(character);
        dialoguePath = "DropGameBotDialogue.yaml";
        botType = "DropGameBot";
        hint = List.of(character.getName());
    }

    private enum DropGameState {
        RESET,
        IDLE_ADVERTISE,
        TRADE_WAIT,
        TRADE_VALIDATE,
        TRADE_FINALIZE,
        PARTY_SETUP,
        PARTY_WAIT,
        PRE_GAME,
        GAME_LAUNCH,
        ACTIVE_GAME,
        END_GAME,
        POST_GAME
    }

    private void setDropGameState(DropGameState newState) {
        if (this.dropGameState != newState) {
            dprint("STATE " + this.dropGameState + " -> " + newState);
        }
        this.dropGameState = newState;
    }

    // Drain stray trade requests from any player other than our locked-in `player`.
    private void rejectOtherTradeRequests() {
        Character requester = BotTradeQueue.getInstance().getTradeRequest(getChr());
        if (requester == null) return;
        if (requester == player) return;
        dprint("draining stray trade request from " + requester.getName() + " — busy");
        BotTradeQueue.getInstance().removeTradeRequest(getChr());
        try {
            BotTradeCommands.declineTradeInvite(getChr());
        } catch (Exception e) {
            log("DropGameBot: declineTradeInvite failed: " + e.getMessage());
        }
    }

    private void resetDropGameState() {
        setDropGameState(DropGameState.RESET);
        selectedTier = null;
        player = null;
        lootPool = null;
        stateStartTime = 0;
        stateEndTime = 0;
        tradeDetected = false;
        hint = List.of(getChr().getName());
        stopDropScheduler();
    }

    // =========================================================================
    // TRADE OVERRIDE
    // =========================================================================
    // The DropGameBot does NOT use the standard BotTradeSM.
    // When BotSM detects a trade partner and enters TRADING state, it calls
    // tradeInitialized(). We override this to set a flag instead of creating
    // a BotTradeSM, then handle the trade in our own state machine.

    @Override
    protected void tradeInitialized(BotTradeSM.TradeMode tradeMode) {
        Character incoming = getTradeHandler().getTradePartnerRaw();
        // Lockout: if we are already engaged with a player, refuse anyone else.
        if (player != null && incoming != null && incoming != player) {
            dprint("REJECT trade from " + incoming.getName()
                    + " — already engaged with " + player.getName());
            BotTradeCommands.writeTradeChat(getChr(),
                    "Busy with " + player.getName() + "! Try again after this game.");
            BotTiming.after(1500, () -> BotTradeCommands.cancelTrade(getChr()));
            waitFor(2000); // hold ticks until the delayed cancel lands
            cleanupTradeState();
            return;
        }
        // Do NOT create a BotTradeSM. Just flag that a trade was detected.
        tradeDetected = true;
        player = incoming;
        dprint("tradeInitialized — partner=" + (player == null ? "?" : player.getName()));
    }

    // =========================================================================
    // MAIN STATE MACHINE
    // =========================================================================

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }

        // Parent's TRADING branch would NPE since we never create a BotTradeSM.
        // Force state back to RUNNING — we manage the trade flow ourselves.
        if (state == BotState.TRADING) {
            state = BotState.RUNNING;
        }

        // Handle trade detection: when BotSM enters TRADING and we flagged it,
        // transition our internal state to TRADE_WAIT.
        if (tradeDetected && dropGameState == DropGameState.IDLE_ADVERTISE) {
            tradeDetected = false;
            dprint("transition IDLE_ADVERTISE -> TRADE_WAIT (player="
                    + (player == null ? "?" : player.getName()) + ")");
            setDropGameState(DropGameState.TRADE_WAIT);
        }

        // While engaged, drain any trade requests from other players.
        if (dropGameState != DropGameState.RESET
                && dropGameState != DropGameState.IDLE_ADVERTISE) {
            rejectOtherTradeRequests();
        }

        getDebugger().debugLoggingFull(
                String.format("%s DropGameState: %s", getChr().getName(), dropGameState),
                String.format("%s", dropGameState));

        switch (dropGameState) {
            case RESET:
                resetDropGameState();
                setDropGameState(DropGameState.IDLE_ADVERTISE);
                break;

            case IDLE_ADVERTISE:
                idleAdvertise();
                break;

            case TRADE_WAIT:
                tradeWait();
                break;

            case TRADE_VALIDATE:
                tradeValidate();
                break;

            case TRADE_FINALIZE:
                tradeFinalize();
                break;

            case PARTY_SETUP:
                partySetup();
                break;

            case PARTY_WAIT:
                partyWait();
                break;

            case PRE_GAME:
                preGame();
                break;

            case GAME_LAUNCH:
                gameLaunch();
                break;

            case ACTIVE_GAME:
                activeGame();
                break;

            case END_GAME:
                endGame();
                break;

            case POST_GAME:
                endGameCleanup();
                break;

            default:
                log("DropGameBot unexpected state: " + dropGameState);
                state = BotState.FINISHED;
                resetDropGameState();
                break;
        }
    }

    // =========================================================================
    // STATE IMPLEMENTATIONS
    // =========================================================================

    // --- IDLE / ADVERTISE ---
    private void idleAdvertise() {
        // Auto-accept incoming trade invites (parent doesn't do this for us).
        checkForTrades();
        if (random.nextInt(100) < 8) {
            dprint("advertising");
            getDialogueHandler().executeBotFlavorDialogue("Advertise", DropGameBot.this);
        }
    }

    // --- TRADE WAIT ---
    // Bot is now in a trade with the player. Show rules, wait for player to lock.
    private void tradeWait() {
        if (!isTradeActive()) {
            dprint("TRADE_WAIT: trade no longer active, cancelling");
            cancelAndReset("Trade cancelled.");
            return;
        }
        dprint("TRADE_WAIT: writing rules, starting 60s timer");
        // Show rules in trade chat
        BotTradeCommands.writeTradeChat(getChr(),
                "Drop Game! Medium: 10m / Elite: 50m");
        BotTradeCommands.writeTradeChat(getChr(),
                "Put in your mesos and confirm!");
        startTimer(60_000); // 60s trade timeout
        setDropGameState(DropGameState.TRADE_VALIDATE);
    }

    // --- TRADE VALIDATE ---
    // Poll until partner locks in, then read mesos and decide tier.
    private void tradeValidate() {
        if (!isTradeActive()) {
            cancelAndReset("Trade cancelled.");
            return;
        }

        // Wait for partner to lock
        if (!BotTradeCommands.isPartnerLocked(getChr())) {
            if (System.currentTimeMillis() > stateEndTime) {
                dprint("TRADE_VALIDATE: partner lock timed out");
                BotTradeCommands.writeTradeChat(getChr(), "Too slow! Trade timed out.");
                BotTiming.after(2000, () -> BotTradeCommands.cancelTrade(getChr()));
                waitFor(2500); // resume after the delayed cancel lands
                cleanupTradeAndReset();
                return;
            }
            return; // Keep waiting
        }

        // Partner locked - read mesos
        int offeredMesos = BotTradeCommands.readPartnerMeso(getChr());
        dprint("TRADE_VALIDATE: partner locked, offered=" + offeredMesos);

        if (offeredMesos == MEDIUM_TIER_COST) {
            selectedTier = "medium";
        } else if (offeredMesos == ELITE_TIER_COST) {
            selectedTier = "elite";
        } else {
            // Invalid amount - reject. Deliberate synchronous beat: the flavor
            // dialogue below blocks for its YAML duration and must finish
            // before the bot resets to advertising.
            dprint("TRADE_VALIDATE: invalid meso amount, rejecting");
            BotTradeCommands.writeTradeChat(getChr(), "Wrong amount! 10m or 50m only.");
            BotHelpers.blockingSleep(2000);
            BotTradeCommands.cancelTrade(getChr());
            getDialogueHandler().executeBotFlavorDialogue("InvalidMeso", DropGameBot.this);
            cleanupTradeAndReset();
            return;
        }

        // Valid amount - confirm trade
        dprint("TRADE_VALIDATE: tier=" + selectedTier + ", confirming trade");
        BotTradeCommands.writeTradeChat(getChr(), selectedTier.toUpperCase() + " tier locked in!");
        BotTiming.after(1000, () -> BotTradeCommands.confirmTrade(getChr()));
        waitFor(3000); // confirm lands at +1s; settle ~2s after it, as before
        setDropGameState(DropGameState.TRADE_FINALIZE);
    }

    // --- TRADE FINALIZE ---
    // One tick after the delayed trade confirm: load loot pool, announce tier.
    private void tradeFinalize() {
        lootPool = DropGameLootPool.load(selectedTier);
        dprint("TRADE_FINALIZE: loot pool loaded, size=" + lootPool.size());
        if (lootPool.isEmpty()) {
            dprint("TRADE_FINALIZE: loot pool empty for tier=" + selectedTier);
            BotSpeak(getChr(), "Loot pool error. Refunding and resetting.");
            cleanupTradeAndReset();
            return;
        }

        // Announce tier
        String tierDialogue = selectedTier.equals("elite") ? "TierConfirmElite" : "TierConfirmMedium";
        getDialogueHandler().executeBotFlavorDialogue(tierDialogue, DropGameBot.this);

        cleanupTradeState();
        setDropGameState(DropGameState.PARTY_SETUP);
    }

    // --- PARTY SETUP ---
    // Bot creates its own party (if needed) and sends an invite to the player.
    // Player must accept through the normal party UI.
    private void partySetup() {
        if (!isPlayerOnMap()) {
            dprint("PARTY_SETUP: player off-map, cancelling");
            cancelAndReset("Player left the map.");
            return;
        }

        getDialogueHandler().executeBotFlavorDialogue("PartyInvite", DropGameBot.this);

        boolean invited = BotPartyCommands.botInvitePlayer(getChr(), player);
        dprint("PARTY_SETUP: botInvitePlayer -> " + invited);
        if (!invited) {
            BotSpeak(getChr(), "Couldn't send party invite. Resetting.");
            forfeitAndReset();
            return;
        }

        startTimer(PARTY_TIMEOUT_MS);
        setDropGameState(DropGameState.PARTY_WAIT);
    }

    // --- PARTY WAIT ---
    // Poll for player to accept the invite. On timeout or decline, warn + forfeit.
    private void partyWait() {
        if (!isPlayerOnMap()) {
            cancelAndReset("Player left the map.");
            return;
        }

        // Player accepted when their party matches the bot's party.
        if (getChr().getParty() != null && player.getParty() == getChr().getParty()) {
            dprint("PARTY_WAIT: player joined, advancing to PRE_GAME");
            setDropGameState(DropGameState.PRE_GAME);
            return;
        }

        // Timeout (also covers the decline case — no direct signal, player just
        // never joins).
        if (System.currentTimeMillis() > stateEndTime) {
            dprint("PARTY_WAIT: timed out waiting for player to accept");
            BotSpeak(getChr(), "Party invite timed out — mesos forfeited. Don't waste my time next round!");
            waitFor(2000); // let the line land before POST_GAME disbands + resets
            setDropGameState(DropGameState.POST_GAME);
        }
    }

    // --- PRE-GAME BUFF ---
    private void preGame() {
        if (!isPlayerOnMap()) {
            cancelAndReset("Player left the map.");
            return;
        }

        dprint("PRE_GAME: casting Haste");
        getDialogueHandler().executeBotFlavorDialogue("PreGame", DropGameBot.this);
        BotTiming.after(1000, this::castHaste);
        waitFor(3000); // haste lands at +1s, GAME_LAUNCH ticks ~2s after it
        setDropGameState(DropGameState.GAME_LAUNCH);
    }

    // Cast Haste on both bot and player
    private void castHaste() {
        try {
            Skill haste = SkillFactory.getSkill(HASTE_SKILL_ID);
            if (haste != null) {
                int maxLevel = haste.getMaxLevel();
                haste.getEffect(maxLevel).applyTo(getChr());
                haste.getEffect(maxLevel).applyTo(player);
            }
        } catch (Exception e) {
            log("DropGameBot: Failed to cast Haste: " + e.getMessage());
        }
    }

    // --- GAME LAUNCH ---
    private void gameLaunch() {
        if (!isPlayerOnMap()) {
            cancelAndReset("Player left the map.");
            return;
        }

        getDialogueHandler().executeBotFlavorDialogue("GameStart", DropGameBot.this);

        // Start the game timer
        stateStartTime = System.currentTimeMillis();
        stateEndTime = stateStartTime + GAME_DURATION_MS;

        dprint("GAME_LAUNCH: starting movement playback + drop scheduler, gameDuration=" + GAME_DURATION_MS + "ms");
        // Kick off the 2-minute movement recording off the tick thread.
        startMovementPlayback();
        // Stagger: drops begin a few seconds after movement starts so the bot
        // has moved away from the trade/party spot before items start raining.
        int initialDropDelay = DROP_INITIAL_DELAY_MIN_MS
                + random.nextInt(DROP_INITIAL_DELAY_MAX_MS - DROP_INITIAL_DELAY_MIN_MS);
        startDropScheduler(initialDropDelay);

        setDropGameState(DropGameState.ACTIVE_GAME);
    }

    // --- ACTIVE GAME ---
    private void activeGame() {
        // Check if player disconnected
        if (!isPlayerOnMap()) {
            dprint("ACTIVE_GAME: player off-map, ending early");
            stopDropScheduler();
            getDialogueHandler().executeBotFlavorDialogue("PlayerDisconnect", DropGameBot.this);
            endGameCleanup();
            return;
        }

        // Check if time's up
        if (System.currentTimeMillis() >= stateEndTime) {
            dprint("ACTIVE_GAME: time up, advancing to END_GAME");
            setDropGameState(DropGameState.END_GAME);
            return;
        }

        // Game is running — drops happen on the async scheduler.
        // The main tick just monitors for end conditions.
    }

    // --- END GAME ---
    private void endGame() {
        dprint("END_GAME: stopping scheduler, cleaning up");
        stopDropScheduler();
        getDialogueHandler().executeBotFlavorDialogue("GameEnd", DropGameBot.this);
        BotEmote(getChr(), 2);
        waitFor(3000); // wind-down beat; POST_GAME then disbands + resets
        setDropGameState(DropGameState.POST_GAME);
    }

    // =========================================================================
    // DROP SCHEDULER (async, non-blocking)
    // =========================================================================

    private void startMovementPlayback() {
        movementTask = ExecutorServiceManager.getVirtualThreadExecutorService().submit(() -> {
            try {
                MovementRecording mvr = getMovementRecording(
                        getChr().getMapId(), MOVEMENT_RECORDING_NAME);
                BotMoveStream(mvr, getChr());
            } catch (Exception e) {
                log("DropGameBot: movement playback error: " + e.getMessage());
            }
        });
    }

    private void startDropScheduler(int initialDelayMs) {
        dropsActive = true;
        BotTiming.after(initialDelayMs, this::performDrop);
    }

    private void scheduleNextDrop() {
        if (!dropsActive) {
            return;
        }
        int delay = DROP_INTERVAL_MIN_MS + random.nextInt(DROP_INTERVAL_MAX_MS - DROP_INTERVAL_MIN_MS);
        BotTiming.after(delay, this::performDrop);
    }

    private void performDrop() {
        try {
            if (!dropsActive || System.currentTimeMillis() >= stateEndTime) {
                return; // Game over, don't drop
            }

            int itemId;
            boolean isEquip = false;
            if (lootPool != null && !lootPool.isEmpty()) {
                DropGameLootPool.LootEntry entry = lootPool.rollItem();
                if (entry != null) {
                    itemId = entry.itemId;
                    isEquip = entry.isEquip;
                } else {
                    itemId = FALLBACK_DROP_ITEM_IDS[random.nextInt(FALLBACK_DROP_ITEM_IDS.length)];
                }
            } else {
                itemId = FALLBACK_DROP_ITEM_IDS[random.nextInt(FALLBACK_DROP_ITEM_IDS.length)];
            }
            dprint("DROP itemId=" + itemId + " equip=" + isEquip);
            int despawnMs = "elite".equals(selectedTier) ? ELITE_DESPAWN_DELAY_MS : MEDIUM_DESPAWN_DELAY_MS;
            DropCommands.botDropItemWithExpiry(getChr(), itemId, isEquip, despawnMs);
            DropGameSpectatorSystem.onItemDropped(getChr(), player, itemId);

            // Schedule next drop (staggered random interval for unpredictable timing)
            scheduleNextDrop();
        } catch (Exception e) {
            log("DropGameBot: Drop error: " + e.getMessage());
            scheduleNextDrop(); // Keep going even if one drop fails
        }
    }

    private void stopDropScheduler() {
        dropsActive = false; // pending BotTiming one-shots no-op on this flag
        stopMovementPlayback();
    }

    private void stopMovementPlayback() {
        if (movementTask != null && !movementTask.isCancelled() && !movementTask.isDone()) {
            movementTask.cancel(true); // interrupt stops BotMoveStream mid-recording
        }
        movementTask = null;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private boolean isTradeActive() {
        return getChr().getTrade() != null;
    }

    private boolean isPlayerOnMap() {
        if (player == null) return false;
        if (!player.isLoggedInWorld()) return false;
        return player.getMapId() == getChr().getMapId();
    }

    private void startTimer(long durationMs) {
        stateStartTime = System.currentTimeMillis();
        stateEndTime = stateStartTime + durationMs;
    }

    private void cancelAndReset(String reason) {
        BotSpeak(getChr(), reason);
        cleanupTradeAndReset();
    }

    private void cleanupTradeAndReset() {
        cleanupTradeState();
        resetDropGameState();
        setDropGameState(DropGameState.IDLE_ADVERTISE);
    }

    private void endGameCleanup() {
        // Bot is party leader → botLeaveParty disbands and auto-removes the player.
        try {
            BotPartyCommands.botLeaveParty(getChr());
        } catch (Exception e) {
            log("DropGameBot: Party cleanup error: " + e.getMessage());
        }

        resetDropGameState();
        setDropGameState(DropGameState.IDLE_ADVERTISE);
    }

    // Disbands any bot-led party and returns to IDLE without refunding mesos.
    private void forfeitAndReset() {
        if (getChr().getParty() != null) {
            try {
                BotPartyCommands.botLeaveParty(getChr());
            } catch (Exception e) {
                log("DropGameBot: forfeit party cleanup error: " + e.getMessage());
            }
        }
        resetDropGameState();
        setDropGameState(DropGameState.IDLE_ADVERTISE);
    }

    @Override
    public void displayCommands(Character chr) {
        SocialCommands.displayPlayerChatCommands(chr, hint);
    }

    @Override
    public void processMessages() {
        // DropGameBot doesn't use chat-based interaction
    }
}
