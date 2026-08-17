package soloMapling.ArtificialPlayer.BotTypes.Blackjack;

import org.gms.client.Character;
import org.gms.server.maps.MapItem;
import org.gms.server.maps.MapObject;
import soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotCustomization;
import soloMapling.ArtificialPlayer.BotDialogueHandler;
import soloMapling.ArtificialPlayer.BotLogic;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.server.ExecutorServiceManager;
import soloMapling.server.MethodScheduler;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static soloMapling.ArtificialPlayer.BotHelpers.adjustCenterPositionXAxis;
import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.ArtificialPlayer.BotHelpers.blockingSleep;
import static soloMapling.ArtificialPlayer.BotLogic.announceBetString;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botFaceTowardsPoint;
import static soloMapling.BotLogger.log;
import static soloMapling.server.SoloMaplingUtilities.random;

public class BlackjackDealerBot extends BotSM {

    private final BlackjackTable table;

    // Timing
    private long startTime;
    private long bettingDeadline;
    private boolean finalDecision;

    // Track last phase so we only log transitions, not every tick
    private BlackjackTable.Phase lastLoggedPhase = null;

    // Prevent double-issuing payouts while we wait for async drop loops to finish
    private boolean payoutsIssued = false;

    // Stamps accepted as bets (same as DiceBot / legacy)
    private static final int[] BET_STAMPS = {4002000, 4002001, 4002002, 4002003, 4031558, 4031559, 4031560, 4031561};

    // Artificial player bet item
    private static final int AI_BET_ITEM = 4031560; // Dark Lord stamp
    private static final int AI_BET_QTY = 5;

    private static final int[] DEALER_OUTFIT = {1042129, 1060001, 1072010, 1702099};

    public BlackjackDealerBot(Character character) {
        super(character);
        this.table = new BlackjackTable(character);
        this.botType = "BLACKJACK_DEALER";
        this.dialoguePath = "BlackjackDealerBotDialogue.yaml";
        for (int itemId : DEALER_OUTFIT) {
            BotCustomization.EquipBot(character, itemId);
        }
    }

    public BlackjackTable getTable() {
        return table;
    }

    // --- Tick rate override: 1-2 seconds for snappy Blackjack pacing ---

    @Override
    public synchronized void startScheduledTask() {
        super.startScheduledTask();
        updateScheduleDelay(2000); // 2s base tick
    }

    // --- Main state machine ---

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }

        getDebugger().debugLoggingFull(String.format("%s BlackjackPhase: %s", getChr().getName(), table.getPhase()), String.format("%s", table.getPhase()));
        boolean firstTickOfPhase = (lastLoggedPhase != table.getPhase());
        if (firstTickOfPhase) {
            dprint("PHASE " + lastLoggedPhase + " -> " + table.getPhase()
                    + " (players=" + table.getPlayerCount()
                    + ", currentIdx=" + table.getCurrentPlayerIndex() + ")");
            lastLoggedPhase = table.getPhase();
        }
        processJoinInquiries();
        switch (table.getPhase()) {
            case WAITING:
                handleWaiting(firstTickOfPhase);
                break;
            case BETTING:
                handleBetting();
                break;
            case PROCESSING_BETS:
                handleProcessingBets();
                break;
            case DEALING:
                handleDealing();
                break;
            case PLAYER_TURNS:
                handlePlayerTurn();
                break;
            case PLAYER_RESPONSE:
                handlePlayerResponse();
                break;
            case DEALER_TURN:
                handleDealerTurn();
                break;
            case RESOLVING:
                handleResolving();
                break;
            case PAYOUT:
                handlePayout();
                break;
            default:
                log("Unexpected blackjack phase: " + table.getPhase());
                state = BotState.FINISHED;
                break;
        }
    }

    // --- Phase handlers ---

    private void handleWaiting(boolean firstTick) {
        if (firstTick) {
            table.resetForNewRound();
            if (!table.hasEnoughPlayers()) {
                SocialCommands.BotChatbubble(getChr(), "Waiting for Players to join table..");
            }
        }
        if (table.hasEnoughPlayers()) {
            dprint("WAITING: enough players (" + table.getPlayerCount() + "), -> BETTING");
            bettingDeadline = 0;
            table.setPhase(BlackjackTable.Phase.BETTING);
        }
    }

    private void handleBetting() {
        if (bettingDeadline == 0) {
            dprint("BETTING: requesting bets from " + (table.getPlayerCount() - 1) + " player(s)");
            SocialCommands.BotChatbubble(getChr(), "Please Place your Bets!");
            triggerArtificialPlayerBets();
            bettingDeadline = System.currentTimeMillis() + 10_000;
            return;
        }
        if (System.currentTimeMillis() >= bettingDeadline) {
            bettingDeadline = 0;
            table.setPhase(BlackjackTable.Phase.PROCESSING_BETS);
        }
    }

    private void handleProcessingBets() {
        table.incrementWaitCount();
        processAllPlayersBets();
        dprint("PROCESSING_BETS: waitCount=" + table.getWaitCount()
                + ", playerCount=" + table.getPlayerCount()
                + ", hasAnyBet=" + table.hasAtLeastOneBet());

        if (table.getPlayerCount() <= 1) {
            dprint("PROCESSING_BETS: no non-dealer players left -> WAITING");
            table.setPhase(BlackjackTable.Phase.WAITING);
            return;
        }

        if (table.hasAtLeastOneBet() && table.getWaitCount() > 1) {
            SocialCommands.BotChatbubble(getChr(), "All bets are closed. Let's Begin.");
            table.setPhase(BlackjackTable.Phase.DEALING);
        } else {
            table.setPhase(BlackjackTable.Phase.BETTING);
        }
    }

    private void handleDealing() {
        dprint("DEALING: dealing initial cards");
        dealCardsToAllPlayers();
        SocialCommands.BotSpeak(getChr(), "My card: " + table.getDealer().getHandValue() + ".");
        table.setPhase(BlackjackTable.Phase.PLAYER_TURNS);
    }

    private void handlePlayerTurn() {
        startTime = System.currentTimeMillis();
        finalDecision = false;
        BlackjackPlayer currentPlayer = table.getCurrentPlayer();
        if (currentPlayer == null) {
            table.incrementToNextPlayer();
            return;
        }

        String playerName = currentPlayer.getName();
        int handValue = currentPlayer.getHandValue();

        dprint("PLAYER_TURNS: idx=" + table.getCurrentPlayerIndex()
                + ", name=" + playerName + ", handValue=" + handValue
                + ", hand=" + currentPlayer.getHand() + ", isBot=" + isBot(currentPlayer.getCharacter()));

        if (handValue == 0) {
            dprint("PLAYER_TURNS: " + playerName + " had no hand, skipping");
            table.incrementToNextPlayer();
            if (table.isCurrentPlayerDealer()) {
                table.setPhase(BlackjackTable.Phase.DEALER_TURN);
            }
            return;
        }

        if (handValue > 21) {
            handleBust(currentPlayer, playerName, handValue);
            return;
        }

        if (handValue == 21) {
            handleAutoStand(currentPlayer, playerName);
            return;
        }

        botFaceTowardsPoint(getChr(), currentPlayer.getCharacter().getPosition());
        SocialCommands.BotSpeak(getChr(), playerName + ": " + handValue + ". What will you do?");
        if (!isBot(currentPlayer.getCharacter())) {
            showPlayerActionHint(currentPlayer.getCharacter());
        }
        triggerArtificialPlayerResponse(currentPlayer);
        table.setPhase(BlackjackTable.Phase.PLAYER_RESPONSE);
    }

    private void handleBust(BlackjackPlayer player, String name, int handValue) {
        dprint("BUST: " + name + " handValue=" + handValue + " hand=" + player.getHand());
        player.setResponseStatus("RESPONDED");
        player.setStatus(BlackjackPlayer.PlayerStatus.BUST);
        SocialCommands.BotSpeak(getChr(), name + ": " + handValue + " Too Many.");
        triggerAIPlayerReaction(player, "PlayerBust");

        Character kicked = lootLoserBets(player);
        DropCommands.lootItemListOnFloor(getChr(), player.getCardsOnMap());
        player.clearCardsOnMap();

        if (kicked != null) {
            // removePlayer shifts currentPlayerIndex so it already points to the next player
            kickPlayer(kicked);
        } else {
            table.incrementToNextPlayer();
        }
        if (table.isCurrentPlayerDealer()) {
            table.setPhase(BlackjackTable.Phase.DEALER_TURN);
        } else {
            table.setPhase(BlackjackTable.Phase.PLAYER_TURNS);
        }
    }

    private void handleAutoStand(BlackjackPlayer player, String name) {
        dprint("AUTO_STAND (21): " + name + " hand=" + player.getHand());
        player.setResponseStatus("RESPONDED");
        player.setStatus(BlackjackPlayer.PlayerStatus.STAND);
        SocialCommands.BotSpeak(getChr(), name + ": 21!");

        table.incrementToNextPlayer();
        if (table.isCurrentPlayerDealer()) {
            table.setPhase(BlackjackTable.Phase.DEALER_TURN);
        } else {
            table.setPhase(BlackjackTable.Phase.PLAYER_TURNS);
        }
    }

    private void handlePlayerResponse() {
        BlackjackPlayer currentPlayer = table.getCurrentPlayer();
        if (currentPlayer == null) {
            table.setPhase(BlackjackTable.Phase.DEALER_TURN);
            return;
        }

        if (!currentPlayer.hasResponded()) {
            waitForResponse(currentPlayer);
        }

        if (!currentPlayer.hasResponded()) {
            // Still waiting
            return;
        }

        processPlayerAction(currentPlayer);
    }

    private void processPlayerAction(BlackjackPlayer player) {
        dprint("ACTION: " + player.getName() + " status=" + player.getStatus()
                + " (handValue=" + player.getHandValue() + ")");
        switch (player.getStatus()) {
            case ACTIVE: // HIT
                dealCardToPlayer(player);
                player.setResponseStatus("WAITING");
                player.setStatus(BlackjackPlayer.PlayerStatus.WAITING);
                table.setPhase(BlackjackTable.Phase.PLAYER_TURNS);
                break;
            case STAND:
            case BUST:
                table.incrementToNextPlayer();
                player.setResponseStatus("WAITING");
                if (table.isCurrentPlayerDealer()) {
                    table.setPhase(BlackjackTable.Phase.DEALER_TURN);
                } else {
                    table.setPhase(BlackjackTable.Phase.PLAYER_TURNS);
                }
                break;
            default:
                log("Unknown player status: " + player.getStatus());
                break;
        }
    }

    // Deliberate synchronous choreography: the hit/stand loop is rule-driven
    // (each card changes the next decision), so the dealing rhythm blocks this
    // bot's own tick - a chain can't express it and nothing else is held up.
    private void handleDealerTurn() {
        SocialCommands.BotSpeak(getChr(), "My Turn.");
        BlackjackPlayer dealer = table.getDealer();

        int handValue = dealer.getHandValue();
        dprint("DEALER_TURN: starting hand=" + dealer.getHand() + " value=" + handValue);

        while (BlackjackRules.shouldDealerHit(handValue)) {
            SocialCommands.BotSpeak(getChr(), handValue + ".");
            dealCardToPlayer(dealer);
            handValue = dealer.getHandValue();
            blockingSleep(500);
        }

        SocialCommands.BotSpeak(getChr(), handValue + ".");
        if (handValue > 21) {
            SocialCommands.BotSpeak(getChr(), "Too many. I Bust.");
            triggerDealerReaction("DealerLoss");
        } else if (handValue == 21) {
            SocialCommands.BotSpeak(getChr(), "21 for me :P");
            SocialCommands.BotEmote(getChr(), 3);
        }

        dprint("DEALER_TURN: final hand=" + dealer.getHand() + " value=" + handValue);
        table.setPhase(BlackjackTable.Phase.RESOLVING);
    }

    private void handleResolving() {
        dprint("RESOLVING: dealerHand=" + table.getDealer().getHand()
                + " dealerValue=" + table.getDealer().getHandValue());
        table.resolveAllOutcomes();

        // Announce all outcomes on a single line so they don't overlap.
        List<String> dealerHand = table.getDealer().getHand();
        List<String> lines = new ArrayList<>();
        for (int i = 1; i < table.getPlayerCount(); i++) {
            BlackjackPlayer player = table.getPlayer(i);
            if (player.getHand().isEmpty()) continue;

            BlackjackRules.Outcome outcome = BlackjackRules.determineOutcome(player.getHand(), dealerHand);
            dprint("RESOLVING: " + player.getName() + " hand=" + player.getHand()
                    + " value=" + player.getHandValue() + " -> " + outcome);
            lines.add(BlackjackRules.formatOutcomeMessage(player.getName(), outcome, player.getHand(), dealerHand));
        }
        if (!lines.isEmpty()) {
            SocialCommands.BotSpeak(getChr(), String.join(" | ", lines));
        }

        boolean dealerBusted = table.getDealer().getHandValue() > 21;
        boolean anyPlayerWon = false;
        for (int i = 1; i < table.getPlayerCount(); i++) {
            BlackjackPlayer player = table.getPlayer(i);
            if (player.getHand().isEmpty()) continue;
            BlackjackRules.Outcome outcome = BlackjackRules.determineOutcome(player.getHand(), dealerHand);
            if (outcome == BlackjackRules.Outcome.WIN || outcome == BlackjackRules.Outcome.BLACKJACK_WIN) {
                anyPlayerWon = true;
            }
            String dialogueNode = outcomeToDialogueNode(outcome);
            if (dialogueNode != null) {
                triggerAIPlayerReaction(player, dialogueNode);
            }
        }
        // DealerLoss on normal player win; bust case already fires in handleDealerTurn
        if (anyPlayerWon && !dealerBusted) {
            triggerDealerReaction("DealerLoss");
        }

        table.setPhase(BlackjackTable.Phase.PAYOUT);
    }

    private void handlePayout() {
        // First tick in PAYOUT: kick off all payouts staggered per player.
        if (!payoutsIssued) {
            dprint("PAYOUT: issuing payouts for " + (table.getPlayerCount() - 1) + " player(s)");
            List<Character> toKick = new ArrayList<>();
            int staggerDelay = 0;
            for (int i = 1; i < table.getPlayerCount(); i++) {
                BlackjackPlayer player = table.getPlayer(i);
                if (player.getResult() == BlackjackPlayer.HandResult.PENDING) continue;
                if (!player.isBetsProcessed()) {
                    dprint("PAYOUT: " + player.getName() + " result=" + player.getResult());
                    if (player.getResult() == BlackjackPlayer.HandResult.WIN
                            || player.getResult() == BlackjackPlayer.HandResult.BLACKJACK_WIN) {
                        int delay = staggerDelay;
                        boolean blackjackBonus = player.getResult() == BlackjackPlayer.HandResult.BLACKJACK_WIN;
                        MethodScheduler.runAfterDelay(() -> payWinner(player, blackjackBonus), delay);
                        staggerDelay += 2000 + random.nextInt(1500);
                    } else {
                        Character kicked = payoutOrCollect(player);
                        if (kicked != null) toKick.add(kicked);
                    }
                }
            }
            for (Character chr : toKick) {
                kickPlayer(chr);
            }
            payoutsIssued = true;
            return;
        }

        // Subsequent ticks: wait for every async winner drop to finish before moving on.
        // Only check players who actually had hands this round (non-empty hand = participated).
        for (int i = 1; i < table.getPlayerCount(); i++) {
            BlackjackPlayer player = table.getPlayer(i);
            if (player.getResult() == BlackjackPlayer.HandResult.PENDING) continue;
            if (!player.isBetsProcessed()) {
                dprint("PAYOUT: waiting on " + player.getName());
                return;
            }
        }

        // All payouts settled — now safe to clean up cards and start the next round.
        cleanupAllCards();
        SocialCommands.BotChatbubble(getChr(), "Let's go again.");
        payoutsIssued = false;
        table.setPhase(BlackjackTable.Phase.WAITING);
    }

    private void kickPlayer(Character chr) {
        table.removePlayer(chr);
        getInteractors().removeRespondant(chr);
    }

    // --- Card dealing ---

    // Deliberate synchronous choreography: per-card dealing rhythm on the tick,
    // sequence depends on live table state (bets, kicks) between cards.
    private void dealCardsToAllPlayers() {
        int size = table.getPlayerCount();
        // Deal 2 cards to everyone. Dealer gets 1 face-up card only (no face-down card for now).
        // Dealer's second card is dealt during dealer turn from the deck.
        for (int round = 0; round < 2; round++) {
            for (int i = 0; i < size; i++) {
                int seqIndex = BlackjackTable.getDealSequenceIndex(i, size);
                BlackjackPlayer player = table.getPlayer(seqIndex);

                // Skip players without bets (except dealer)
                if (seqIndex != 0 && !player.hasBets()) {
                    continue;
                }

                // Dealer only gets 1 card during initial deal
                if (seqIndex == 0 && round == 1) {
                    continue;
                }

                dealCardToPlayer(player);
                blockingSleep(500);
            }
        }
    }

    private void dealCardToPlayer(BlackjackPlayer player) {
        String drawnCard = table.drawCard();
        Point pos = calculateDealPosition(player);
        int cardItemId = CardVisuals.getCardItemId(drawnCard);
        dprint("DEAL card=" + drawnCard + " (itemId=" + cardItemId + ") -> "
                + player.getName() + " at " + pos);

        MapItem cardOnMap = DropCommands.botThrowItemNoExpireOwnerOnly(getChr(), cardItemId, pos);
        table.dealCardToPlayer(player, drawnCard);
        if (cardOnMap != null) {
            player.addCardOnMap(cardOnMap);
        }
    }

    private Point calculateDealPosition(BlackjackPlayer player) {
        Point origin = player.getOriginLocation();
        Point target = new Point(origin);
        int cardIndex = player.getHand().size();
        target.x += (cardIndex + 1) * 35;
        return target;
    }

    // --- Bet detection (floor scanning) ---

    private void processAllPlayersBets() {
        List<Character> toKick = new ArrayList<>();
        List<String> betLines = new ArrayList<>();
        for (int i = 0; i < table.getPlayerCount(); i++) {
            BlackjackPlayer player = table.getPlayer(i);
            if (i == 0) {
                // Dealer — just record origin location
                player.setOriginLocation(player.getCharacter().getPosition());
                continue;
            }
            Character chr = player.getCharacter();
            if (chr.getMapId() != getChr().getMapId()) {
                SocialCommands.BotSpeak(getChr(), chr.getName() + " has left the map.");
                toKick.add(chr);
                continue;
            }
            player.setOriginLocation(new Point(chr.getPosition()));

            List<MapObject> items = BotLogic.readPlayersBetsStamps(chr, 5000);
            dprint("BET_SCAN: " + chr.getName() + " at " + player.getOriginLocation()
                    + " found " + items.size() + " stamp item(s)");

            if (items.isEmpty()) {
                SocialCommands.BotChatbubble(getChr(), "No Bet detected for: " + chr.getName());
                if (player.skipHand()) {
                    dprint("BET_SCAN: kicking " + chr.getName() + " (3 skips)");
                    toKick.add(chr);
                }
            } else {
                player.resetSkippedHands();
                player.setBets(items);
                betLines.add(announceBetString(chr, items));
            }
        }
        if (!betLines.isEmpty()) {
            SocialCommands.BotSpeak(getChr(), String.join(" | ", betLines));
        }
        for (Character chr : toKick) {
            kickPlayer(chr);
        }
    }

    // --- Payout & collection ---

    // Returns the Character that should be kicked (stole their bets), or null if nothing to kick.
    private Character payoutOrCollect(BlackjackPlayer player) {
        boolean blackjackBonus = false;
        switch (player.getResult()) {
            case BLACKJACK_WIN:
                blackjackBonus = true;
                // fall through
            case WIN:
                payWinner(player, blackjackBonus);
                return null;
            case LOSE:
                return lootLoserBets(player);
            case PUSH:
                player.setBetsProcessed(true);
                return null;
            default:
                log("Unknown hand result for " + player.getName());
                player.setBetsProcessed(true);
                return null;
        }
    }

    // Deliberate blocking beats on a dedicated virtual thread (runAsync): the
    // payout spray length is data-driven (bet quantity), so it can't be a chain.
    private void payWinner(BlackjackPlayer player, boolean blackjackBonus) {
        boolean isAI = isBot(player.getCharacter());
        ExecutorServiceManager.runAsync(() -> {
            for (MapObject item : player.getBets()) {
                MapItem mapItem = (MapItem) item;
                int quantity = blackjackBonus
                        ? (int) (mapItem.getItem().getQuantity() * 1.5)
                        : mapItem.getItem().getQuantity();

                for (int i = 0; i < quantity; i++) {
                    Point center = new Point(player.getOriginLocation());
                    center = adjustCenterPositionXAxis(center, i, 2, 4, 20);
                    DropCommands.botThrowItemToOwner(getChr(), mapItem.getItemId(), center, player.getCharacter());
                    blockingSleep(100 + random.nextInt(75));
                }
            }
            player.setBetsProcessed(true);

            if (isAI) {
                blockingSleep(2000 + random.nextInt(1500));
                double lootRadius = 1500;
                List<MapObject> betsOnFloor = BotLogic.readPlayersBetsStamps(
                        player.getCharacter(), player.getOriginLocation(), lootRadius);
                if (!betsOnFloor.isEmpty()) {
                    for (MapObject lootItem : betsOnFloor) {
                        DropCommands.lootItemListOnFloor(player.getCharacter(), List.of(lootItem));
                        blockingSleep(200 + random.nextInt(300));
                    }
                }
            }
        });
    }

    // Returns the thief Character if they stole their bets (caller should kick), else null.
    private Character lootLoserBets(BlackjackPlayer player) {
        Character chr = player.getCharacter();
        double lootRadius = isBot(chr) ? 1500 : 5000;
        boolean potCollectible = BotLogic.checkIfItemsOnFloorStill(
                player.getBets(),
                BotLogic.readPlayersBetsStamps(chr, player.getOriginLocation(), lootRadius));

        player.setBetsProcessed(true);

        if (potCollectible) {
            DropCommands.lootItemListOnFloor(getChr(), player.getBets());
            return null;
        }

        // Fame penalty for stealing bets
        chr.setFame(chr.getFame() - 10);
        chr.dropMessage(5, "[Mushroom Casino] You have stolen your bets. You have been defamed by the Mushroom Casino.");
        SocialCommands.BotSpeak(getChr(), "Player has stolen back his bet.");
        lootPlayerCards(player);
        return chr;
    }

    // --- Card cleanup ---

    private void cleanupAllCards() {
        for (BlackjackPlayer player : table.getPlayers()) {
            lootPlayerCards(player);
        }
    }

    private void lootPlayerCards(BlackjackPlayer player) {
        try {
            if (!player.getCardsOnMap().isEmpty()) {
                DropCommands.lootItemListOnFloor(getChr(), player.getCardsOnMap());
                player.clearCardsOnMap();
            }
        } catch (Exception e) {
            System.out.println("[Blackjack] Failed to loot cards for " + player.getName());
        }
    }

    // --- Artificial player handling ---

    private void triggerArtificialPlayerBets() {
        int stagger = 0;
        for (int i = 1; i < table.getPlayerCount(); i++) {
            BlackjackPlayer player = table.getPlayer(i);
            if (!isBot(player.getCharacter())) continue;
            if (player.hasBets()) continue;
            Character botChr = player.getCharacter();
            int delay = 1250 + stagger;
            MethodScheduler.runAfterDelay(() ->
                DropCommands.botDropItemQtyOwnerOnly(botChr, AI_BET_ITEM, AI_BET_QTY), delay);
            stagger += 1500 + random.nextInt(2000);
        }
    }

    private String outcomeToDialogueNode(BlackjackRules.Outcome outcome) {
        switch (outcome) {
            case WIN: return "PlayerWin";
            case BLACKJACK_WIN: return "PlayerBlackjack";
            case LOSE: return "PlayerLose";
            case PUSH: return "PlayerPush";
            default: return null;
        }
    }

    private void triggerAIPlayerReaction(BlackjackPlayer player, String dialogueNode) {
        if (!isBot(player.getCharacter())) return;
        BotDialogueHandler.DialogueConstructor dialog =
                BotDialogueHandler.getDialogueCon(dialoguePath, botType, dialogueNode);
        if (dialog == null) return;
        SocialCommands.BotEmote(player.getCharacter(), dialog.getEmote());
        if (random.nextDouble() < 0.30) {
            String line = dialog.getDialogue(random.nextInt(dialog.getDialogue().size()));
            SocialCommands.BotSpeak(player.getCharacter(), line);
        }
    }

    private void triggerDealerReaction(String dialogueNode) {
        BotDialogueHandler.DialogueConstructor dialog =
                BotDialogueHandler.getDialogueCon(dialoguePath, botType, dialogueNode);
        if (dialog == null) return;
        SocialCommands.BotEmote(getChr(), dialog.getEmote());
        if (random.nextDouble() < 0.55) {
            String line = dialog.getDialogue(random.nextInt(dialog.getDialogue().size()));
            SocialCommands.BotSpeak(getChr(), line);
        }
    }

    // The runAfterDelay body hops to a virtual thread, so the emote beat inside
    // may block (deliberate).
    private void triggerArtificialPlayerResponse(BlackjackPlayer player) {
        if (isBot(player.getCharacter())) {
            Character botChr = player.getCharacter();
            int handValue = player.getHandValue();
            MethodScheduler.runAfterDelay(() -> {
                String decision = BlackjackAI.hitOrStand(handValue);
                dprint("AI_DECISION " + botChr.getName() + " handValue="
                        + handValue + " -> " + decision);

                String dialogueNode = decision.equals("hit") ? "HitResponse" : "StandResponse";
                BotDialogueHandler.DialogueConstructor dialog =
                        BotDialogueHandler.getDialogueCon(dialoguePath, botType, dialogueNode);

                String spokenLine = decision;
                if (dialog != null && !dialog.getDialogue().isEmpty()) {
                    spokenLine = dialog.getDialogue(random.nextInt(dialog.getDialogue().size()));
                    int emote = dialog.getEmote();
                    if (emote > 0) {
                        SocialCommands.BotEmote(botChr, emote);
                        blockingSleep(400);
                    }
                }

                SocialCommands.BotSpeak(botChr, spokenLine);

                if (decision.equals("hit")) {
                    player.setResponseStatus("RESPONDED");
                    player.setStatus(BlackjackPlayer.PlayerStatus.ACTIVE);
                } else {
                    player.setResponseStatus("RESPONDED");
                    player.setStatus(BlackjackPlayer.PlayerStatus.STAND);
                }
            }, 1250 + random.nextInt(1500));
        }
    }

    // --- Message processing ---

    private void waitForResponse(BlackjackPlayer player) {
        if (!isBot(player.getCharacter()) && player.getCharacter().getMapId() != getChr().getMapId()) {
            SocialCommands.BotSpeak(getChr(), player.getName() + " has left. Automatic Stand.");
            player.setResponseStatus("RESPONDED");
            player.setStatus(BlackjackPlayer.PlayerStatus.STAND);
            return;
        }

        long endTime = startTime + (15 * 1000); // 15s pre-nag, 15s post-nag = 30s total

        if (System.currentTimeMillis() >= endTime) {
            if (finalDecision) {
                SocialCommands.BotSpeak(getChr(), "No response from " + player.getName() + ". Automatic Stand.");
                player.setResponseStatus("RESPONDED");
                player.setStatus(BlackjackPlayer.PlayerStatus.STAND);
                finalDecision = false;
            } else {
                SocialCommands.BotSpeak(getChr(), "Please Make a decision " + player.getName());
                if (!isBot(player.getCharacter())) {
                    showPlayerActionHint(player.getCharacter());
                }
                startTime = System.currentTimeMillis();
                finalDecision = true;
            }
            return;
        }

        processBlackjackMessages(player);
    }

    private void processBlackjackMessages(BlackjackPlayer player) {
        try {
            ChatMessage message = MessageQueue.getInstance().getMessageNonBlocking("secondary");
            if (message == null) {
                return;
            }
            log("[Blackjack] Message: " + message.getSender().getName() + ":" + message.getContent());

            if (!getInteractors().isRespondant(message.getSender())) {
                MessageQueue.getInstance().requeueMessage("secondary", message);
                return;
            }

            if (!message.getSender().equals(player.getCharacter())) {
                return;
            }
            String content = message.getContent().toLowerCase();
            if (content.contains("stand") || content.contains("stay")) {
                player.setResponseStatus("RESPONDED");
                player.setStatus(BlackjackPlayer.PlayerStatus.STAND);
            } else if (content.contains("hit")) {
                player.setResponseStatus("RESPONDED");
                player.setStatus(BlackjackPlayer.PlayerStatus.ACTIVE); // ACTIVE = HIT
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processJoinInquiries() {
        try {
            ChatMessage message = MessageQueue.getInstance().getMessageNonBlocking("tertiary");
            if (message == null) {
                return;
            }
            if (!getInteractors().isInquirer(message.getSender())) {
                MessageQueue.getInstance().requeueMessage("tertiary", message);
                return;
            }
            String content = message.getContent().toLowerCase();
            if (content.contains("join")) {
                Character sender = message.getSender();

                if (isAlreadyAtTable(sender)) {
                    sender.yellowMessage("You are already at the table.");
                    return;
                }

                boolean added = table.addPlayer(sender);
                dprint("JOIN attempt by " + sender.getName()
                        + " -> " + (added ? "ADDED" : "REJECTED (full)")
                        + " (tableSize=" + table.getPlayerCount() + ")");
                if (added) {
                    getInteractors().removeInquirer(sender);
                    getInteractors().setRespondant(sender);
                    SocialCommands.BotChatbubble(getChr(), sender.getName() + " has joined the table.");
                    sender.yellowMessage("You have joined " + getChr().getName() + "'s Blackjack table. You will be in the next round.");
                } else {
                    sender.yellowMessage("Table is full.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isAlreadyAtTable(Character chr) {
        for (BlackjackPlayer player : table.getPlayers()) {
            if (player.getCharacter().equals(chr)) {
                return true;
            }
        }
        return false;
    }

    // --- Display commands to player ---

    @Override
    public void displayCommands(Character chr) {
        // Inquiry hint — shown when a player calls the dealer by name.
        SocialCommands.displayPlayerChatCommands(chr, List.of("Join"));
    }

    // Shown to a real player when it becomes their turn.
    private void showPlayerActionHint(Character chr) {
        SocialCommands.displayPlayerChatCommands(chr, List.of("Stand", " - Hit -"));
    }

    // TODO: BotSwapItemAtLocation — not yet available in new architecture.
    // Face-down card reveal is skipped for now; dealer only receives 1 card
    // during initial deal and draws from deck during dealer turn.
}
