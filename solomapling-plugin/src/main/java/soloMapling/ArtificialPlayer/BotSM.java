package soloMapling.ArtificialPlayer;

import org.gms.client.Character;
import org.gms.server.Trade;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotAttackSystem.ThrowingStarSelector;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeHandler;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeInventory;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeLogic;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeSM;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeWants;
import soloMapling.ArtificialPlayer.GCMoveSystem.LodCounts;
import soloMapling.server.EventMessageSystem.BotEventBuffer;
import soloMapling.server.EventMessageSystem.EventBus;
import soloMapling.server.EventMessageSystem.EventSubscriber;
import soloMapling.server.EventMessageSystem.GameEvent;

import soloMapling.server.BotTickService;

import java.util.Collection;
import java.util.List;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.botClearChalkboard;
import static soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage.botLoggedIn;
import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.BotIdleStandingUpdate;
import static soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeLogic.clearTradeRequest;
import static soloMapling.BotLogger.log;
import static soloMapling.DebugUtilities.debugprint;
import static soloMapling.server.SoloMaplingUtilities.random;


public abstract class BotSM implements EventSubscriber {

    // BotSM - bot framework related stuff. setting/getting data

    public enum BotState {
        IDLE,
        RUNNING,
        PAUSE,
        TRADING,
        FINISHED;
    }

    private final Character character; // Reference to the existing Character object
    // Level-appropriate throwing star chosen once at creation for claw-thief bots (0 = not a claw
    // thrower). Cosmetic packet projectile only; lives here so we never touch the Cosmic Character.
    private final int chosenStarId;
    private boolean running;
    protected BotState state;
    private BotDebugHandler debugger;
    private BotInteractorsHandler interactors = new BotInteractorsHandler();
    private BotTradeHandler tradeHandler;

    protected String dialoguePath;
    protected String botType;

    protected void dprint(String msg) {
        boolean debugPrintConsole = false;
        if (!debugPrintConsole) {
            return;
        }
        debugprint("[" + getBotType() + ":" + getChr().getName() + "] " + msg);
    }

    private static MessageQueue messageQueue = MessageQueue.getInstance();

    // One shared tick body for every (re)schedule path - start / priority change / nudge.
    private final Runnable tickRunnable = () -> {
        try {
            if (isWaiting()) {
                return; // FSM-requested pause (waitFor) - skip the tick entirely
            }
            soloMapling.server.BotPerfStats.MACRO_TICKS.increment();
            updateState();
        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions to ensure the scheduler doesn't stop unexpectedly
        }
    };

    // ── Waiting without sleeping (Fable Phase 4) ─────────────────────────────
    // FSM code that needs a pause calls waitFor(ms) and RETURNS from its tick
    // instead of Thread.sleep. The tick gate above then skips updateState until
    // the wait passes - no thread is held anywhere, and the FSM resumes in
    // whatever state was set before returning.
    //
    //   old:  doThing(); blockingSleep(3000); doNext();
    //   new:  doThing(); waitFor(3000); setXxxState(NEXT); return;  // next tick runs doNext
    //
    // The wait also holds through nudges (a player walking in doesn't cut a pause
    // short - that's what the pause means), so keep waits short for anything that
    // should feel reactive.
    private volatile long waitUntilMs = 0;

    // public so collaborators driven from the tick (BotTradeSM) can pace the bot too
    public void waitFor(long ms) {
        waitUntilMs = System.currentTimeMillis() + ms;
    }

    protected void waitForRandom(long loMs, long hiMs) {
        waitFor(loMs + random.nextInt((int) Math.max(1, hiMs - loMs + 1)));
    }

    protected boolean isWaiting() {
        return System.currentTimeMillis() < waitUntilMs;
    }

    // Map-entry responsiveness (see BotMapEntryResponder): timestamp of the last nudgeSoon, used to
    // debounce repeated entries so the next tick isn't perpetually reset (which would starve the FSM).
    private volatile long lastNudgeMs = 0L;
    private static final long NUDGE_DEBOUNCE_MS = 1500;

    private BotDialogueHandler dialogueHandler;

    private BotTradeSM botTradeSM = null; // Initially null
    private BotTradeInventory tradeInventory = new BotTradeInventory();
    private BotTradeWants tradeWants = new BotTradeWants();
    private BotTradeSM.TradeMode currentTradeMode = BotTradeSM.TradeMode.NULL;
    private volatile long currentDelay = getRandomDelay(); // Store current delay
    private volatile boolean movementInterrupted = false;
    protected Trade.TradeResult lastTradeResult = null;
    protected Character lastTradedCharacter = null;

    private final BotEventBuffer eventBuffer;

    public BotSM(Character chr) {
        this.character = chr;
        this.running = false;
        this.state = BotState.IDLE;
        this.tradeHandler = new BotTradeHandler(chr);
        this.debugger = new BotDebugHandler(chr);
        this.dialogueHandler = new BotDialogueHandler(chr);
        this.eventBuffer = new BotEventBuffer(100);
        // Roll the throwing star now: the Character is fully decorated by the time a BotSM is built
        // (createBot decorates, setAndStartBots then constructs us), so weapon/level/job are set.
        this.chosenStarId = ThrowingStarSelector.selectFor(chr);
        debugprint(("Bot Initialized: " + this.character.getName() + ", " + this.character.getId()));
    }

    private void setState(BotState state) {
        this.state = state;
    }

    public BotState getState() {
        return this.state;
    }

    private boolean verifyState(BotState expectedState) {
        return this.state == expectedState;
    }

    public org.gms.client.Character getChr() {
        return this.character;
    }

    // The throwing star this claw-thief bot chose at creation, or 0 if it isn't a claw thrower.
    public int getChosenStarId() {
        return this.chosenStarId;
    }

    public String getBotType() {
        return this.botType;
    }

    public void setRunning(boolean bool) {
        this.running = bool;
    }

    public boolean getRunning() {
        return running;
    }

    public void interruptMovement() {
        this.movementInterrupted = true;
    }

    public boolean isMovementInterrupted() {
        return this.movementInterrupted;
    }

    public void clearMovementInterrupt() {
        this.movementInterrupted = false;
    }

    public BotInteractorsHandler getInteractors() {
        return interactors;
    }

    public BotTradeHandler getTradeHandler() {
        return tradeHandler;
    }

    protected BotDebugHandler getDebugger() {
        return debugger;
    }

//    public static void getMessageQueue() {
//        messageQueue = MessageQueue.getInstance();
//    }


    public boolean checkRunningOnline() {
        return getRunning() && botLoggedIn(this.getChr().getId());
    }

    // "Is a real player on my map?" - answered by the LOD observer tracker in O(1)
    // (Fable Phase 1, F3) instead of scanning the map's character list per tick.
    // Falls back to the scan only when the tracker poll isn't running yet
    // (no GC-movement bot enabled, e.g. bare dev spawns).
    public boolean checkMainPlayersOnMap() {
        if (LodCounts.trackerRunning()) {
            return LodCounts.isMapFull(character.getMapId());
        }
        Collection<Character> charsOnMap = character.getMap().getCharacters();
        for (Character chrs : charsOnMap) {
            if (!isBot(chrs)) {
                return true;
            }
        }
        return false;
    }

    public void updateState() {
        debugger.handleDebugPrints(this);
        switch (state) {
            case IDLE:
                if (checkRunningOnline()) {
                    setState(BotState.RUNNING);
                    log("Moving to RUNNING: " + getChr().getName());
                }
                break;
            case RUNNING:
                checkPrioritySpeed();
                if (!checkRunningOnline()) {
                    setState(BotState.FINISHED);
                    log("Moving to FINISHED: " + getChr().getName());
                    break;
                }
                BotIdleStandingUpdate(getChr());
//                if (!checkMainPlayersOnMap()) {
//                    state = BotState.PAUSE;
//                    log("Moving to PAUSE: " + getChr().getName());
//                    break;
//                }
                if (tradeHandler.verifyTradePartner()) {
                    debugprint("verifyTradePartner");
                    tradeInitialized(getTradeMode());
                    setState(BotState.TRADING);
                    break;
                }
                break;
            case PAUSE:
                if (checkMainPlayersOnMap()) {
                    setState(BotState.RUNNING);
                    log("Resuming to RUNNING: " + getChr().getName());
                }
                break;
            case TRADING:
                /*
                1. completed, has trade partner = should not be possible
                2. not completed, has trade partner = still trading continuously - TRADING

                3. completed, no trade partner = successfully finished trade. go to running
                4. not completed, no partner = canceled / trade declined = go to running - RUNNING
                 */
                if (botTradeSM.isTradeComplete() && !tradeHandler.verifyTradePartner() ||
                        !botTradeSM.isTradeComplete() && !tradeHandler.verifyTradePartner() && !botTradeSM.isOfferAccepted()) {
                    cleanupTradeState();
                    waitFor(2000); // settle beat after the trade closes (gated, no thread held)
                    setState(BotState.RUNNING);
                    break;
                }
                try {
                    updateTradeSM();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                break;
            case FINISHED:
                this.setRunning(false);
                getInteractors().resetRespondant();
                stopScheduledTask();
                setState(BotState.IDLE);
                break;
            default:
                throw new IllegalStateException("Unexpected state: " + state);
        }
    }

    // Method to start the scheduled task
    public synchronized void startScheduledTask() {
        startScheduledTask(0);
    }

    // Fable Phase 2: macro ticks ride the shared BotTickService wheel - no per-bot
    // scheduler thread. Registration is keep-if-present (the old code left a live
    // task alone), the period is measured from tick completion (no pileups), and
    // ticks never overlap for one bot (the wheel's per-entry guard).
    public synchronized void startScheduledTask(long initialDelayMs) {
        BotTickService.register(getChr().getId(), tickRunnable, initialDelayMs, getRandomDelay());
    }

    public synchronized void updateScheduleDelay(long newDelayMs) {
        if (this.currentDelay == newDelayMs) {
            return; // No change needed
        }
        this.currentDelay = newDelayMs;
        BotTickService.reschedule(getChr().getId(), newDelayMs);
    }

    // Pull the next macro tick forward to ~initialDelayMs from now, then resume the normal 2-6s
    // cadence. Lets a bot act promptly the instant it shares a map with a real player (a player walked
    // into the bot's map, or the bot walked into the player's map) instead of waiting out its slow
    // wheel. Debounced so a pacing player / repeated entries can't keep resetting the next tick and
    // starve the FSM. Steady state is unchanged - only the next tick moves; checkPrioritySpeed settles
    // the cadence on the following tick. Only moves this bot's due time on the shared wheel, never
    // calls updateState directly, so the no-overlapping-ticks invariant holds.
    public synchronized void nudgeSoon(long initialDelayMs) {
        if (!getRunning() || state == BotState.TRADING || state == BotState.FINISHED) {
            return; // don't disrupt a trade or a shutting-down bot
        }
        if (!BotTickService.isRegistered(getChr().getId())) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastNudgeMs < NUDGE_DEBOUNCE_MS) {
            return;
        }
        lastNudgeMs = now;
        soloMapling.server.BotPerfStats.NUDGES.increment();

        long period = getRandomDelay(); // steady state stays 2-6s; only the next tick is pulled forward
        this.currentDelay = period;
        this.cadenceObserved = true; // the nudge re-established the normal cadence; next tick re-evaluates
        BotTickService.nudge(getChr().getId(), initialDelayMs, period);
    }

    // Cadence tier tracking: reschedule the observed cadence only on a tier flip, but
    // re-assert the low cadence every unobserved tick - a reschedule is two field
    // writes on the tick wheel now, and re-asserting lets a phase-dependent low delay
    // (TrainingBot deepens to 60-120s while GRINDING) take effect one tick after the
    // bot's phase changes.
    private volatile boolean cadenceObserved = true; // startScheduledTask begins at the normal 2-6s cadence

    public void checkPrioritySpeed() {
        boolean observed = checkMainPlayersOnMap();
        if (observed) {
            if (!cadenceObserved) {
                cadenceObserved = true;
                setPriorityNormal();
            }
            return;
        }
        cadenceObserved = false;
        setPriorityLow();
    }

    // Unobserved macro cadence for this bot type. Jittered so cohorts that flipped to
    // low together don't stay tick-aligned forever (Fable Phase 3: lockstep clumps CPU
    // into bursts and aliases short !env perf windows). Deep-background types override
    // this (TrainingBot returns 60-120s while GRINDING - abstract EXP accrues by
    // elapsed time and the grind watchdog skips unobserved bots, so nothing is lost).
    protected long lowPriorityDelayMs() {
        return 9000 + random.nextInt(3000); // 9-12s
    }

    // Convenience methods for common adjustments
    public void setPriorityLow() {
        updateScheduleDelay(lowPriorityDelayMs());
    }

    public void setPriorityHigh() {
        updateScheduleDelay(2000); // 2 seconds
    }

    public void setPriorityNormal() {
        updateScheduleDelay(getRandomDelay()); // Your original random delay
    }

    private long getRandomDelay() {
        return 2000 + random.nextInt(4000); // 2000 to 3000 ms
    }

    // Method to stop the scheduled task. An in-flight tick is allowed to finish
    // (the old cancel(true) interrupt is gone); the wheel entry is simply removed.
    public synchronized void stopScheduledTask() {
        log("Shutting down scheduler: " + this.getChr().getName());
        EventBus.getInstance().unsubscribeAll(this);
        botClearChalkboard(this.getChr());
        BotTickService.unregister(getChr().getId());
    }

    // todo
    // At the moment this is not used as far as I know.
    protected void processMessages() {
        System.out.println("BotSM processMessages");
        try {
            ChatMessage message = messageQueue.getMessageNonBlocking("secondary");
            if (message.getSender() == getInteractors().getRespondant()) {
                log("This Message is from Respondant: " + getInteractors().getRespondant().getName() + ", Msg: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<MapObject> detectItems() {
        return null;
    }

    protected boolean checkIfNotRunningOrPaused() {
        if (!this.getRunning()) {
            return true;
        }
        if (verifyState(BotState.PAUSE)) {
            return true;
        }
        return false;
    }

    public void displayCommands(Character chr) {
        List<String> hint = List.of(getChr().getName());
        SocialCommands.talkCygnusGuideCommands(chr, hint);
    }

    //

    protected void checkForTrades() {
        boolean acceptedTrade = BotTradeLogic.checkTradeQueue(getChr());
        if (acceptedTrade) {
            tradeHandler.setTradePartner(tradeHandler.getTradePartnerRaw());
            debugprint("Accepted Trade");
        }
    }

    public void setTradeMode(BotTradeSM.TradeMode tradeMode) {
        this.currentTradeMode = tradeMode;
    }

    protected BotTradeSM.TradeMode getTradeMode() {
        return this.currentTradeMode;
    }

    protected void tradeInitialized(BotTradeSM.TradeMode tradeMode) {
        startTradeSM(tradeMode);
    }

    protected void startTradeSM() {
        if (botTradeSM == null) {
            botTradeSM = new BotTradeSM(this); // Create only when entering TRADING
        }
    }

    protected void startTradeSM(BotTradeSM.TradeMode mode) {
        botTradeSM = new BotTradeSM(this, mode);
    }

    protected void updateTradeSM() {
        botTradeSM.update(); // Continue trading logic
    }

    protected void discardTradeSM() {
        botTradeSM = null;
    }

    protected void cleanupTradeState() {
        botTradeSM = null;
        clearTradeRequest(getChr());
        tradeHandler.resetTradePartner();
        discardTradeSM();
    }

    public BotDialogueHandler getDialogueHandler() {
        return dialogueHandler;
    }

    public BotTradeInventory getTradeInventory() {
        return tradeInventory;
    }

    public BotTradeWants getTradeWants() {
        return tradeWants;
    }

    public void resetLastTradeResult() {
        lastTradeResult = null;
    }

    public void setLastTradeResult(Trade.TradeResult result) {
        lastTradeResult = result;
    }

    public Trade.TradeResult getLastTradeResult() {
        return lastTradeResult;
    }

    public void setLastTradedCharacter(Character character) {
        lastTradedCharacter = character;
    }

    public Character getLastTradedCharacter() {
        return lastTradedCharacter;
    }

    public void resetLastTradedCharacter() {
        lastTradedCharacter = null;
    }

    @Override
    public void onEvent(GameEvent event) {
        // Queue the event for processing later
        eventBuffer.add(event);
    }

    @Override
    public boolean matchesFilter(GameEvent event) {
        // Check if this event is relevant to this bot
        int targetWorld = getChr().getWorld();
        int targetChannel = getChr().getMap().getChannelServer().getId();
        MapleMap targetMap = getChr().getMap();
        if (event.getWorld() != targetWorld) {
            return false;
        }
        if (event.getChannel() != targetChannel) {
            return false;
        }
        if (targetMap != null && event.getMap() != targetMap) {
            return false;
        }
        return true;
    }

    // Call this from your event processing state
    public void processQueuedEvents() {
        GameEvent event = eventBuffer.poll();
        if (event != null) {
            handleEvent(event);
        }
    }

    public void handleEvent(GameEvent event) {
        // Process based on event type
        System.out.println("BotSM handleEvent");
        return;
    }

    public boolean isAvailableForAmbientActions() {
        return false;
    }

    public boolean hasQueuedEvents() {
        return !eventBuffer.isEmpty();
    }

}
