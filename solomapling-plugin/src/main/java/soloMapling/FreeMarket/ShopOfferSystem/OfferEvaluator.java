package soloMapling.FreeMarket.ShopOfferSystem;

import java.util.Random;

public class OfferEvaluator {

    public enum Decision {
        DECLINE,
        ACCEPT,
        COUNTER
    }

    private static final Random random = new Random();

    public static Decision evaluate(long offerPrice, int listingPrice) {
        if (listingPrice <= 0) return Decision.DECLINE;

        double ratio = offerPrice / (double) listingPrice;

        if (ratio < 0.70) {
            return Decision.DECLINE;
        }

        double acceptChance = getAcceptChance(ratio);

        double roll = random.nextDouble();
        if (roll < acceptChance) {
            return Decision.ACCEPT;
        }

        if (ratio >= 0.80) {
            return Decision.COUNTER;
        }

        return Decision.DECLINE;
    }

    private static double getAcceptChance(double ratio) {
        if (ratio >= 0.95) {
            return 0.75 + random.nextDouble() * 0.25;
        } else if (ratio >= 0.90) {
            return 0.50 + random.nextDouble() * 0.20;
        } else if (ratio >= 0.85) {
            return 0.40 + random.nextDouble() * 0.20;
        } else if (ratio >= 0.80) {
            return 0.25 + random.nextDouble() * 0.15;
        } else {
            return 0.05 + random.nextDouble() * 0.10;
        }
    }

    public static long calculateCounterPrice(long offerPrice, int listingPrice) {
        return (offerPrice + listingPrice) / 2;
    }
}
