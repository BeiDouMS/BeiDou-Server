package soloMapling.ArtificialPlayer.BotTypes.Blackjack;

import java.util.Random;

public class BlackjackAI {

    private static final Random random = new Random();

    public static String hitOrStand(int handValue) {
        if (handValue <= 11) {
            return "hit";
        } else if (handValue <= 16) {
            double hitProbability = (17.0 - handValue) / 10.0;
            return (random.nextDouble() < hitProbability) ? "hit" : "stand";
        } else if (handValue == 17) {
            return (random.nextDouble() < 0.1) ? "hit" : "stand";
        } else {
            return "stand";
        }
    }
}
