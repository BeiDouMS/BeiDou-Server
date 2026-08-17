package soloMapling.ArtificialPlayer.BotDecoratorSystem;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotCustomization;
import soloMapling.ArtificialPlayer.BotTier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * NX (cash) cosmetic decoration for bots. Runs independently of QuickEquip and
 * the full decoration queue - both of those handle "real" gear in regular
 * inventory slots, while NX equips live in the separate cash overlay slots
 * and layer visually on top of whatever the bot is already wearing.
 *
 * Decision flow per bot:
 *   1. {@link #NX_BASE_CHANCE} flat gate - does this bot have NX at all?
 *   2. Roll intensity by tier (S=[3,8] ... D=[1,1]).
 *   3. Weighted draw without replacement of `intensity` slots from the main
 *      slot pool. Higher-tier slots (weapon / cap / clothing) are drawn first
 *      statistically so a bot with only 1-2 pieces still looks "real".
 *   4. Equip each picked slot, with two special cases:
 *        - WEAPON has a {@link #WEAPON_OPT_OUT_CHANCE} skip ("proud of my
 *          real weapon" players).
 *        - CLOTHING rolls overall vs top+bottom with the same either/or
 *          fallback as {@link QuickEquip}.
 *   5. Separately, a small tier-scaled chance to wear a ring (not part of the
 *      intensity budget).
 *
 * Toggle on/off via {@link #ENABLED} or the MMC `decoratenx` command.
 */
public class BotDecorateNX {

    public static boolean ENABLED = true;

    /** Flat chance per bot to even consider NX. Independent of tier. */
    private static final double NX_BASE_CHANCE = 0.20;

    /** Chance to skip the NX weapon even if the weapon slot was picked. */
    private static final double WEAPON_OPT_OUT_CHANCE = 0.40;

    /** Within the clothing branch, preference for overall over top+bottom. */
    private static final double OVERALL_PREFERENCE = 0.75;

    /** Tier -> [minIntensity, maxIntensity] inclusive. */
    private static final Map<BotTier, int[]> TIER_INTENSITY = Map.of(
            BotTier.S, new int[]{2, 5},
            BotTier.A, new int[]{1, 3},
            BotTier.B, new int[]{1, 2},
            BotTier.C, new int[]{1, 1},
            BotTier.D, new int[]{1, 1}
    );

    /** Tier -> chance to wear a single NX ring (separate from main intensity). */
    private static final Map<BotTier, Double> TIER_RING_CHANCE = Map.of(
            BotTier.S, 0.40,
            BotTier.A, 0.25,
            BotTier.B, 0.15,
            BotTier.C, 0.05,
            BotTier.D, 0.02
    );

    /**
     * Main slot pool with priority weights for the weighted-draw algorithm.
     * Rings are NOT in this pool - they're a separate layer below.
     *
     * Hierarchy (from user spec):
     *   TOP    (10): WEAPON, CAP, CLOTHING
     *   HIGH   (6) : SHOES, GLOVES, CAPE
     *   MEDIUM (3) : EARRING, FACE_ACC, EYE_ACC
     */
    private enum NxSlot {
        WEAPON(10),
        CAP(10),
        CLOTHING(10),
        SHOES(6),
        GLOVES(6),
        CAPE(6),
        EARRING(3),
        FACE_ACC(3),
        EYE_ACC(3);

        final int weight;

        NxSlot(int weight) {
            this.weight = weight;
        }
    }

    /**
     * Apply NX cosmetic decoration to a bot. NXItemPool caches all items at
     * load time (from YAML + WZ auto-population), so per-bot calls are just
     * in-memory RNG. Safe to call from spawn-time code.
     */
    public static void apply(Character bot) {
        if (!ENABLED) return;
        if (!NXItemPool.isLoaded()) {
            NXItemPool.load();
        }

        ThreadLocalRandom rng = ThreadLocalRandom.current();
        if (rng.nextDouble() >= NX_BASE_CHANCE) return;

        applyInternal(bot);
    }

    /**
     * Force-apply NX decoration to a bot, ignoring the ENABLED flag and the
     * base-chance gate. Used for testing via MMC commands.
     */
    public static void applyForced(Character bot) {
        if (!NXItemPool.isLoaded()) {
            NXItemPool.load();
        }
        applyInternal(bot);
    }

    private static void applyInternal(Character bot) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        BotTier tier = bot.getTier();
        int gender = bot.getGender();

        int intensity = rollIntensity(tier, rng);
        List<NxSlot> slots = pickSlots(intensity, rng);
        for (NxSlot slot : slots) {
            equipSlot(bot, slot, gender, rng);
        }

        maybeEquipRing(bot, tier, gender, rng);
    }

    private static int rollIntensity(BotTier tier, ThreadLocalRandom rng) {
        int[] range = TIER_INTENSITY.getOrDefault(tier, new int[]{1, 2});
        return rng.nextInt(range[0], range[1] + 1);
    }

    /**
     * Weighted draw without replacement of `count` slots from the main pool.
     * Higher-weighted slots are picked first statistically, so a bot with
     * intensity=2 tends to get top-tier slots rather than accessories.
     */
    private static List<NxSlot> pickSlots(int count, ThreadLocalRandom rng) {
        List<NxSlot> remaining = new ArrayList<>(Arrays.asList(NxSlot.values()));
        List<NxSlot> picked = new ArrayList<>(count);
        while (picked.size() < count && !remaining.isEmpty()) {
            int total = 0;
            for (NxSlot s : remaining) total += s.weight;
            int roll = rng.nextInt(total);
            int acc = 0;
            for (int i = 0; i < remaining.size(); i++) {
                acc += remaining.get(i).weight;
                if (roll < acc) {
                    picked.add(remaining.remove(i));
                    break;
                }
            }
        }
        return picked;
    }

    private static void equipSlot(Character bot, NxSlot slot, int gender, ThreadLocalRandom rng) {
        switch (slot) {
            case WEAPON:
                // Some players prefer their real weapon over a cosmetic override.
                if (rng.nextDouble() < WEAPON_OPT_OUT_CHANCE) return;
                equipFromCategory(bot, "weapons", gender);
                break;
            case CAP:
                equipFromCategory(bot, "caps", gender);
                break;
            case CLOTHING:
                equipClothing(bot, gender, rng);
                break;
            case SHOES:
                equipFromCategory(bot, "shoes", gender);
                break;
            case GLOVES:
                equipFromCategory(bot, "gloves", gender);
                break;
            case CAPE:
                equipFromCategory(bot, "capes", gender);
                break;
            case EARRING:
                equipFromCategory(bot, "earrings", gender);
                break;
            case FACE_ACC:
                equipFromCategory(bot, "face", gender);
                break;
            case EYE_ACC:
                equipFromCategory(bot, "eye", gender);
                break;
        }
    }

    /**
     * Clothing branch - same either/or fallback as QuickEquip: prefer overall
     * (75%), fall back to top+bottom (both-or-nothing) if no overall is
     * available, and vice versa.
     */
    private static void equipClothing(Character bot, int gender, ThreadLocalRandom rng) {
        boolean wantOverall = rng.nextDouble() < OVERALL_PREFERENCE;
        if (wantOverall) {
            if (!tryOverall(bot, gender)) tryTopBottom(bot, gender);
        } else {
            if (!tryTopBottom(bot, gender)) tryOverall(bot, gender);
        }
    }

    private static boolean tryOverall(Character bot, int gender) {
        Integer id = NXItemPool.getRandom("overalls", gender);
        if (id == null) return false;
        BotCustomization.EquipBot(bot, id);
        return true;
    }

    private static boolean tryTopBottom(Character bot, int gender) {
        Integer topId = NXItemPool.getRandom("tops", gender);
        Integer botId = NXItemPool.getRandom("bottoms", gender);
        if (topId == null || botId == null) return false;
        BotCustomization.EquipBot(bot, topId);
        BotCustomization.EquipBot(bot, botId);
        return true;
    }

    private static void maybeEquipRing(Character bot, BotTier tier, int gender, ThreadLocalRandom rng) {
        double chance = TIER_RING_CHANCE.getOrDefault(tier, 0.05);
        if (rng.nextDouble() >= chance) return;

        // v1: single ring only. Multi-ring (user's "occasionally 2") requires
        // slot-aware equipping across RING_1..RING_4, which BotCustomization
        // .EquipBot doesn't currently support - its commented-out
        // EquipBotRing helper is the stub. Leave multi-ring as future work;
        // equipping twice here would just overwrite the same RING_1 slot.
        equipFromCategory(bot, "rings", gender);
    }

    private static boolean equipFromCategory(Character bot, String category, int gender) {
        Integer itemId = NXItemPool.getRandom(category, gender);
        if (itemId == null) return false;
        BotCustomization.EquipBot(bot, itemId);
        return true;
    }
}
