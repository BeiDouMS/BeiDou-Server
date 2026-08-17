package soloMapling.ArtificialPlayer.BotTypes.Blackjack;

import java.util.HashMap;
import java.util.Map;

public class CardVisuals {

    private static final Map<Integer, Integer> cardValueToItemId = new HashMap<>();

    static {
        // Card value -> Monster Card item ID
        // These are MapleStory monster cards used as visual representations of playing cards
        cardValueToItemId.put(2, 2380004);   // 2 - Red Snail
        cardValueToItemId.put(3, 2380005);   // 3 - Slime
        cardValueToItemId.put(4, 2380007);   // 4 - Orange Mushroom
        cardValueToItemId.put(5, 2380009);   // 5 - Ribbon Pig
        cardValueToItemId.put(6, 2381007);   // 6 - Horny Mushroom
        cardValueToItemId.put(7, 2381024);   // 7 - Evil Eye
        cardValueToItemId.put(8, 2383012);   // 8 - Drake
        cardValueToItemId.put(9, 2383043);   // 9 - Mixed Golem
        cardValueToItemId.put(10, 2388017);  // 10 - Crimson Balrog
        cardValueToItemId.put(11, 2388022);  // J - Papulatus // Eckhart 2388150
        cardValueToItemId.put(12, 2388023);  // Q - Zakum // Oz 2388148
        cardValueToItemId.put(13, 2388024);  // K - Horn Tail // Mihile 2388147
        cardValueToItemId.put(14, 2388043);  // A - Pink Bean // Empress 2388152
    }

    public static int getCardItemId(String card) {
        int displayValue = BlackjackRules.getCardDisplayValue(card);
        Integer itemId = cardValueToItemId.get(displayValue);
        if (itemId == null) {
            throw new IllegalArgumentException("No visual mapping for card: " + card);
        }
        return itemId;
    }
}
