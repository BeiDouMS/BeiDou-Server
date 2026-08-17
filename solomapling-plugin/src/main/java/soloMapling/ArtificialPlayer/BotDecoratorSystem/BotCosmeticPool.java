package soloMapling.ArtificialPlayer.BotDecoratorSystem;

import soloMapling.ArtificialPlayer.BotTier;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Central registry for all cosmetic ID pools and the probability bridge
 * between bot tiers and cosmetic tiers.
 *
 * To add new hair/eye IDs: just append them to the appropriate array
 * in the pool maps below. The selection logic doesn't need to change.
 */
public class BotCosmeticPool {

    // ─── Probability bridge: bot tier → cosmetic tier selection weights ───
    // Each array is [PREMIUM, STANDARD, BASIC] and must sum to 100.

    private static final Map<BotTier, int[]> HAIR_EYE_WEIGHTS = new EnumMap<>(BotTier.class);

    static {
        //                              Premium  Standard  Basic
        HAIR_EYE_WEIGHTS.put(BotTier.S, new int[]{55,      35,      10});
        HAIR_EYE_WEIGHTS.put(BotTier.A, new int[]{40,      45,      15});
        HAIR_EYE_WEIGHTS.put(BotTier.B, new int[]{20,      55,      25});
        HAIR_EYE_WEIGHTS.put(BotTier.C, new int[]{10,      50,      40});
        HAIR_EYE_WEIGHTS.put(BotTier.D, new int[]{5,       40,      55});
    }

    // ─── Hair color variation chance by bot tier ───
    // Higher tier = more likely to have a non-default hair color

    private static final Map<BotTier, Double> HAIR_COLOR_CHANCE = new EnumMap<>(BotTier.class);

    static {
        HAIR_COLOR_CHANCE.put(BotTier.S, 0.60);
        HAIR_COLOR_CHANCE.put(BotTier.A, 0.45);
        HAIR_COLOR_CHANCE.put(BotTier.B, 0.30);
        HAIR_COLOR_CHANCE.put(BotTier.C, 0.18);
        HAIR_COLOR_CHANCE.put(BotTier.D, 0.10);
    }

    // ─── Eye color variation chance by bot tier ───

    private static final Map<BotTier, Double> EYE_COLOR_CHANCE = new EnumMap<>(BotTier.class);

    static {
        EYE_COLOR_CHANCE.put(BotTier.S, 0.55);
        EYE_COLOR_CHANCE.put(BotTier.A, 0.40);
        EYE_COLOR_CHANCE.put(BotTier.B, 0.25);
        EYE_COLOR_CHANCE.put(BotTier.C, 0.15);
        EYE_COLOR_CHANCE.put(BotTier.D, 0.08);
    }

    // ─── Wild card pools (selected via independent pre-roll, bypasses tier system) ───

    private static final double WILD_CHANCE = 0.01; // 0.5% = 1 in 200

    private static final int[] WILD_MALE_HAIR   = new int[]{30040, 30100, 30140, 30150, 30190, 30220, 30230, 30240, 30250, 30270, 30290, 30300, 30310, 30320, 30400, 30420, 30450, 30470, 30480, 30510, 30520, 30530, 30540, 30570, 30580, 30590, 30640, 30670, 30680, 30760, 30840, 30860, 30870, 30880};
    private static final int[] WILD_FEMALE_HAIR = new int[]{31010, 31030, 31060, 31080, 31160, 31180, 31200, 31250, 31260, 31270, 31280, 31320, 31330, 31350, 31400, 31460, 31600, 31670, 31700, 31720, 31810, 31840};
    private static final int[] WILD_MALE_EYES   = new int[]{20012};
    private static final int[] WILD_FEMALE_EYES = new int[]{21009};

    // ─── Male Hair Pools ───
    // Add/remove IDs as you research them. Base IDs only (color variant applied separately).

    private static final Map<CosmeticTier, int[]> MALE_HAIR = new EnumMap<>(CosmeticTier.class);

    static {
        MALE_HAIR.put(CosmeticTier.PREMIUM,  new int[]{30050, 30060, 30110, 30120, 30130, 30180, 30200, 30210, 30260, 30280, 30330, 30340, 30350, 30360, 30370, 30410, 30440, 30460, 30490, 30560, 30610, 30630, 30650, 30660, 30700, 30720, 30730, 30790, 30780, 30800, 30810, 30820, 30830, 30920});
        MALE_HAIR.put(CosmeticTier.STANDARD, new int[]{30020});
        MALE_HAIR.put(CosmeticTier.BASIC,    new int[]{30000, 30030});
    }

    // ─── Female Hair Pools ───

    private static final Map<CosmeticTier, int[]> FEMALE_HAIR = new EnumMap<>(CosmeticTier.class);

    static {
        FEMALE_HAIR.put(CosmeticTier.PREMIUM,  new int[]{31020, 31120, 31150, 31100, 31110, 31140, 31220, 31230, 31240, 31290, 31310, 31340, 31420, 31440, 31490, 31480, 31510, 31520, 31530, 31540, 31550, 31560, 31570, 31580, 31590, 31610, 31620, 31640, 31630, 31650, 31690, 31710, 31740, 31750, 31760, 31780, 31800, 31820, 31830, 31850, 31890, 31910, 31940, 31950, 33000, 34020, 34030, 34110});
        FEMALE_HAIR.put(CosmeticTier.STANDARD, new int[]{31000, 31040});
        FEMALE_HAIR.put(CosmeticTier.BASIC,    new int[]{31050});
    }

    // ─── Male Eye Pools ───

    private static final Map<CosmeticTier, int[]> MALE_EYES = new EnumMap<>(CosmeticTier.class);

    static {
        MALE_EYES.put(CosmeticTier.PREMIUM,  new int[]{20005});
        MALE_EYES.put(CosmeticTier.STANDARD, new int[]{20000, 20001, 20002});
        MALE_EYES.put(CosmeticTier.BASIC,    new int[]{20000, 20001, 20002});
    }

    // ─── Female Eye Pools ───

    private static final Map<CosmeticTier, int[]> FEMALE_EYES = new EnumMap<>(CosmeticTier.class);

    static {
        FEMALE_EYES.put(CosmeticTier.PREMIUM,  new int[]{21003, 21004});
        FEMALE_EYES.put(CosmeticTier.STANDARD, new int[]{21001, 21000, 21002});
        FEMALE_EYES.put(CosmeticTier.BASIC,    new int[]{21000, 21001, 21002});
    }

    // ─── Public selection methods ───

    /**
     * Selects a random hair ID based on the bot's gender and tier.
     * The bot tier influences which cosmetic tier pool is drawn from,
     * then a random ID is picked from that pool.
     */
    public static int selectHair(byte gender, BotTier botTier) {
        if (ThreadLocalRandom.current().nextDouble() < WILD_CHANCE) {
            int baseHair = pickRandom((gender == 1) ? WILD_FEMALE_HAIR : WILD_MALE_HAIR);
            return applyHairColor(baseHair, botTier);
        }
        Map<CosmeticTier, int[]> pool = (gender == 1) ? FEMALE_HAIR : MALE_HAIR;
        CosmeticTier cosmeticTier = rollCosmeticTier(botTier);
        int baseHair = pickRandom(pool.get(cosmeticTier));
        return applyHairColor(baseHair, botTier);
    }

    /**
     * Selects a random eye/face ID based on the bot's gender and tier.
     */
    public static int selectEyes(byte gender, BotTier botTier) {
        if (ThreadLocalRandom.current().nextDouble() < WILD_CHANCE) {
            int baseEye = pickRandom((gender == 1) ? WILD_FEMALE_EYES : WILD_MALE_EYES);
            return applyEyeColor(baseEye, botTier);
        }
        Map<CosmeticTier, int[]> pool = (gender == 1) ? FEMALE_EYES : MALE_EYES;
        CosmeticTier cosmeticTier = rollCosmeticTier(botTier);
        int baseEye = pickRandom(pool.get(cosmeticTier));
        return applyEyeColor(baseEye, botTier);
    }

    // ─── Internal helpers ───

    /**
     * Rolls a cosmetic tier based on the bot's tier using weighted probabilities.
     */
    private static CosmeticTier rollCosmeticTier(BotTier botTier) {
        int[] weights = HAIR_EYE_WEIGHTS.get(botTier);
        int roll = ThreadLocalRandom.current().nextInt(100);

        if (roll < weights[0]) {
            return CosmeticTier.PREMIUM;
        } else if (roll < weights[0] + weights[1]) {
            return CosmeticTier.STANDARD;
        } else {
            return CosmeticTier.BASIC;
        }
    }

    /**
     * Applies a random hair color variant based on bot tier.
     * Hair colors in v83 are base ID + 1 through +7.
     */
    private static int applyHairColor(int baseHair, BotTier botTier) {
        double chance = HAIR_COLOR_CHANCE.get(botTier);
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            int colorOffset = ThreadLocalRandom.current().nextInt(7) + 1; // 1-7
            return baseHair + colorOffset;
        }
        return baseHair;
    }

    /**
     * Applies a random eye color variant based on bot tier.
     * Eye colors in v83 are base ID + 100 through +800 (in increments of 100).
     */
    private static int applyEyeColor(int baseEye, BotTier botTier) {
        double chance = EYE_COLOR_CHANCE.get(botTier);
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            int colorOffset = (ThreadLocalRandom.current().nextInt(8) + 1) * 100; // 100-800
            return baseEye + colorOffset;
        }
        return baseEye;
    }

    private static int pickRandom(int[] array) {
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }
}
