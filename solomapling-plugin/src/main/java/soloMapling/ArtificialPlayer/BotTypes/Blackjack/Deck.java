package soloMapling.ArtificialPlayer.BotTypes.Blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private static final char[] SUITS = {'H', 'D', 'C', 'S'};
    private static final char[] RANKS = {'2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A'};
    private List<String> cards;

    public Deck() {
        cards = createAndShuffle();
    }

    private static List<String> createAndShuffle() {
        List<String> deck = new ArrayList<>(52);
        for (char suit : SUITS) {
            for (char rank : RANKS) {
                deck.add("" + rank + suit);
            }
        }
        Collections.shuffle(deck);
        return deck;
    }

    public String draw() {
        if (cards.isEmpty()) {
            cards = createAndShuffle();
        }
        return cards.remove(0);
    }

    public int remaining() {
        return cards.size();
    }

    public void reshuffle() {
        cards = createAndShuffle();
    }
}
