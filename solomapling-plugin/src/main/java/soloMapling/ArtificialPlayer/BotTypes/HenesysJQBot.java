package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotDialogueHandler;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecording;
import java.awt.Point;
import soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotTypeManager;
import soloMapling.server.BotTiming;
import soloMapling.server.MethodScheduler;

import java.util.List;
import java.util.Random;

import static soloMapling.ArtificialPlayer.BotClientHandler.getBotClient;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.botWarpMapOnPortal;
import static soloMapling.ArtificialPlayer.BotDialogueHandler.getRandomDialogueLine;
import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.getMovementRecording;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.BotMoveStream;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.pathFinderAware;
import static soloMapling.BotLogger.log;
import static soloMapling.server.SoloMaplingUtilities.rollChanceInverse;

public class HenesysJQBot extends BotSM {

    private enum JQState {
        RESET,
        NAVIGATE_TO_JQ,
        ATTEMPT_JQ,
        RECOVER,
        REST,
        NAVIGATE_TO_EXIT,
        CONVERT
    }

    private static final int PET_PARK_MAP = 100000202;
    private static final int HENESYS_PARK_MAP = 100000200;
    private static final int EXIT_PORTAL_ID = 5;
    private static final int EXIT_DEST_PORTAL_ID = 13;
    private static final Point JQ_START = new Point(-1005, 274);
    private static final int NEAR_START_THRESHOLD_X = 60;
    private static final int NEAR_START_THRESHOLD_Y = 20;

    private static final String[][] TIER_RECORDINGS = {
            {"jq1fail", "jq1afail"},     // Tier 1
            {"jq2fail", "jq2afail"},     // Tier 2
            {"jq3fail", "jq3afail"},     // Tier 3
            {"jq4fail", "jq4afail"},     // Tier 4
            {"jq5fail", "jq5afail"},     // Tier 5
            {"jq6fail", "jq6afail", "jq6bfail", "jq6cfail", "jq6dfail", "jq6efail"},     // Tier 6
            {"jq7top"}                    // Tier 7
    };

    private static final int[] TIER_WEIGHTS = {20, 18, 16, 14, 12, 8, 4};

    private static final int[] EXPERIENCED_TIER_WEIGHTS = {0, 0, 0, 0, 15, 25, 60};

    private static final double CONTINUE_JQ_CHANCE = 0.75;

    private final Random random = new Random();
    private JQState jqState = JQState.RESET;
    private int highestCompletedTier = 0;
    private boolean hasReachedTop = false;
    private boolean lastAttemptSuccess = false;

    public HenesysJQBot(Character character) {
        super(character);
        dialoguePath = "JQBotDialogue.yaml";
        botType = "JQBot";
    }

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) return;
        getDebugger().debugLoggingFull(String.format("%s JQState: %s", getChr().getName(), jqState), String.format("%s", jqState));

        switch (jqState) {
            case RESET:
                waitForRandom(2000, 17000); // startup stagger so cohorts don't climb in lockstep
                jqState = JQState.NAVIGATE_TO_JQ;
                break;
            case NAVIGATE_TO_JQ:
                navigateToJQStart();
                waitForRandom(200, 700); // human beat before starting the climb
                jqState = JQState.ATTEMPT_JQ;
                break;
            case ATTEMPT_JQ:
                attemptJQ();
                jqState = JQState.RECOVER;
                break;
            case RECOVER:
                boolean nearStart = recover();
                if (nearStart) {
                    jqState = JQState.ATTEMPT_JQ;
                } else {
                    jqState = JQState.REST;
                }
                break;
            case REST:
                restAndDecide();
                break;
            case NAVIGATE_TO_EXIT:
                chatLine("LeavingJQ");
                navigateToExit();
                jqState = JQState.CONVERT;
                break;
            case CONVERT:
                convertToHenesysBot();
                break;
        }
    }

    private void navigateToJQStart() {
        if (isNearJQStart()) {
            return; // Already at starting point, no need to path there again.
        }

        try {
            pathFinderAware(getChr(), JQ_START);
        } catch (Exception e) {
            log("[HenesysJQBot] Failed to navigate to JQ start: " + e.getMessage());
        }
    }

    private void attemptJQ() {
        int selectedTier = rollNextTier();
        String[] variants = TIER_RECORDINGS[selectedTier - 1];
        String recordingName = variants[random.nextInt(variants.length)];
        lastAttemptSuccess = selectedTier == 7;

        log("[HenesysJQBot] " + getChr().getName() + " attempting " + recordingName
                + " (tier=" + selectedTier + ", highest=" + highestCompletedTier + ")");

        if (rollChanceInverse(3)) {
            MethodScheduler.runAfterDelay(() -> chatLine("AttemptStart"), 500 + random.nextInt(2000));
        }

        if (rollChanceInverse(3)) {
            scheduleMidJQChat();
        }

        try {
            MovementRecording mvr = getMovementRecording(PET_PARK_MAP, recordingName);
            BotMoveStream(mvr, getChr());
        } catch (Exception e) {
            log("[HenesysJQBot] Recording playback error: " + e.getMessage());
            return;
        }

        highestCompletedTier = selectedTier;
        waitForRandom(500, 1500); // settle beat before RECOVER ticks
    }

    private boolean recover() {
        if (lastAttemptSuccess) {
            hasReachedTop = true;
            chatLine("SuccessReaction");
            doEmote();
            int randomX = -1810 + random.nextInt(693);
            BotTiming.afterRandom(500, 1500, () -> {
                try {
                    pathFinderAware(getChr(), new Point(randomX, 274)); // break up stack after successful jq finish.
                } catch (Exception e) {
                    log("[HenesysJQBot] Failed to disperse after success: " + e.getMessage());
                }
            });
            waitFor(2000); // hold REST until the dispersal walk has kicked off
            return false;
        }

        boolean nearStart = isNearJQStart();

        if (!nearStart) {
            chatLine("FailReaction");
            if (rollChanceInverse(3)) doEmote();
            waitForRandom(1000, 3000); // sulk beat before REST ticks
        } else {
            //I am at the start, just attempt it again.
            if (rollChanceInverse(3)) {
                chatLine("FailReaction");
            }
            if (rollChanceInverse(5)) doEmote();
        }
        return nearStart;
    }

    private void restAndDecide() {
        // rest chatter plays off-tick; the decision below is invisible until the
        // next tick, which the waitFor holds to the original 1.5-4s rest total
        BotTiming.chain()
                .pauseRandom(500, 1500)
                .run(() -> {
                    if (rollChanceInverse(3)) chatLine("RestChat");
                    if (rollChanceInverse(5)) doEmote();
                })
                .start();
        waitForRandom(1500, 4000);

        if (hasReachedTop) {
            if (random.nextDouble() < CONTINUE_JQ_CHANCE) {
                log("[HenesysJQBot] " + getChr().getName() + " continuing JQ (experienced).");
                jqState = JQState.NAVIGATE_TO_JQ;
            } else {
                log("[HenesysJQBot] " + getChr().getName() + " done with JQ, converting.");
                jqState = JQState.NAVIGATE_TO_EXIT;
            }
        } else {
            jqState = JQState.NAVIGATE_TO_JQ; // todo investigate
        }
    }

    private int rollNextTier() {
        if (hasReachedTop) {
            return rollWeighted(EXPERIENCED_TIER_WEIGHTS);
        }

        int[] adjustedWeights = new int[TIER_WEIGHTS.length];
        for (int i = 0; i < TIER_WEIGHTS.length; i++) {
            adjustedWeights[i] = (i + 1 > highestCompletedTier) ? TIER_WEIGHTS[i] : 0;
        }
        return rollWeighted(adjustedWeights);
    }

    private int rollWeighted(int[] weights) {
        int totalWeight = 0;
        for (int w : weights) totalWeight += w;
        if (totalWeight == 0) return 7;

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (roll < cumulative) return i + 1;
        }
        return 7;
    }

    private boolean isNearJQStart() {
        Point pos = getChr().getPosition();
        return Math.abs(pos.x - JQ_START.x) <= NEAR_START_THRESHOLD_X
                && Math.abs(pos.y - JQ_START.y) <= NEAR_START_THRESHOLD_Y;
    }

    private void scheduleMidJQChat() {
        int chatCount = random.nextInt(3);
        for (int i = 0; i < chatCount; i++) {
            long delay = 20000 + random.nextInt(10000);
            Character chr = getChr();
            MethodScheduler.runAfterDelay(() -> {
                try {
                    BotDialogueHandler.DialogueConstructor dialog =
                            BotDialogueHandler.getDialogueCon(dialoguePath, botType, "MidJQ");
                    if (dialog != null && !dialog.getDialogue().isEmpty()) {
                        List<String> lines = dialog.getDialogue();
                        BotSpeak(chr, lines.get(random.nextInt(lines.size())));
                    }
                } catch (Exception e) {
                    // dialogue load failed, skip
                }
            }, delay);
        }
    }

    // Deliberate synchronous exit script — the walk/warp steps have data-driven
    // durations, and CONVERT must never run before the bot has left Pet Park.
    private void navigateToExit() {
        try {
            pathFinderAware(getChr(), getChr().getMap().getPortal(EXIT_PORTAL_ID).getPosition());
            BotHelpers.blockingSleep(1000 + random.nextInt(1000));
            MapleMap destMap = getBotClient().getChannelServer().getMapFactory().getMap(HENESYS_PARK_MAP);
            BotHelpers.blockingSleep(1000 + random.nextInt(1000));
            botWarpMapOnPortal(getChr(), destMap, EXIT_DEST_PORTAL_ID);
            BotHelpers.blockingSleep(1000);
            log("[HenesysJQBot] " + getChr().getName() + " exited Pet Park.");
        } catch (Exception e) {
            log("[HenesysJQBot] Failed to navigate to exit: " + e.getMessage());
        }
    }

    private void convertToHenesysBot() {
        log("[HenesysJQBot] " + getChr().getName() + " converting to HenesysBot.");
        BotTypeManager.convertBotType(getChr(), BotTypeManager.BotType.HENESYS_BOT);
        BotSM newBot = CharacterStorage.getBotById(getChr().getId());
        if (newBot instanceof HenesysBot) {
            ((HenesysBot) newBot).setLastJQConversionTime(System.currentTimeMillis());
        }
    }

    private void chatLine(String dialogueNode) {
        try {
            String line = getRandomDialogueLine(this, dialogueNode);
            if (line != null) BotSpeak(getChr(), line);
        } catch (Exception e) {
            // dialogue node missing
        }
    }

    private void doEmote() {
        int emoteId = 1 + random.nextInt(7);
        BotEmote(getChr(), emoteId);
    }

    @Override
    public void displayCommands(Character chr) {
        SocialCommands.displayPlayerChatCommands(chr, List.of(getChr().getName()));
    }

    @Override
    public void processMessages() {}
}
