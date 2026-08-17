package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import org.gms.server.maps.ReactorDropEntry;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecording;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.server.EventMessageSystem.EventBus;
import soloMapling.server.EventMessageSystem.EventType;
import soloMapling.server.EventMessageSystem.GameEvent;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands.botLootOwnerItems;
import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.getMovementRecording;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.BotMoveStreamOffset;
import static soloMapling.DebugUtilities.debugprint;
import static soloMapling.MapVFX.CustomReactor.createReactorDropList;
import static soloMapling.MapVFX.CustomReactor.gachaPop;
import static soloMapling.ArtificialPlayer.BotDialogueHandler.getRandomDialogueLine;
import static soloMapling.BotLogger.log;
import static soloMapling.itemPool.GachaFillerSystem.createGachaListWithPrize;

public class GachaBot extends BotSM {
    private GachaBotState gachaBotState = GachaBotState.RESET;
    private List<String> hint = Collections.singletonList(getChr().getName());

    private long startTime;
    private long endTime;

    private Point basePosition;
    private Queue<String> rewardQueue; // Queue for gacha rewards to react to

    public GachaBot(Character character) {
        super(character);
        dialoguePath = "GachaBotDialogue.yaml";
        botType = "GachaBot";
    }

    private void setGachaBotState(GachaBotState state) {
        this.gachaBotState = state;
    }

    protected enum GachaBotState {
        RESET,
        SET_POSITION,
        RUN_ROULETTE,
        STAND_BY_1,
        STAND_BY_2,
        PICKUP_ITEM,
        STAND_BY_3,
        STAND_BY_4,
        PROCESS_REWARD
    }

    private void resetGachaBotState() {
        setGachaBotState(GachaBotState.RESET);

        EventBus.getInstance().subscribe(EventType.LEVEL_UP, this);
        EventBus.getInstance().subscribe(EventType.SCROLLING, this);
    }

    private void setPosition() {
        // Set the bot's main position where it will operate from
        this.basePosition = getChr().getPosition();
        SocialCommands.BotChatbubble(getChr(), "Position set!");
    }

    private void runRoulette() {
        // Check if there are main characters on the map
        if (!checkMainPlayersOnMap()) {
            return;
        }

        // Run the roulette drop animation
//        SocialCommands.BotChatbubble(getChr(), "Running roulette!");
        // todo get prize based on map
        int prize_id = 1082223; // scg
        List<ReactorDropEntry> popDrops = createReactorDropList(createGachaListWithPrize(prize_id));
        gachaPop(getChr(), popDrops);
//        BotHelpers.blockingSleep(2000); // Simulate roulette animation time
    }

    private void pickupItem() {
        // Check if there are main characters on the map
        if (!checkMainPlayersOnMap()) {
            return;
        }
//        SocialCommands.BotChatbubble(getChr(), "Running pickup!");
        SocialCommands.BotEmote(getChr(), 3);
        botLootOwnerItems(getChr(), getChr().getPosition(), 12000);
    }

    private void processReward() {
//        SocialCommands.BotChatbubble(getChr(), "process reward!");
        return;

//        // Read gachapon reward queue and do a reaction
//        if (rewardQueue != null && !rewardQueue.isEmpty()) {
//            String reward = rewardQueue.poll();
//            reactToReward(reward);
//        }
    }

    private void reactToReward(String reward) {
        // React based on the reward received
        String reaction = getRandomDialogueLine(GachaBot.this, "RewardReaction");
        if (reaction != null && !reaction.isEmpty()) {
            SocialCommands.BotSpeak(getChr(), reaction + " " + reward);
        } else {
            SocialCommands.BotSpeak(getChr(), "Wow! " + reward);
        }
    }


    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }
        getDebugger().debugLoggingFull(String.format("%s GachaBotState: %s", this.getChr().getName(), gachaBotState), String.format("%s", gachaBotState));

        switch (gachaBotState) {
            case RESET:
                resetGachaBotState();
                setGachaBotState(GachaBotState.SET_POSITION);
                break;
            case SET_POSITION:
                setPosition();
                setGachaBotState(GachaBotState.RUN_ROULETTE);
                break;
            case RUN_ROULETTE:
                runRoulette();
                setGachaBotState(GachaBotState.STAND_BY_1);
                break;
            case STAND_BY_1:
//                SocialCommands.BotChatbubble(getChr(), "Stand by 1!");
                setGachaBotState(GachaBotState.STAND_BY_2);
                break;
            case STAND_BY_2:
//                SocialCommands.BotChatbubble(getChr(), "Stand by 2!");
                setGachaBotState(GachaBotState.PICKUP_ITEM);
                break;
            case PICKUP_ITEM:
                pickupItem();
                setGachaBotState(GachaBotState.STAND_BY_3);
                break;
            case STAND_BY_3:
//                SocialCommands.BotChatbubble(getChr(), "Stand by 3!");
                executeEventQueueGacha();
//                setGachaBotState(GachaBotState.STAND_BY_4);
                break;
            case STAND_BY_4:
//                SocialCommands.BotChatbubble(getChr(), "Stand by 4!");
                setGachaBotState(GachaBotState.PROCESS_REWARD);
                break;
            case PROCESS_REWARD:
                processReward();
                setGachaBotState(GachaBotState.RUN_ROULETTE); // Loop back to state 1
                break;
            default:
                log("Unexpected state: " + gachaBotState);
                state = BotState.FINISHED;
                resetGachaBotState();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeEventQueueGacha() {
        // Process all queued events
        super.processQueuedEvents();

        // Transition logic
        if (super.hasQueuedEvents()) {
            System.out.println("Events queued. staying in STAND BY 3");
        } else {
            // Move to next appropriate state
            setGachaBotState(GachaBotState.STAND_BY_4);
        }
    }

    @Override
    public void handleEvent(GameEvent event) {
        debugprint("handleEvent GachaBot: ");
        event.printDescription();
        switch (event.getType()) {
            case GACHAPON_REWARD:
                handleGachaponEvent(event);
                break;
            case SCROLLING:
                handleScrollingEvent(event);
                break;
            case LEVEL_UP:
                handleLevelUpEvent(event);
                break;
            default:
                throw new IllegalStateException("Unknown Event Type: " + event.getType());
                // Add other cases as needed
        }
    }

    private void handleGachaponEvent(GameEvent event) {
        SocialCommands.BotEmote(getChr(), 2);
        SocialCommands.BotChatbubble(getChr(), "Lucky " + event.getPlayerName() + "!");
    }

    private void handleScrollingEvent(GameEvent event) {
        if (event.getPass()) {
            SocialCommands.BotEmote(getChr(), 3);
            SocialCommands.BotChatbubble(getChr(), "Nice scroll " + event.getPlayerName() + "!");
        } else {
            SocialCommands.BotEmote(getChr(), 4);
            SocialCommands.BotChatbubble(getChr(), "Ooof... unlucky D:");
        }
    }

    // Deliberate synchronous choreography — the recording playbacks block for
    // their own (data-driven) duration, so the celebration runs on its tick.
    private void handleLevelUpEvent(GameEvent event) {
        MovementRecording mvr = getMovementRecording(0, "rightleft45");
        BotMoveStreamOffset(mvr, getChr());
        BotHelpers.blockingSleep(1500);
        SocialCommands.BotEmote(getChr(), 2);
        SocialCommands.BotChatbubble(getChr(), "Ayy Congrats " + event.getPlayerName() + "!");
        BotHelpers.blockingSleep(1500);
        MovementRecording mvr2 = getMovementRecording(0, "leftright70");
        BotMoveStreamOffset(mvr2, getChr());
    }

}