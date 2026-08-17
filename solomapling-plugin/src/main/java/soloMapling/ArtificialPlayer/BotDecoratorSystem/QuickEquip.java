package soloMapling.ArtificialPlayer.BotDecoratorSystem;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotCustomization;
import soloMapling.ArtificialPlayer.BotTier;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Fast, lightweight equipment assignment for bots at spawn time.
 * Picks from the tiny curated {@link GenericEquipPool} - no WZ lookups.
 *
 * Higher-tier bots are more likely to have gear equipped.
 * Equipment is assigned in priority order:
 *   1. Clothing (top+bottom OR overall)  - highest priority
 *   2. Weapon (generic/classless)
 *   3. Cap
 *   4. Shoes
 *   5. Cape, Gloves                      - lowest priority
 *
 * Toggle on/off via {@link #ENABLED}.
 */
public class QuickEquip {

    public static boolean ENABLED = true;

    // Base probability that a bot of this tier has ANY equipment at all.
    // Individual slots scale down from this base.
    private static final Map<BotTier, Double> TIER_EQUIP_CHANCE = Map.of(
            BotTier.S, 0.90,
            BotTier.A, 0.75,
            BotTier.B, 0.55,
            BotTier.C, 0.35,
            BotTier.D, 0.15
    );

    /**
     * Apply quick generic equipment to a bot based on tier probability.
     * Fast path - only array lookups from the pre-loaded pool.
     */
    public static void apply(Character bot) {
        if (!ENABLED) return;
        if (!GenericEquipPool.isLoaded()) {
            GenericEquipPool.load();
        }

        BotTier tier = bot.getTier();
        double base = TIER_EQUIP_CHANCE.getOrDefault(tier, 0.30);
        int level = bot.getLevel();
        int gender = bot.getGender();
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        // 1. Clothing (most important) - full base chance.
        // Overalls (sauna robes etc.) are heavily preferred at 75%.
        // If the chosen branch has no eligible item for this bot (e.g. a level-15
        // bot rolling overall but no low-level overall exists), fall back to the
        // other branch so the bot is never left bare-torso'd.
        if (rng.nextDouble() < base) {
            boolean wantOverall = rng.nextDouble() < 0.75;
            if (wantOverall) {
                if (!tryOverall(bot, level, gender)) tryTopBottom(bot, level, gender);
            } else {
                if (!tryTopBottom(bot, level, gender)) tryOverall(bot, level, gender);
            }
        }

        // 2. Weapon - 90% of base
        if (rng.nextDouble() < base * 0.90) {
            equipFromPool(bot, "weapons", level, gender);
        }

        // 3. Cap - 60% of base
        if (rng.nextDouble() < base * 0.60) {
            equipFromPool(bot, "caps", level, gender);
        }

        // 4. Shoes - 50% of base
        if (rng.nextDouble() < base * 0.50) {
            equipFromPool(bot, "shoes", level, gender);
        }

        // 5. Accessories - 30% of base
        if (rng.nextDouble() < base * 0.30) {
            equipFromPool(bot, "capes", level, gender);
        }
        if (rng.nextDouble() < base * 0.30) {
            equipFromPool(bot, "gloves", level, gender);
        }
    }

    private static void equipFromPool(Character bot, String category, int level, int gender) {
        Integer itemId = GenericEquipPool.getRandom(category, level, gender);
        if (itemId != null) {
            BotCustomization.EquipBot(bot, itemId);
        }
    }

    /** @return true if an overall was found and equipped. */
    private static boolean tryOverall(Character bot, int level, int gender) {
        Integer id = GenericEquipPool.getRandom("overalls", level, gender);
        if (id == null) return false;
        BotCustomization.EquipBot(bot, id);
        return true;
    }

    /**
     * Both-or-nothing: only commits if BOTH top and bottom are available, so a bot
     * never ends up wearing a shirt with no pants.
     * @return true if both pieces were found and equipped.
     */
    private static boolean tryTopBottom(Character bot, int level, int gender) {
        Integer topId = GenericEquipPool.getRandom("tops", level, gender);
        Integer botId = GenericEquipPool.getRandom("bottoms", level, gender);
        if (topId == null || botId == null) return false;
        BotCustomization.EquipBot(bot, topId);
        BotCustomization.EquipBot(bot, botId);
        return true;
    }
}
