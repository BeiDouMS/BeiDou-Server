package soloMapling.ArtificialPlayer.BotTypes.Blackjack;

import org.gms.client.Character;

import java.util.ArrayList;
import java.util.List;

public class BlackjackTable {

    public enum Phase {
        WAITING,
        BETTING,
        PROCESSING_BETS,
        DEALING,
        PLAYER_TURNS,
        PLAYER_RESPONSE,
        DEALER_TURN,
        RESOLVING,
        PAYOUT
    }

    private static final int MAX_PLAYERS = 8; // including dealer at index 0

    private Phase phase;
    private final Deck deck;
    private final List<BlackjackPlayer> players;
    private int currentPlayerIndex;
    private int waitCount;

    public BlackjackTable(Character dealerCharacter) {
        this.deck = new Deck();
        this.players = new ArrayList<>(MAX_PLAYERS);
        this.players.add(new BlackjackPlayer(dealerCharacter)); // Index 0 = dealer
        this.phase = Phase.WAITING;
        this.currentPlayerIndex = 1;
        this.waitCount = 0;
    }

    // Phase

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    // Players

    public List<BlackjackPlayer> getPlayers() {
        return players;
    }

    public BlackjackPlayer getDealer() {
        return players.get(0);
    }

    public BlackjackPlayer getPlayer(int index) {
        if (index >= 0 && index < players.size()) {
            return players.get(index);
        }
        return null;
    }

    public BlackjackPlayer getCurrentPlayer() {
        return getPlayer(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public boolean addPlayer(Character character) {
        if (players.size() < MAX_PLAYERS) {
            players.add(new BlackjackPlayer(character));
            return true;
        }
        return false;
    }

    public boolean removePlayer(Character character) {
        for (int i = 1; i < players.size(); i++) { // never remove dealer at index 0
            if (players.get(i).getCharacter().equals(character)) {
                players.remove(i);
                // Keep currentPlayerIndex pointing at the "same slot" in the shrunken list:
                //  - if removed before current → shift index down
                //  - if removed at/after current → no change (what was next is now at currentPlayerIndex)
                //  - if index now past end → wrap to dealer (0)
                if (currentPlayerIndex > i) {
                    currentPlayerIndex--;
                }
                if (currentPlayerIndex >= players.size()) {
                    currentPlayerIndex = 0;
                }
                return true;
            }
        }
        return false;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public boolean hasEnoughPlayers() {
        return players.size() > 1;
    }

    // Turn management

    public void incrementToNextPlayer() {
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0; // wraps to dealer = signals dealer turn
        }
    }

    public boolean isCurrentPlayerDealer() {
        return currentPlayerIndex == 0;
    }

    // Round reset

    public void resetForNewRound() {
        for (BlackjackPlayer player : players) {
            player.resetForNewRound();
        }
        currentPlayerIndex = 1;
        waitCount = 0;
    }

    // Betting

    public int getWaitCount() {
        return waitCount;
    }

    public void incrementWaitCount() {
        waitCount++;
    }

    public boolean hasAtLeastOneBet() {
        for (int i = 1; i < players.size(); i++) {
            if (players.get(i).hasBets()) {
                return true;
            }
        }
        return false;
    }

    // Dealing

    public static int getDealSequenceIndex(int index, int totalPlayers) {
        // Deals to players first (1, 2, ... n-1) then dealer (0) last each round
        if (index == totalPlayers - 1) {
            return 0;
        } else {
            return (index % (totalPlayers - 1)) + 1;
        }
    }

    public String drawCard() {
        return deck.draw();
    }

    public void dealCardToPlayer(BlackjackPlayer player, String card) {
        player.addCardToHand(card);
    }

    // Dealer turn logic

    public boolean shouldDealerHit() {
        return BlackjackRules.shouldDealerHit(getDealer().getHandValue());
    }

    // Outcome resolution

    public void resolveAllOutcomes() {
        List<String> dealerHand = getDealer().getHand();
        for (int i = 1; i < players.size(); i++) {
            BlackjackPlayer player = players.get(i);
            if (player.getHand().isEmpty()) {
                continue;
            }
            BlackjackRules.Outcome outcome = BlackjackRules.determineOutcome(player.getHand(), dealerHand);
            switch (outcome) {
                case WIN:
                    player.setResult(BlackjackPlayer.HandResult.WIN);
                    break;
                case LOSE:
                    player.setResult(BlackjackPlayer.HandResult.LOSE);
                    break;
                case PUSH:
                    player.setResult(BlackjackPlayer.HandResult.PUSH);
                    break;
                case BLACKJACK_WIN:
                    player.setResult(BlackjackPlayer.HandResult.BLACKJACK_WIN);
                    break;
            }
        }
    }
}
