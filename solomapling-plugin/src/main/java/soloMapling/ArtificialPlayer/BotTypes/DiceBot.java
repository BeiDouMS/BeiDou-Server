package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotLogic;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotCommands;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;
import org.gms.server.maps.MapItem;
import org.gms.server.maps.MapObject;

import soloMapling.server.MethodScheduler;

import java.awt.*;
import java.util.List;
import java.util.concurrent.*;

import static soloMapling.ArtificialPlayer.BotHelpers.adjustCenterPositionXAxis;
import static soloMapling.ArtificialPlayer.BotLogic.announceBetString;
import static soloMapling.BotLogger.log;

public class DiceBot extends BotSM {
    private DiceBotState diceBotState = DiceBotState.RESET;
    private BetType currentBet = BetType.NONE;
    int[] rolls;
    private List<MapObject> currentPot;
    long startTime;
    long endTime;
    private boolean waitingForBet;
    private boolean betsProcessed;


    public DiceBot(Character character) {
        super(character);
    }

    private enum DiceBotState {
        RESET,
        WAIT_FOR_BET,
        BET,
        ROLL,
        ROLL_PENDING,
        PROCESS_BET,
        BET_PAYOUT
    }

    private void setDiceBotState(DiceBotState state) {
        this.diceBotState = state;
    }

    private enum BetType {
        CHO,
        HAN,
        NONE;
    }

    private void setBet(BetType bet) {
        this.currentBet = bet;
    }


    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }

        getDebugger().debugLoggingFull(String.format("%s DiceBotState: %s", getChr().getName(), diceBotState), String.format("%s", diceBotState));
        switch (diceBotState) {
            case RESET:
                resetDiceBotState();
                startGame();
                setDiceBotState(DiceBotState.WAIT_FOR_BET);
                break;
            case WAIT_FOR_BET:
                waitForBet();
                break;
            case BET:
                if (detectBet()) {
                    setDiceBotState(DiceBotState.ROLL);
                } else {
                    setDiceBotState(DiceBotState.WAIT_FOR_BET);
                }
                break;
            case ROLL:
                calculateRolls();
                setDiceBotState(DiceBotState.ROLL_PENDING);
                break;
            case ROLL_PENDING:
                if (rolls != null) {
                    setDiceBotState(DiceBotState.PROCESS_BET);
                }
                break;
            case PROCESS_BET:
                calculateBet();
                setDiceBotState(DiceBotState.BET_PAYOUT);
                break;
            case BET_PAYOUT:
                if (betsProcessed) {
                    setDiceBotState(DiceBotState.RESET);
                }
                break;
            default:
                log("Unexpected state: " + diceBotState);
                state = BotState.FINISHED;
                resetDiceBotState();
                throw new IllegalStateException("Unexpected state: " + state);
        }
    }

    private void resetDiceBotState() {
        setDiceBotState(DiceBotState.RESET);
        setBet(BetType.NONE);
        currentPot = null;
        rolls = null;
        startTime = System.currentTimeMillis();
        endTime = 0;
        betsProcessed = false;
    }

    private void startGame() {
        SocialCommands.BotChatbubble(getChr(), "Please Place your bets!");
    }

    @Override
    public void displayCommands(Character chr) {
        List<String> hint = List.of("Bet Cho (even)", "Bet Han (odd)");
        SocialCommands.displayPlayerChatCommands(chr, hint);
    }

    private void waitForBet() {
        log("Waiting for bet");
        if (waitingForBet) {
            return; // Method is already running, so exit
        }

        synchronized (this) {
            if (waitingForBet) {
                return; // Double-check in case another thread just set it
            }
            waitingForBet = true;
        }

        try {
            try {
                displayCommands(getInteractors().getRespondant());
                waitForState(DiceBotState.BET, 25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("Operation was interrupted");
            }
        } finally {
            waitingForBet = false; // Reset the flag when done
        }
    }

    private boolean handleIfListIsEmpty(List<MapObject> items) {
        // Check if the list is empty
        if (items.isEmpty()) {
            SocialCommands.BotSpeak(getChr(), "No Bet detected. Please Place your bets!");
            startTime = System.currentTimeMillis(); // Give player more time to bet in case they mistype
            return true;
        }
        return false;
    }

    private void handleBetList(List<MapObject> items) {
        SocialCommands.BotSpeak(getChr(), announceBetString(getInteractors().getRespondant(), items));
        currentPot = items;
    }

    private boolean detectBet() {
        List<MapObject> items = BotLogic.readPlayersBetsStamps(getInteractors().getRespondant(), 15000);
        if (handleIfListIsEmpty(items)) {
            return false;
        } else {
            handleBetList(items);
            return true;
        }
    }


    private void calculateRolls() {
        MethodScheduler.runAfterDelay(() -> rolls = BotCommands.botDealerRollDoubleDice(this.getChr()), 3000);
    }

    private void calculateBet() {
        MethodScheduler.runAfterDelay(() -> processBet(rolls), 2000);
    }

    private boolean calculateIfPlayerWins(boolean isOdd) {
        boolean playerWins;
        if (currentBet == BetType.HAN) {
            playerWins = isOdd; // HAN means Odd
        } else if (currentBet == BetType.CHO) {
            playerWins = !isOdd; // CHO means Even
        } else {
            throw new IllegalArgumentException("Invalid bet: " + currentBet); // Handle unexpected bet values
        }
        return playerWins;
    }

    private void payoutWinnersBets() {
        for (MapObject item : currentPot) {
            MapItem mapItem = (MapItem) item;
            for (int x = 0; x < mapItem.getItem().getQuantity(); x++) {
                Point center = getInteractors().getRespondant().getPosition();
                center = adjustCenterPositionXAxis(center, x, 2, 4, 20);
                DropCommands.botThrowItem(getChr(), mapItem.getItemId(), center);
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private CompletableFuture<Void> handlePlayerWin() {
        return CompletableFuture.runAsync(() -> {
            payoutWinnersBets();
            betsProcessed = true;
        });
    }

    private void handlePlayerStealsBets() {
        getInteractors().getRespondant().setFame(getInteractors().getRespondant().getFame() - 10);
        getInteractors().getRespondant().dropMessage(5, "[Mushroom Casino] You have stolen your bets. You have been defamed by the Mushroom Casino.");
        SocialCommands.BotSpeak(getChr(), "Player has stolen back his bet. Ceasing Game.");
        state = BotState.FINISHED;
    }

    private void handlePlayerLose() {
        boolean pot_collectible = BotLogic.checkIfItemsOnFloorStill(
                currentPot, BotLogic.readPlayersBetsStamps(getInteractors().getRespondant(),
                        currentPot.getFirst().getPosition(), 75000)); // originally getRespondant().getPosition()

        if (pot_collectible) {
            DropCommands.lootItemListOnFloor(getChr(), currentPot);
        } else {
            handlePlayerStealsBets();
        }
    }

    private CompletableFuture<Void> processBet(int[] rolls) {
        int sum = rolls[0] + rolls[1];
        boolean isOdd = sum % 2 != 0;
        boolean playerWins = calculateIfPlayerWins(isOdd);
        SocialCommands.BotSpeak(getChr(), "Result: [" + rolls[0] + "] [" + rolls[1] + "] - " + (isOdd ? "Han" : "Cho")); //  // playerWins ? "Win" : "Lose"
        SocialCommands.BotEmote(getChr(), 2);

        if (playerWins) {
            handlePlayerWin();
        } else {
            handlePlayerLose();
        }
        betsProcessed = true;
        return null;
    }

    @Override
    public void processMessages() {
        try {
            ChatMessage message = MessageQueue.getInstance().getMessageWithTimeout("secondary", 1, TimeUnit.SECONDS);
            if (message == null) {
                return;
            }
            handleBetCommand(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waitForState(DiceBotState targetState, long timeoutSeconds) throws InterruptedException {
        log("Waiting for state: " + targetState);
        endTime = startTime + (timeoutSeconds * 1000);
        synchronized (this) {
            if (!diceBotState.equals(targetState) && System.currentTimeMillis() < endTime) {
                processMessages();
            } else {
                SocialCommands.BotSpeak(getChr(), "Please place your bets when you are ready!");
                state = BotState.FINISHED;
                resetDiceBotState();
            }
        }
    }

    private void handleBetCommand(ChatMessage message) {
        if (!getInteractors().isMessageFromRespondant(message)) {
            return;
        }

        String content = message.getContent().toLowerCase();
        if (!content.contains("bet")) {
            return;
        }

        if (content.contains("han")) {
            processBet(BetType.HAN, message);
        } else if (content.contains("cho")) {
            processBet(BetType.CHO, message);
        } else {
            handleInvalidBet(message);
        }
    }

    private void processBet(BetType betType, ChatMessage message) {
        setDiceBotState(DiceBotState.BET);
        //expirePlayerChatCommands(getRespondant());
        String announcement = String.format("%s bets on %s!",
                message.getSender().getName(),
                betType.name());
        SocialCommands.BotSpeak(getChr(), announcement);
        setBet(betType);
    }

    private void handleInvalidBet(ChatMessage message) {
        SocialCommands.BotSpeak(getChr(), "Please Select Han or Cho!");
        startTime = System.currentTimeMillis();
        setBet(BetType.NONE);
    }
}
