package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotDialogueHandler;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.server.EventMessageSystem.EventBus;
import soloMapling.server.EventMessageSystem.EventType;
import soloMapling.server.EventMessageSystem.GameEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotChatbubble;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.BotLogger.log;
import static soloMapling.server.SoloMaplingUtilities.random;

public class GameZoneHostBot extends BotSM {
    private HostBotState hostBotState = HostBotState.RESET;
    private Boolean drinkAccepted;
    private int selectedDrink;

    private long startTime;
    private long endTime;

    // Drink item IDs - placeholder values, to be updated with actual item IDs
    private static final int DRINK_COKE = 2020031;
    private static final int DRINK_WATER = 2022000;
    private static final int DRINK_ELIXIR = 2000012;

    private static final String[] DRINK_NAMES = {"Coke", "Water", "Elixir"};
    private static final int[] DRINK_IDS = {DRINK_COKE, DRINK_WATER, DRINK_ELIXIR};

    private List<String> hint;

    public GameZoneHostBot(Character character) {
        super(character);
        dialoguePath = "GameZoneHostBotDialogue.yaml";
        botType = "GameZoneHostBot";
        hint = Collections.singletonList(character.getName());
        EventBus.getInstance().subscribe(EventType.MAP_ENTERED, this);
    }

    private enum HostBotState {
        RESET,
        IDLE_WAITING,
        DRINK_OFFER,
        WAIT_DRINK_RESPONSE,
        DRINK_SELECTION,
        WAIT_DRINK_PICK,
        SERVE_DRINK,
        FAREWELL
    }

    private void setHostBotState(HostBotState state) {
        this.hostBotState = state;
    }

    private void resetHostBotState() {
        setHostBotState(HostBotState.RESET);
        drinkAccepted = null;
        selectedDrink = 0;
        hint = Collections.singletonList(getChr().getName());
        startTime = 0;
        endTime = 0;
    }

    // --- Event-driven passive greeting (decoupled from state machine) ---

    @Override
    public void handleEvent(GameEvent event) {
        if (event.getType() == EventType.MAP_ENTERED) {
            String playerName = event.getPlayerName();
            String greetingLine = BotDialogueHandler.getRandomDialogueLine(this, "PassiveGreeting");
            greetingLine = greetingLine.replace("%PLAYER_NAME%", playerName);
            BotChatbubble(getChr(), greetingLine);
            BotEmote(getChr(), 2);
        }
    }

    // --- Main state machine ---

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }

        processQueuedEvents();

        getDebugger().debugLoggingFull(
                String.format("%s HostBotState: %s", getChr().getName(), hostBotState),
                String.format("%s", hostBotState));

        switch (hostBotState) {
            case RESET:
                resetHostBotState();
                setHostBotState(HostBotState.IDLE_WAITING);
                break;

            case IDLE_WAITING:
                idleFlavorText();
                checkForInquirer();
                break;

            case DRINK_OFFER:
                offerDrink();
                setHostBotState(HostBotState.WAIT_DRINK_RESPONSE);
                break;

            case WAIT_DRINK_RESPONSE:
                waitForResponse();
                if (drinkAccepted == null) {
                    return;
                }
                if (drinkAccepted) {
                    setHostBotState(HostBotState.DRINK_SELECTION);
                } else {
                    getDialogueHandler().executeBotDialogue("DeclineResponse", GameZoneHostBot.this);
                    setHostBotState(HostBotState.FAREWELL);
                }
                break;

            case DRINK_SELECTION:
                presentDrinkOptions();
                setHostBotState(HostBotState.WAIT_DRINK_PICK);
                break;

            case WAIT_DRINK_PICK:
                waitForResponse();
                if (selectedDrink == 0) {
                    return;
                }
                setHostBotState(HostBotState.SERVE_DRINK);
                break;

            case SERVE_DRINK:
                serveDrink();
                setHostBotState(HostBotState.FAREWELL);
                break;

            case FAREWELL:
                farewell();
                getInteractors().resetRespondant();
                setHostBotState(HostBotState.RESET);
                break;

            default:
                log("Unexpected state: " + hostBotState);
                state = BotState.FINISHED;
                resetHostBotState();
                throw new IllegalStateException("Unexpected state: " + hostBotState);
        }
    }

    // --- Player detection (inquirer promotion) ---

    private void checkForInquirer() {
        if (getInteractors().getListInquirer().isEmpty()) {
            return;
        }
        Character inquirer = getInteractors().getInquirer();
        getInteractors().removeInquirer(inquirer);
        getInteractors().setRespondant(inquirer);
        setHostBotState(HostBotState.DRINK_OFFER);
    }

    // --- Drink offer ---

    private void offerDrink() {
        getDialogueHandler().executeBotDialogue("DrinkOffer", GameZoneHostBot.this);
        hint = List.of("Yes", "No");
        displayCommands(getInteractors().getRespondant());
        startTimer(20_000);
    }

    // --- Drink selection ---

    private void presentDrinkOptions() {
        getDialogueHandler().executeBotDialogue("DrinkSelection", GameZoneHostBot.this);
        hint = List.of(DRINK_NAMES);
        displayCommands(getInteractors().getRespondant());
        startTimer(30_000);
    }

    // --- Serve drink ---

    private void serveDrink() {
        getDialogueHandler().executeBotDialogue("ServeDrink", GameZoneHostBot.this);
        DropCommands.botThrowItem(getChr(), selectedDrink, getInteractors().getRespondant().getPosition());
    }

    // --- Farewell ---

    private void farewell() {
        getDialogueHandler().executeBotFlavorDialogue("Farewell", GameZoneHostBot.this);
    }

    // --- Idle flavor text ---

    private void idleFlavorText() {
        if (random.nextInt(100) < 6) {
            getDialogueHandler().executeBotFlavorDialogue("Flavor", GameZoneHostBot.this);
        }
    }

    // --- Timer ---

    private void startTimer(long durationMs) {
        startTime = System.currentTimeMillis();
        endTime = startTime + durationMs;
    }

    // --- Response handling ---

    private void waitForResponse() {
        if (System.currentTimeMillis() < endTime) {
            processMessages();
        } else {
            BotSpeak(getChr(), "Talk to me again if you're ready.");
            state = BotState.FINISHED;
            resetHostBotState();
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
            handleMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(ChatMessage message) {
        if (!getInteractors().isMessageFromRespondant(message)) {
            return;
        }
        String content = message.getContent().toLowerCase();
        if (hostBotState == HostBotState.WAIT_DRINK_RESPONSE) {
            handleDrinkOfferResponse(content);
        } else if (hostBotState == HostBotState.WAIT_DRINK_PICK) {
            handleDrinkPickResponse(content);
        }
    }

    private void handleDrinkOfferResponse(String content) {
        if (content.contains("yes")) {
            drinkAccepted = true;
        } else if (content.contains("no")) {
            drinkAccepted = false;
        }
    }

    private void handleDrinkPickResponse(String content) {
        // Ignore stale yes/no from the previous drink offer phase
        if (content.contains("yes") || content.contains("no")) {
            return;
        }
        for (int i = 0; i < DRINK_NAMES.length; i++) {
            if (content.contains(DRINK_NAMES[i].toLowerCase())) {
                selectedDrink = DRINK_IDS[i];
                BotSpeak(getChr(), String.format("One %s, coming right up!", DRINK_NAMES[i]));
                waitFor(2000); // beat before SERVE_DRINK ticks
                return;
            }
        }
        BotSpeak(getChr(), "Hmm, I don't have that. Try again!");
        BotEmote(getChr(), 6);
    }
}
