package soloMapling.ArtificialPlayer.BotTypes.OPQ;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.Reactor;
import soloMapling.ArtificialPlayer.BotCommandsPack.BotAttack;
import soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotLogic;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands;
import soloMapling.ArtificialPlayer.BotPartySystem.BotPartyLogic;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotTypes.OPQ.OPQSharedContext.OPQPhase;
import soloMapling.Environment.EnvironmentManager;
import soloMapling.Environment.PlatformPlacement;
import soloMapling.MapVFX.CustomReactor;
import soloMapling.server.BotTiming;

import java.awt.Point;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static soloMapling.ArtificialPlayer.BotClientHandler.getBotClient;
import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.botWarpMapOnPortal;
import static soloMapling.ArtificialPlayer.BotGeneration.warpBotToLocation;
import static soloMapling.ArtificialPlayer.BotHelpers.blockingSleep;
import static soloMapling.ArtificialPlayer.BotTypes.OPQ.OPQConstants.STAGE_1_COMPLETE_TP;
import static soloMapling.ArtificialPlayer.BotTypes.OPQ.OPQConstants.STAGE_1_ENTRY_TP;
import static soloMapling.BotLogger.log;
import static soloMapling.DebugUtilities.debugprint;

/**
 * Orbis Party Quest rush-bot state machine.
 * <p>
 * Design:
 * - Perception-driven. The bot decides transitions from its own observable
 * state (current mapId, position, inventory) plus orchestrator blackboard
 * reads. It never accepts commands from other bots.
 * - Map change is authoritative for major phase swaps (lobby -> stage 1 ->
 * tower -> stage 2 -> exit lobby). When the game teleports the party, the
 * mapId flips and the bot rehomes itself via {@link #detectPhaseFromMap()}.
 * - Per-stage work is a short loop: navigate -> hit target -> loot -> return
 * -> drop -> wait. The wait state flips to TRANSITION once the orchestrator
 * (or a map change) signals the stage is done.
 * - All waits are time-boxed against {@link OPQConstants#STAGE_WAIT_TIMEOUT_MS}
 * so a stuck bot eventually falls back to LOOP_CHECK instead of hanging.
 */
public class OPQBot extends BotSM {

    // volatile: the swing chains run off-tick and read/write these
    private volatile OPQBotState opqBotState = OPQBotState.RESET;
    private final OPQOrchestrator orchestrator;
    private final OPQSharedContext sharedContext;
    private List<String> hint = Collections.singletonList(getChr().getName());

    // Per-state timers / scratch fields
    private long stageWaitStartTime;
    private long lastRecruitMessageAt;
    private volatile int reactorHitsThisTarget;

    private int cloudPiecesLooted;
    private volatile int lootedRecordItemId = -1;

    public OPQBot(Character character) {
        super(character);
        dialoguePath = "OPQBotDialogue.yaml"; // TODO: add YAML or fall back gracefully
        botType = "OPQBot";
        this.orchestrator = OPQOrchestrator.getInstance();
        this.sharedContext = orchestrator.getSharedContext();
        orchestrator.registerBot(this);
    }

    // =========================================================================
    // State machine plumbing
    // =========================================================================

    private void setOPQBotState(OPQBotState state) {
        this.opqBotState = state;
    }

    /**
     * Dev-only setter used by the !opq forcestate command.
     */
    public void setStateForDebug(OPQBotState state) {
        OPQBotState prev = this.opqBotState;
        this.opqBotState = state;
        log(String.format("[OPQBot %s] DEBUG forced %s -> %s",
                getChr().getName(), prev, state));
    }

    /**
     * Read the current top-level state (for dev tooling / dump command).
     */
    public OPQBotState getOPQBotState() {
        return opqBotState;
    }

    /**
     * Logged state transition. Use this instead of setOPQBotState(...) for any
     * transition you want to see in BotLog.txt. The reason argument is the
     * single most useful field for tracing why a bot moved — write it as a
     * short clause: "arrived at platform m3", "stage1Complete flag flipped",
     * "wait timed out", etc.
     */
    private void transitionTo(OPQBotState next, String reason) {
        OPQBotState prev = this.opqBotState;
        if (prev == next) {
            return;
        }
        this.opqBotState = next;
        log(String.format("[OPQBot %s] %s -> %s | %s | map=%d pos=%s",
                getChr().getName(), prev, next, reason,
                getChr().getMapId(), getChr().getPosition()));
        debugprint("[OPQBot]", getChr().getName(), prev, "->", next, "|", reason);
    }

    public enum OPQBotState {
        RESET,
        RECRUITMENT,
        IN_PARTY_IDLE,
        STAGE_1_NAVIGATE,
        STAGE_1_HIT_REACTOR,
        STAGE_1_LOOT,
        STAGE_1_RETURN,
        STAGE_1_DROP_ITEMS,
        STAGE_1_WAIT,
        STAGE_1_TRANSITION,
        STAGE_1_TRANSITION_PT_2,
        STAGE_2_NAVIGATE,
        STAGE_2_HIT_BOX,
        STAGE_2_LOOT,
        STAGE_2_RETURN,
        STAGE_2_DROP_ITEMS,
        STAGE_2_WAIT,
        EXIT_DETECT,
        EXIT_LOBBY,
        LOOP_CHECK
    }

    private void resetOPQBotState() {
        setOPQBotState(OPQBotState.RESET);
        hint = Collections.singletonList(getChr().getName());
        stageWaitStartTime = 0;
        lastRecruitMessageAt = 0;
        reactorHitsThisTarget = 0;
        cloudPiecesLooted = 0;
        lootedRecordItemId = -1;
    }

    // =========================================================================
    // Main tick
    // =========================================================================

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }
        getDebugger().debugLoggingFull(
                String.format("%s OPQBotState: %s", this.getChr().getName(), opqBotState),
                String.format("%s", opqBotState));

        if (isInsidePQ() && !isInParty()) {
            handlePQAbandoned();
            return;
        }

        // Authoritative re-home: if the game teleported us to a map we weren't
        // expecting, snap to the correct phase entry state.
        OPQBotState mapDerived = detectPhaseFromMap();
        if (mapDerived != null && mapDerived != opqBotState && !inSameStageFamily(mapDerived, opqBotState)) {
            transitionTo(mapDerived,
                    "map-derived rehome (mapId=" + getChr().getMapId() + ")");
        }

        switch (opqBotState) {
            case RESET:
                resetOPQBotState();
                transitionTo(OPQBotState.RECRUITMENT, "RESET completed, beginning recruitment");
                break;
            case RECRUITMENT:
                handleRecruitment();
                break;
            case IN_PARTY_IDLE:
                handleInPartyIdle();
                break;
            case STAGE_1_NAVIGATE:
                handleStage1Navigate();
                break;
            case STAGE_1_HIT_REACTOR:
                hitReactor4Times();
                break;
            case STAGE_1_LOOT:
                handleStage1Loot();
                break;
            case STAGE_1_RETURN:
                handleStage1Return();
                break;
            case STAGE_1_DROP_ITEMS:
                handleStage1DropItems();
                break;
            case STAGE_1_WAIT:
                handleStage1Wait();
                break;
            case STAGE_1_TRANSITION:
                handleStage1Transition();
                break;
            case STAGE_1_TRANSITION_PT_2:
                handleStage1TransitionPart2();
                break;
            case STAGE_2_NAVIGATE:
                handleStage2Navigate();
                break;
            case STAGE_2_HIT_BOX:
                hitBox4Times();
                break;
            case STAGE_2_LOOT:
                handleStage2Loot();
                break;
            case STAGE_2_RETURN:
                handleStage2Return();
                break;
            case STAGE_2_DROP_ITEMS:
                handleStage2DropItems();
                break;
            case STAGE_2_WAIT:
                handleStage2Wait();
                break;
            case EXIT_DETECT:
                handleExitDetect();
                break;
            case EXIT_LOBBY:
                handleExitLobby();
                break;
            case LOOP_CHECK:
                handleLoopCheck();
                break;
            default:
                log("Unexpected state: " + opqBotState);
                state = BotState.FINISHED;
                resetOPQBotState();
                throw new IllegalStateException("Unexpected state: " + state);
        }
    }

    @Override
    public void displayCommands(Character chr) {
        SocialCommands.displayPlayerChatCommands(chr, hint);
    }

    @Override
    public void processMessages() {
        try {
            ChatMessage message = MessageQueue.getInstance().getMessageWithTimeout("secondary", 1, TimeUnit.SECONDS);
            if (message == null) {
                return;
            }
            // OPQ bots don't need to react to player chat during a run, but the
            // hook is here for future extensions (e.g. leader shouting "go").
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // Phase: Recruitment (lobby)
    // =========================================================================

    private void handleRecruitment() {
        debugLogf("handleRecruitment: mapId=" + getChr().getMapId()
                + " inParty=" + isInParty()
                + " sinceLastChat=" + (System.currentTimeMillis() - lastRecruitMessageAt) + "ms");

        // Auto-accept any pending party invite first; if accepted we'll flip
        // to IN_PARTY_IDLE on the next tick via the isInParty() check below.
        boolean accepted = BotPartyLogic.checkPartyQueue(getChr());
        if (accepted) {
            debugLogf("Accepted a pending party invite.");
        }

        if (isInParty()) {
            orchestrator.noteLeaderFromBot(this);
            sharedContext_trySetPhase(OPQPhase.IN_PARTY_IDLE);
            transitionTo(OPQBotState.IN_PARTY_IDLE, "joined a party");
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastRecruitMessageAt >= OPQConstants.RECRUIT_MESSAGE_INTERVAL_MS) {
            String msg = OPQRecruitMessages.generateRecruitMessage(getChr());
            SocialCommands.BotSpeak(getChr(), msg);
            lastRecruitMessageAt = now;
            debugLogf("Recruit chat sent: \"" + msg + "\"");

            List<String> platforms = PlatformPlacement.getMainPlatformIds(getChr().getMapId());
            if (!platforms.isEmpty()) {
                String target = platforms.get(new Random().nextInt(platforms.size()));
                PlatformPlacement.botMoveToPlatformAnyUnoccupiedSpot(getChr(), target);
            }
        }
    }

    // =========================================================================
    // Phase: In party, pre-start idle
    // =========================================================================

    private void handleInPartyIdle() {
        debugLogf("handleInPartyIdle: mapId=" + getChr().getMapId()
                + " inParty=" + isInParty()
                + " phase=" + sharedContext.getCurrentPhase());

        if (!isInParty()) {
            // Disbanded before PQ started — back to lobby chat.
            transitionTo(OPQBotState.RECRUITMENT, "party disbanded before PQ started");
            return;
        }

        // Refresh leader id every tick — covers leader change / re-invite.
//        orchestrator.noteLeaderFromBot(this);
        // The orchestrator's tick polls leader.getMapId() and warps us into
        // Stage 1 server-side once the leader enters. detectPhaseFromMap() at
        // the top of updateState() then flips us into STAGE_1_NAVIGATE.

        if (getPartyLeader().getMapId() == OPQConstants.OPQ_STAGE_1) {
            cloudPiecesLooted = 0;
            lootedRecordItemId = -1;
            // deliberate synchronous warp: warpBotToLocation blocks through the
            // arrival choreography (up to ~7s) and nothing may overlap it
            blockingSleep(3000);
            OPQOrchestrator.getInstance().followLeaderWarp(getChr(), STAGE_1_ENTRY_TP);
            transitionTo(OPQBotState.STAGE_1_NAVIGATE, "Warp to Stage 1 with Leader");
        }
    }

    // =========================================================================
    // Phase: Stage 1
    // =========================================================================

    // Stage 1 has 20 cloud reactors; one cycle per bot is not enough.
    // If any unbroken clouds remain (and not already claimed by another
    // bot), drop our now-dead assignment and loop back to NAVIGATE so
    // assignCloudReactor picks a fresh closest one. Only when no clouds
    // are left do we mark task complete and wait.

    private void handleStage1Navigate() {
        sharedContext_trySetPhase(OPQPhase.STAGE_1);

        Integer reactorOid = orchestrator.assignCloudReactor(this);
        debugLogf("handleStage1Navigate: reactorOid=" + reactorOid
                + "Char pos=" + getChr().getPosition());


        boolean moreCloudsLeft = orchestrator.hasUnclaimedLiveCloudReactor(
                getChr().getMap(), getChr().getId());
        if (!moreCloudsLeft) {
            sharedContext.putCloudAssignment(getChr().getId(), null);
            transitionTo(OPQBotState.STAGE_1_RETURN,
                    "no clouds left -> going return state");
            return;
        }
//        if (reactorOid == null) {
//            // No live reactor available — every cloud is taken or already broken.
//            startStageWaitTimer();
//            transitionTo(OPQBotState.STAGE_1_WAIT, "no cloud reactor available");
//            return;
//        }

        Reactor reactor = getChr().getMap().getReactorByOid(reactorOid);
        if (reactor == null || !reactor.isAlive() || reactor.getState() >= 4) {
            // Stale assignment — clear and retry next tick.
            sharedContext.putCloudAssignment(getChr().getId(), null);
            debugLogf("Reactor oid=" + reactorOid + " is stale (null/dead/state>=4) — re-requesting next tick");
            return;
        }

        // ensures its in range to hit it, could potentially loop if not in range based on reactor hit range px
        Point reactorPos = reactor.getPosition();
        double dx = Math.abs(getChr().getPosition().getX() - reactorPos.getX());
        if (dx <= OPQConstants.REACTOR_HIT_RANGE_PX) {
            reactorHitsThisTarget = 0;
            transitionTo(OPQBotState.STAGE_1_HIT_REACTOR,
                    "arrived within range of reactor oid=" + reactorOid + " (dx=" + dx + "px)");
            return;
        }
        MovementCommands.pathFinderBetaAerial(getChr(), reactorPos);
        waitFor(OPQConstants.NAVIGATE_SETTLE_MS); // let the walk land; range check re-runs next tick
        debugLogf("Stage1Navigate walking: dx=" + dx + " target=" + reactorPos);
    }

    private void hitReactor4Times() {
        // swings play out on a chain; the gate kills leftover swings once a hit
        // transitions us to LOOT, and waitFor holds ticks until the chain is done
        BotTiming.Chain chain = BotTiming.chain()
                .stopUnless(() -> opqBotState == OPQBotState.STAGE_1_HIT_REACTOR);
        for (int x = 0; x < 4; x++) {
            chain.run(this::handleStage1HitReactor).pause(OPQConstants.SWING_INTERVAL_MS);
        }
        chain.start();
        waitFor(OPQConstants.SWING_INTERVAL_MS * 4 + 200);
    }

    private void handleStage1HitReactor() {
        Integer reactorOid = sharedContext.getMyCloudAssignment(getChr().getId());
        if (reactorOid == null) {
            // Lost our assignment somehow — go back to navigate to re-request.
            transitionTo(OPQBotState.STAGE_1_NAVIGATE, "no reactor assignment in HIT state");
            return;
        }
        Reactor reactor = getChr().getMap().getReactorByOid(reactorOid);
        if (reactor == null || !reactor.isAlive() || reactor.getState() >= 4) {
            debugLogf("Reactor oid=" + reactorOid + " already dead — skipping to LOOT");
            transitionTo(OPQBotState.STAGE_1_LOOT, "reactor already broken on arrival");
            return;
        }

        debugLogf("handleStage1HitReactor: hit#" + (reactorHitsThisTarget + 1)
                + "/" + OPQConstants.MAX_REACTOR_HITS
                + " reactorOid=" + reactorOid + " state=" + reactor.getState());

        // Defensive: re-check state immediately before swinging. The top-of-tick
        // check above can go stale if a real player hit the reactor in the same
        // tick window — overshooting state 4 crashes the map.
        if (reactor.getState() >= 4) {
            debugLogf("Pre-hit guard tripped: state=" + reactor.getState() + " — skipping to LOOT");
            transitionTo(OPQBotState.STAGE_1_LOOT, "pre-hit state guard");
            return;
        }

        BotAttack.basicSwing(getChr());
        CustomReactor.hitReactor(getChr().getMap(), reactorOid);
        reactorHitsThisTarget++;

        // Bot-owned drop: clientless bots don't trigger the normal reactor drop
        // pipeline, so we manually spawn the cloud piece if our hit was the one
        // that finalized the break (state == 4 immediately after the hit).
        byte stateAfter = reactor.getState();
        if (stateAfter >= 4) {
            CustomReactor.dropItemAtReactor(getChr().getMap(), reactorOid,
                    OPQConstants.CLOUD_PIECE, getChr());
            debugLogf("Forced cloud drop after finalizing break: oid=" + reactorOid);
        }

        if (stateAfter >= 4 || reactorHitsThisTarget >= OPQConstants.MAX_REACTOR_HITS) {
            transitionTo(OPQBotState.STAGE_1_LOOT,
                    "reactor broken (state=" + stateAfter + ", hits=" + reactorHitsThisTarget + ")");
        }
    }

    private void handleStage1Loot() {
        Point botPos = getChr().getPosition();
        Integer reactorOid = sharedContext.getMyCloudAssignment(getChr().getId());
        Reactor reactor = (reactorOid != null) ? getChr().getMap().getReactorByOid(reactorOid) : null;
        Point reactorPos = (reactor != null) ? reactor.getPosition() : null;
        int[] cloudFilter = {OPQConstants.CLOUD_PIECE};

        // 1) Primary scan: at the bot's feet (where the just-broken reactor stood).
        List<MapObject> found = BotLogic.checkForItemsOnFloor(
                getChr(), botPos, OPQConstants.STAGE_1_LOOT_SCAN_RANGE_PX, cloudFilter);
        debugLogf("handleStage1Loot: primary @" + botPos + " hits=" + found.size());

        // 2) Fallback: cloud may have fallen through one or more footholds
        //    below the reactor anchor. Step straight down from the reactor's
        //    x in fixed increments and stop on the first hit.
        if (found.isEmpty() && reactorPos != null) {
            for (int step = 1; step <= OPQConstants.STAGE_1_LOOT_FALLBACK_STEPS; step++) {
                Point probe = new Point(
                        reactorPos.x,
                        reactorPos.y + step * OPQConstants.STAGE_1_LOOT_FALLBACK_STEP_PX);
                List<MapObject> hits = BotLogic.checkForItemsOnFloor(
                        getChr(), probe, OPQConstants.STAGE_1_LOOT_SCAN_RANGE_PX, cloudFilter);
                debugLogf("handleStage1Loot: fallback step " + step
                        + " @" + probe + " hits=" + hits.size());
                if (!hits.isEmpty()) {
                    found = hits;
                    break;
                }
            }
        }

        if (!found.isEmpty()) {
            DropCommands.lootItemListOnFloor(getChr(), found);
            cloudPiecesLooted += 1;
            SocialCommands.BotChatbubble(getChr(), "Cloud Pieces: " + cloudPiecesLooted);
        }
        waitFor(800); // loot beat before NAVIGATE ticks

        debugLogf("handleStage1Loot: scannedHits=" + found.size()
                + " cloudPiecesLooted=" + cloudPiecesLooted);

        transitionTo(OPQBotState.STAGE_1_NAVIGATE,
                "loot pass complete, searching for next cloud reactor");
    }

    private void handleStage1Return() {
        MovementCommands.pathFinderBeta(getChr(), new Point(497, 143));
        waitFor(OPQConstants.NAVIGATE_SETTLE_MS); // settle before DROP_ITEMS ticks
        transitionTo(OPQBotState.STAGE_1_DROP_ITEMS, "return state done.");
    }

    private void handleStage1DropItems() {
        int cloudCount = cloudPiecesLooted;
        debugLogf("handleStage1DropItems: cloudCount=" + cloudCount + " pos=" + getChr().getPosition());
        if (cloudCount > 0) {
            SocialCommands.BotSpeak(getChr(), "Dropping " + cloudCount + " cloud" + (cloudCount == 1 ? "" : "s") + "!");
            BotTiming.after(400, () ->
                    DropCommands.botThrowItemQty(getChr(), OPQConstants.CLOUD_PIECE, cloudCount, getChr().getPosition()));
            waitFor(800); // hold WAIT until the throw lands
        }

        sharedContext.markTaskComplete(getChr().getId());
        startStageWaitTimer();
        transitionTo(OPQBotState.STAGE_1_WAIT,
                "all cloud reactors broken, waiting for stage-1 clear");
    }

    private void handleStage1Wait() {
        if (sharedContext.isStage1Complete() && orchestrator.isChamberlainSpawned()) {
            OPQOrchestrator.getInstance().followLeaderWarp(getChr(), STAGE_1_COMPLETE_TP);
            // Walk to Portal
            MovementCommands.moveToPortal(getChr(), 4);
            transitionTo(OPQBotState.STAGE_1_TRANSITION, "stage1Complete flag flipped by orchestrator");
            return;
        }
    }

    private void handleStage1Transition() {
        debugLogf("handleStage1Transition: mapId=" + getChr().getMapId()
                + " awaiting teleport to central tower (" + OPQConstants.OPQ_TOWER + ")");

        // I'm not sure how to have the bots enter the proper portal which is to get them to the map of OPQ_TOWER
        // Because technically its a PQ instance, so it's gotta be the correct MapleMap, not just generic map.
        if (getPartyLeader().getMapId() == OPQConstants.OPQ_TOWER) {
            // deliberate synchronous warp sequence (blocking arrival choreography)
            blockingSleep(1000);
            OPQOrchestrator.getInstance().followLeaderWarp(getChr(), new Point(-260,-32)); // Spawn point for OPQ tower [x=-260,y=-32]
            blockingSleep(1000);
            MovementCommands.pathFinderBeta(getChr(), new Point(159, -32)); // Walk to Portal [x=159,y=-32]
            transitionTo(OPQBotState.STAGE_1_TRANSITION_PT_2, "Waiting for leader to enter stage 2");
        }
    }

    private void handleStage1TransitionPart2() {
        // Teleport from Tower to Stage 2
        if (getPartyLeader().getMapId() == OPQConstants.OPQ_STAGE_2) {
            OPQOrchestrator.getInstance().followLeaderWarp(getChr(), new Point(-113,-321)); // Spawn point for stage 2 [x=-113,y=-321]
            waitFor(1000); // settle after the warp before NAVIGATE ticks
            transitionTo(OPQBotState.STAGE_2_NAVIGATE, "arrived in stage-2 map");
        }
    }

    // =========================================================================
    // Phase: Stage 2
    // =========================================================================

    private void handleStage2Navigate() {
        sharedContext_trySetPhase(OPQPhase.STAGE_2);

        if (leaderLeftStage2()) {
            sharedContext.putBoxAssignment(getChr().getId(), null);
            followLeaderOut();
            return;
        }

        // Debug: dump all reactors on the map on first entry
        debugLogf("handleStage2Navigate: dumping all reactors on map " + getChr().getMapId());
//        for (Reactor r : getChr().getMap().getAllReactors()) {
//            debugLogf("  reactor oid=" + r.getObjectId()
//                    + " dataId=" + r.getId()
//                    + " state=" + r.getState()
//                    + " alive=" + r.isAlive()
//                    + " pos=" + r.getPosition());
//        }

        Integer reactorOid = orchestrator.assignBoxReactor(this);
        debugLogf("handleStage2Navigate: reactorOid=" + reactorOid
                + " pos=" + getChr().getPosition());

        boolean moreBoxesLeft = orchestrator.hasUnclaimedLiveBoxReactor(
                getChr().getMap(), getChr().getId());
        if (!moreBoxesLeft && reactorOid == null) {
            // All boxes broken or claimed — if we have an item, go drop it first
            if (lootedRecordItemId > 0) {
                transitionTo(OPQBotState.STAGE_2_RETURN,
                        "no boxes left, returning to drop looted item");
            } else {
                sharedContext.markTaskComplete(getChr().getId());
                startStageWaitTimer();
                transitionTo(OPQBotState.STAGE_2_WAIT,
                        "no boxes left and nothing to drop — waiting for stage clear");
            }
            return;
        }

        if (reactorOid == null) {
            debugLogf("Stage2Navigate: no assignment yet, waiting for one to free up");
            return;
        }

        Reactor reactor = getChr().getMap().getReactorByOid(reactorOid);
        if (reactor == null || !reactor.isAlive() || reactor.getState() >= 4) {
            sharedContext.putBoxAssignment(getChr().getId(), null);
            debugLogf("Box reactor oid=" + reactorOid + " is stale (null/dead/state>=4) — re-requesting next tick");
            return;
        }

        // Chat which box we're going for (ordinal based on sorted position right-to-left)
        String ordinal = orchestrator.getBoxOrdinal(reactorOid);
        SocialCommands.BotSpeak(getChr(), "I'll get the " + ordinal + " box!");

        Point reactorPos = reactor.getPosition();
        double dx = Math.abs(getChr().getPosition().getX() - reactorPos.getX());
        if (dx <= OPQConstants.REACTOR_HIT_RANGE_PX) {
            reactorHitsThisTarget = 0;
            transitionTo(OPQBotState.STAGE_2_HIT_BOX,
                    "arrived at " + ordinal + " box (oid=" + reactorOid + ")");
            return;
        }
        MovementCommands.pathFinderBetaAerial(getChr(), reactorPos);
        waitFor(OPQConstants.NAVIGATE_SETTLE_MS); // let the walk land; range check re-runs next tick
        debugLogf("Stage2Navigate walking: dx=" + dx + " target=" + reactorPos);
    }

    private void hitBox4Times() {
        // Face toward the box reactor before swinging
        Integer reactorOid = sharedContext.getMyBoxAssignment(getChr().getId());
        if (reactorOid != null) {
            Reactor reactor = getChr().getMap().getReactorByOid(reactorOid);
            if (reactor != null) {
                boolean boxIsLeft = reactor.getPosition().x < getChr().getPosition().x;
                if (boxIsLeft && !MovementCommands.facingLeft(getChr())) {
                    MovementCommands.microTurnAroundToLeft(getChr());
                } else if (!boxIsLeft && MovementCommands.facingLeft(getChr())) {
                    MovementCommands.microTurnAroundToRight(getChr());
                }
            }
        }

        BotTiming.Chain chain = BotTiming.chain()
                .stopUnless(() -> opqBotState == OPQBotState.STAGE_2_HIT_BOX);
        for (int x = 0; x < 4; x++) {
            chain.run(this::handleStage2HitBox).pause(OPQConstants.SWING_INTERVAL_MS);
        }
        chain.start();
        waitFor(OPQConstants.SWING_INTERVAL_MS * 4 + 200);
    }

    private void handleStage2HitBox() {
        Integer reactorOid = sharedContext.getMyBoxAssignment(getChr().getId());
        if (reactorOid == null) {
            transitionTo(OPQBotState.STAGE_2_NAVIGATE, "no box assignment in HIT state");
            return;
        }
        Reactor reactor = getChr().getMap().getReactorByOid(reactorOid);
        if (reactor == null || !reactor.isAlive() || reactor.getState() >= 4) {
            debugLogf("Box reactor oid=" + reactorOid + " already broken — skipping to LOOT");
            transitionTo(OPQBotState.STAGE_2_LOOT, "box already broken on arrival");
            return;
        }

        debugLogf("handleStage2HitBox: hit#" + (reactorHitsThisTarget + 1)
                + "/" + OPQConstants.MAX_REACTOR_HITS
                + " reactorOid=" + reactorOid + " state=" + reactor.getState());

        if (reactor.getState() >= 4) {
            debugLogf("Pre-hit guard tripped: state=" + reactor.getState() + " — skipping to LOOT");
            transitionTo(OPQBotState.STAGE_2_LOOT, "pre-hit state guard");
            return;
        }

        BotAttack.basicSwing(getChr());
        CustomReactor.hitReactor(getChr().getMap(), reactorOid);
        reactorHitsThisTarget++;

        byte stateAfter = reactor.getState();
        if (stateAfter >= 4) {
            int recordItem = orchestrator.getBoxItemId(reactorOid);
            CustomReactor.dropItemAtReactor(getChr().getMap(), reactorOid,
                    recordItem, getChr());
            lootedRecordItemId = recordItem;
            debugLogf("Forced record drop (itemId=" + recordItem + ") after finalizing box break: oid=" + reactorOid);
        }

        if (stateAfter >= 4 || reactorHitsThisTarget >= OPQConstants.MAX_REACTOR_HITS) {
            transitionTo(OPQBotState.STAGE_2_LOOT,
                    "box broken (state=" + stateAfter + ", hits=" + reactorHitsThisTarget + ")");
        }
    }

    private void handleStage2Loot() {
        Point botPos = getChr().getPosition();
        Integer reactorOid = sharedContext.getMyBoxAssignment(getChr().getId());
        Reactor reactor = (reactorOid != null) ? getChr().getMap().getReactorByOid(reactorOid) : null;
        Point scanCenter = (reactor != null) ? reactor.getPosition() : botPos;

        // Loot the item off the floor for game state consistency
        int[] recordFilter = OPQConstants.STAGE_2_ITEMS.stream().mapToInt(Integer::intValue).toArray();
        List<MapObject> found = BotLogic.checkForItemsOnFloor(
                getChr(), scanCenter, OPQConstants.STAGE_1_LOOT_SCAN_RANGE_PX, recordFilter);
        if (!found.isEmpty()) {
            DropCommands.lootItemListOnFloor(getChr(), found);
        }

        debugLogf("handleStage2Loot: lootedRecordItemId=" + lootedRecordItemId
                + " floorHits=" + found.size());
        if (lootedRecordItemId > 0) {
            SocialCommands.BotChatbubble(getChr(), "Got a record!");
        }
        waitFor(800); // loot beat before RETURN ticks

        // Clear box assignment so we can pick a new one
        sharedContext.putBoxAssignment(getChr().getId(), null);

        transitionTo(OPQBotState.STAGE_2_RETURN, "loot pass complete, returning to music box");
    }

    private void handleStage2Return() {
        MovementCommands.pathFinderBeta(getChr(), new Point(-1588, -127));
        waitFor(OPQConstants.NAVIGATE_SETTLE_MS); // settle before DROP_ITEMS ticks
        transitionTo(OPQBotState.STAGE_2_DROP_ITEMS,
                "arrived at music box drop zone");
    }

    private void handleStage2DropItems() {
        if (lootedRecordItemId > 0) {
            SocialCommands.BotSpeak(getChr(), "Dropping my record!");
            int recordId = lootedRecordItemId;
            BotTiming.after(400, () ->
                    DropCommands.botThrowItem(getChr(), recordId, getChr().getPosition()));
            debugLogf("handleStage2DropItems: dropping itemId=" + recordId);
            lootedRecordItemId = -1;
        } else {
            debugLogf("handleStage2DropItems: no record to drop (lootedId=" + lootedRecordItemId + ")");
        }
        waitFor(1000); // replaces the old 400+600ms drop beats

        // If leader already cleared and left, don't bother with remaining boxes
        if (leaderLeftStage2()) {
            followLeaderOut();
            return;
        }

        // Check if more boxes remain — loop back if so
        boolean moreBoxes = orchestrator.hasUnclaimedLiveBoxReactor(
                getChr().getMap(), getChr().getId());
        if (moreBoxes) {
            transitionTo(OPQBotState.STAGE_2_NAVIGATE,
                    "more boxes remaining, looping back for another");
        } else {
            sharedContext.markTaskComplete(getChr().getId());
            startStageWaitTimer();
            transitionTo(OPQBotState.STAGE_2_WAIT,
                    "all boxes broken, waiting for stage-2 clear");
        }
    }

    private void handleStage2Wait() {
        long waitedMs = stageWaitStartTime > 0
                ? (System.currentTimeMillis() - stageWaitStartTime) : 0;
        debugLogf("handleStage2Wait: waited=" + waitedMs + "ms"
                + " stage2Complete=" + sharedContext.isStage2Complete());

        // Detect leader leaving Stage 2 (via NPC exit or fast-exit through lobby)
        int leaderMap = getPartyLeader().getMapId();
        if (leaderMap == OPQConstants.OPQ_EXIT_LOBBY) {
            blockingSleep(1000); // deliberate: blocking follow-warp below
            OPQOrchestrator.getInstance().followLeaderWarp(getChr(), new Point(-161, 323));
            transitionTo(OPQBotState.EXIT_LOBBY, "followed leader to exit lobby");
            return;
        }
        if (leaderMap == OPQConstants.OPQ_LOBBY) {
            blockingSleep(1000); // deliberate: blocking follow-warp below
            OPQOrchestrator.getInstance().followLeaderWarp(getChr(), new Point(-233, 174));
            transitionTo(OPQBotState.LOOP_CHECK, "leader already in OPQ lobby");
            return;
        }

        if (sharedContext.isStage2Complete()) {
            transitionTo(OPQBotState.EXIT_DETECT,
                    "stage2Complete flag flipped by orchestrator");
            return;
        }

        if (waitTimedOut()) {
            transitionTo(OPQBotState.LOOP_CHECK,
                    "stage-2 wait timed out after "
                            + OPQConstants.STAGE_WAIT_TIMEOUT_MS + "ms");
        }
    }

    // =========================================================================
    // Phase: Exit
    // =========================================================================

    private void handleExitDetect() {
        debugLogf("handleExitDetect: mapId=" + getChr().getMapId()
                + " leaderMap=" + getPartyLeader().getMapId());
        sharedContext_trySetPhase(OPQPhase.EXIT);

        if (getChr().getMapId() == OPQConstants.OPQ_EXIT_LOBBY) {
            transitionTo(OPQBotState.EXIT_LOBBY, "arrived at exit lobby");
            return;
        }

        // Actively follow leader to exit lobby or recruitment lobby
        int leaderMap = getPartyLeader().getMapId();
        if (leaderMap == OPQConstants.OPQ_EXIT_LOBBY) {
            blockingSleep(1000); // deliberate: blocking follow-warp below
            OPQOrchestrator.getInstance().followLeaderWarp(getChr(), new Point(-161, 323));
            transitionTo(OPQBotState.EXIT_LOBBY, "followed leader to exit lobby");
        } else if (leaderMap == OPQConstants.OPQ_LOBBY) {
            blockingSleep(1000); // deliberate: blocking follow-warp below
            OPQOrchestrator.getInstance().followLeaderWarp(getChr(), new Point(-233, 174));
            transitionTo(OPQBotState.LOOP_CHECK, "followed leader to OPQ lobby");
        }
    }

    private void handleExitLobby() {
        debugLogf("handleExitLobby: mapId=" + getChr().getMapId()
                + " leaderMap=" + getPartyLeader().getMapId());

        // If leader has already moved to recruitment lobby, follow them
        // (deliberate synchronous warps: arrival choreography blocks and must not overlap)
        int leaderMap = getPartyLeader().getMapId();
        if (leaderMap == OPQConstants.OPQ_LOBBY) {
            blockingSleep(300);
            MapleMap lobbyMap = getBotClient().getChannelServer().getMapFactory().getMap(OPQConstants.OPQ_LOBBY);
            warpBotToLocation(getChr(), new Point(-233, 174), lobbyMap);
            transitionTo(OPQBotState.LOOP_CHECK, "followed leader to recruitment lobby");
            return;
        }

        // Otherwise warp ourselves to lobby after a short wait
        blockingSleep(500);
        MapleMap lobbyMap = getBotClient().getChannelServer().getMapFactory().getMap(OPQConstants.OPQ_LOBBY);
        warpBotToLocation(getChr(), new Point(-233, 174), lobbyMap);
        blockingSleep(2000);
        List<String> platforms = PlatformPlacement.getMainPlatformIds(getChr().getMapId());
        if (!platforms.isEmpty()) {
            String target = platforms.get(new Random().nextInt(platforms.size()));
            PlatformPlacement.botMoveToPlatformAnyUnoccupiedSpot(getChr(), target);
        }

        transitionTo(OPQBotState.LOOP_CHECK, "exit-lobby complete, warped to recruitment lobby");
    }

    private void handleLoopCheck() {
        debugLogf("handleLoopCheck: inParty=" + isInParty()
                + " mapId=" + getChr().getMapId());
        // Clear ALL per-run scratch so next run starts completely clean.
        sharedContext.clearTaskComplete(getChr().getId());
        sharedContext.putCloudAssignment(getChr().getId(), null);
        sharedContext.putBoxAssignment(getChr().getId(), null);
        sharedContext.putPlatformAssignment(getChr().getId(), null);
        reactorHitsThisTarget = 0;
        stageWaitStartTime = 0;
        lootedRecordItemId = -1;
        cloudPiecesLooted = 0;

        if (isInParty()) {
            transitionTo(OPQBotState.IN_PARTY_IDLE,
                    "still partied, ready for next PQ run");
        } else {
            transitionTo(OPQBotState.RECRUITMENT,
                    "no party, returning to lobby chat");
        }
    }

    // =========================================================================
    // PQ abandonment
    // =========================================================================

    private boolean isInsidePQ() {
        return switch (opqBotState) {
            case RESET, RECRUITMENT, IN_PARTY_IDLE, LOOP_CHECK -> false;
            default -> true;
        };
    }

    private void handlePQAbandoned() {
        debugLogf("Party lost — PQ abandoned. Cleaning up and warping out.");

        // Clear this bot's shared context entries
        int botId = getChr().getId();
        sharedContext.putCloudAssignment(botId, null);
        sharedContext.putBoxAssignment(botId, null);
        sharedContext.putPlatformAssignment(botId, null);
        sharedContext.clearTaskComplete(botId);

        // deliberate synchronous warps (blocking arrival choreography)
        MapleMap exitMap = getBotClient().getChannelServer().getMapFactory().getMap(OPQConstants.OPQ_EXIT_LOBBY);
        warpBotToLocation(getChr(), new Point(-161, 323), exitMap);
        blockingSleep(1500);

        MapleMap lobbyMap = getBotClient().getChannelServer().getMapFactory().getMap(OPQConstants.OPQ_LOBBY);
        warpBotToLocation(getChr(), new Point(-233, 174), lobbyMap);

        resetOPQBotState();
        transitionTo(OPQBotState.RECRUITMENT, "party lost — PQ abandoned, returned to lobby");
    }

    // =========================================================================
    // Perception helpers
    // =========================================================================

    /**
     * Translate the current mapId into the state the bot should be in, if any.
     */
    private OPQBotState detectPhaseFromMap() {
        int mapId = getChr().getMapId();
        if (mapId == OPQConstants.OPQ_LOBBY) {
            return isInParty() ? OPQBotState.IN_PARTY_IDLE : OPQBotState.RECRUITMENT;
        }
        if (mapId == OPQConstants.OPQ_STAGE_1) return OPQBotState.STAGE_1_NAVIGATE;
        if (mapId == OPQConstants.OPQ_TOWER) return OPQBotState.STAGE_1_TRANSITION;
        if (mapId == OPQConstants.OPQ_STAGE_2) return OPQBotState.STAGE_2_NAVIGATE;
        if (mapId == OPQConstants.OPQ_EXIT_LOBBY) return OPQBotState.EXIT_DETECT;
        return null;
    }

    /**
     * Avoid re-homing mid-stage: if we're already in a STAGE_1_* state and the
     * map-derived state is also a STAGE_1_* state, don't rewind us to NAVIGATE.
     */
    private boolean inSameStageFamily(OPQBotState a, OPQBotState b) {
        return family(a) != null && family(a).equals(family(b));
    }

    private String family(OPQBotState s) {
        if (s == null) return null;
        String n = s.name();
        if (n.startsWith("STAGE_1")) return "S1";
        if (n.startsWith("STAGE_2")) return "S2";
        if (n.startsWith("EXIT")) return "EX";
        if (n.equals("RECRUITMENT") || n.equals("IN_PARTY_IDLE")) return "LOBBY";
        return null;
    }

    private boolean isInParty() {
        return getChr().getParty() != null;
    }

    private Character getPartyLeader() {
        return getChr().getParty().getLeader().getPlayer();
    }

    private boolean leaderLeftStage2() {
        int leaderMap = getPartyLeader().getMapId();
        return leaderMap == OPQConstants.OPQ_EXIT_LOBBY || leaderMap == OPQConstants.OPQ_LOBBY;
    }

    private void followLeaderOut() {
        int leaderMap = getPartyLeader().getMapId();
        blockingSleep(1000); // deliberate: blocking follow-warps below
        if (leaderMap == OPQConstants.OPQ_EXIT_LOBBY) {
            OPQOrchestrator.getInstance().followLeaderWarp(getChr(), new Point(-161, 323));
            transitionTo(OPQBotState.EXIT_LOBBY, "leader already left stage 2 — following to exit lobby");
        } else {
            OPQOrchestrator.getInstance().followLeaderWarp(getChr(), new Point(-233, 174));
            transitionTo(OPQBotState.LOOP_CHECK, "leader already in OPQ lobby — skipping exit");
        }
    }

    private void startStageWaitTimer() {
        stageWaitStartTime = System.currentTimeMillis();
    }

    private boolean waitTimedOut() {
        return stageWaitStartTime > 0
                && (System.currentTimeMillis() - stageWaitStartTime) > OPQConstants.STAGE_WAIT_TIMEOUT_MS;
    }

    private void sharedContext_trySetPhase(OPQPhase phase) {
        // Bots observe their own mapId and ask the orchestrator to mirror the
        // derived phase into the blackboard. This keeps OPQSharedContext's
        // writers package-private while letting subscribers read a single
        // source of truth instead of polling mapIds themselves.
        if (sharedContext.getCurrentPhase() != phase) {
            orchestrator.mirrorPhase(phase);
        }
    }

    private void debugLogf(String msg) {
        boolean opqDebug = false;
        if(!opqDebug) {
            return;
        }
        String line = "[OPQBot " + getChr().getName() + " " + opqBotState + "] " + msg;
        log(line);
        debugprint(line);
    }
}
