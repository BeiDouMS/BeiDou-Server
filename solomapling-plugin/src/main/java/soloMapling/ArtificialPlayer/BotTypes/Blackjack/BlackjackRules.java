package soloMapling.ArtificialPlayer.BotTypes.Blackjack;

import java.util.List;

public class BlackjackRules {

    public static int calculateHandValue(List<String> hand) {
        int totalValue = 0;
        int aceCount = 0;

        for (String card : hand) {
            char rank = card.charAt(0);
            switch (rank) {
                case 'A':
                    aceCount++;
                    totalValue += 11;
                    break;
                case 'K':
                case 'Q':
                case 'J':
                case 'T':
                    totalValue += 10;
                    break;
                default:
                    totalValue += Character.getNumericValue(rank);
                    break;
            }
        }

        while (totalValue > 21 && aceCount > 0) {
            totalValue -= 10;
            aceCount--;
        }

        return totalValue;
    }

    public static boolean isBlackjack(List<String> hand) {
        return hand.size() == 2 && calculateHandValue(hand) == 21;
    }

    public static boolean isBust(List<String> hand) {
        return calculateHandValue(hand) > 21;
    }

    public static boolean shouldDealerHit(int handValue) {
        return handValue < 17;
    }

    public static int getCardDisplayValue(String card) {
        char rank = card.charAt(0);
        switch (rank) {
            case '2': return 2;
            case '3': return 3;
            case '4': return 4;
            case '5': return 5;
            case '6': return 6;
            case '7': return 7;
            case '8': return 8;
            case '9': return 9;
            case 'T': return 10;
            case 'J': return 11;
            case 'Q': return 12;
            case 'K': return 13;
            case 'A': return 14;
            default: throw new IllegalArgumentException("Invalid card rank: " + rank);
        }
    }

    public enum Outcome {
        WIN,
        LOSE,
        PUSH,
        BLACKJACK_WIN
    }

    public static Outcome determineOutcome(List<String> playerHand, List<String> dealerHand) {
        int playerValue = calculateHandValue(playerHand);
        int dealerValue = calculateHandValue(dealerHand);
        boolean playerBJ = isBlackjack(playerHand);
        boolean dealerBJ = isBlackjack(dealerHand);

        if (playerValue > 21) return Outcome.LOSE;
        if (dealerValue > 21) return Outcome.WIN;
        if (playerBJ && dealerBJ) return Outcome.PUSH;
        if (playerBJ) return Outcome.BLACKJACK_WIN;
        if (dealerBJ) return Outcome.LOSE;
        if (playerValue == dealerValue) return Outcome.PUSH;
        return playerValue > dealerValue ? Outcome.WIN : Outcome.LOSE;
    }

    public static int calculatePayout(int betQuantity, Outcome outcome) {
        switch (outcome) {
            case BLACKJACK_WIN:
                return (int) (betQuantity * 1.5);
            case WIN:
                return betQuantity;
            case PUSH:
            case LOSE:
            default:
                return 0;
        }
    }

    public static String formatOutcomeMessage(String playerName, Outcome outcome, List<String> playerHand, List<String> dealerHand) {
        int playerValue = calculateHandValue(playerHand);
        int dealerValue = calculateHandValue(dealerHand);

        switch (outcome) {
            case LOSE:
                if (playerValue > 21) return playerName + ": Busted.";
                if (isBlackjack(dealerHand)) return playerName + ": Lose. Dealer Blackjack.";
                return playerName + ": Lose. Dealer wins.";
            case WIN:
                if (dealerValue > 21) return playerName + ": Win! Dealer busts.";
                return playerName + ": Win!";
            case BLACKJACK_WIN:
                return playerName + ": Blackjack!";
            case PUSH:
                return playerName + ": Push.";
            default:
                return playerName + ": Unexpected result.";
        }
    }
}
