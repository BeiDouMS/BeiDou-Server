package soloMapling.ArtificialPlayer.BotDecoratorSystem;

import org.gms.client.Character;
import org.gms.client.Job;
import org.gms.client.inventory.InventoryType;
import soloMapling.ArtificialPlayer.BotTier;
import soloMapling.itemPool.EquipMetadataCache;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class BotDecorate {

    /**
     * Fraction of bots that get the full class-aware decoration pass.
     * The rest keep only their QuickEquip generic/classless outfit so the
     * population reads as a mix of casual and kitted-out players.
     * Tune to taste while experimenting.
     */
    public static final double FULL_DECORATION_RATE = 0.5;

    public static int calculate_min_equip_level(short level) {
        int nearestLowInterval = (((level / 10) * 10) - 10) + 1;
        return nearestLowInterval;
    }

    /**
     * Returns a randomly selected BotTier based on a modified bell curve distribution:
     * - S tier is very rare (5%)
     * - A tier is uncommon (15%)
     * - B tier is common (40%)
     * - C tier is common (30%)
     * - D tier is uncommon (10%)
     *
     * @return A randomly selected BotTier
     */
    public static BotTier getRandomTier() {
        Random random = new Random();
        int roll = random.nextInt(100); // 0-99

        // original rates
//        if (roll < 5) {
//            return BotTier.S;       // 5% chance for S tier
//        } else if (roll < 20) {
//            return BotTier.A;       // 15% chance for A tier
//        } else if (roll < 60) {
//            return BotTier.B;       // 40% chance for B tier (most common)
//        } else if (roll < 90) {
//            return BotTier.C;       // 30% chance for C tier (2nd most common)
//        } else {
//            return BotTier.D;       // 10% chance for D tier
//        }

        // for demo
        if (roll < 30) {
            return BotTier.S;
        } else if (roll < 60) {
            return BotTier.A;
        } else if (roll < 80) {
            return BotTier.B;
        } else if (roll < 90) {
            return BotTier.C;
        } else {
            return BotTier.D;
        }

    }

    /**
     * Generates a random level for a bot based on its tier (S, A, B, C) and the overall level range.
     * S tier gets the highest levels, followed by A, then B, with C tier getting the lowest levels.
     *
     * @param tier     The tier of the bot (S, A, B, C)
     * @param minLevel The minimum level in the range
     * @param maxLevel The maximum level in the range
     * @return A randomly selected level appropriate for the tier
     */
    public static int generateBotLevel(BotTier tier, int minLevel, int maxLevel) {
        // Validate input parameters
        if (minLevel >= maxLevel) {
            throw new IllegalArgumentException("minLevel must be less than maxLevel");
        }

        // Calculate the total range
        int range = maxLevel - minLevel + 1;

        // Define sub-ranges based on the tier
        int lowerBound, upperBound;

        switch (tier) {
            case BotTier.S:
                // S tier gets the top quarter of the range
                lowerBound = minLevel + (3 * range / 4);
                upperBound = maxLevel;
                break;
            case BotTier.A:
                // A tier gets the second highest quarter
                lowerBound = minLevel + (2 * range / 4);
                upperBound = minLevel + (3 * range / 4) - 1;
                break;
            case BotTier.B:
                // B tier gets the second lowest quarter
                lowerBound = minLevel + (range / 4);
                upperBound = minLevel + (2 * range / 4) - 1;
                break;
            case BotTier.C:
            case BotTier.D:
                // C tier gets the bottom quarter of the range
                lowerBound = minLevel;
                upperBound = minLevel + (range / 4) - 1;
                break;
            default:
                throw new IllegalArgumentException("Unknown BotTier: " + tier);
        }

        // Generate a random number in the specified sub-range
        return (int) (Math.random() * (upperBound - lowerBound + 1)) + lowerBound;
    }

    /**
     * Selects an appropriate MapleStory job based on character level.
     *
     * @param level The character's level
     * @return The job ID constant corresponding to the appropriate job
     */
    public static int selectJobByLevel(int level) {
        // Beginner for levels 1-9
        if (level < 10) {
            return 0; // BEGINNER
        }

        return selectJobForClass(rollBaseClass(), level);
    }

    // Weighted base-class roll (Pirate excluded). Skewed to match v83 nostalgia:
    // thieves everywhere, mages second, warriors third, bowmen the rarest.
    // Both roll sites (generic decoration + training-bot spawn) go through here
    // so the population mix stays in sync. Returns 1=Warrior 2=Magician 3=Bowman 4=Thief.
    public static int rollBaseClass() {
        int roll = (int) (Math.random() * 100); // 0..99
        if (roll < 31) return 4;       // THIEF    31%
        else if (roll < 56) return 2;  // MAGICIAN 25%  (31..55)
        else if (roll < 80) return 1;  // WARRIOR  24%  (56..79)
        else return 3;                 // BOWMAN   20%  (80..99)
    }

    // Sub-path rolls, weighted per class. Path indices match the tier switches below
    // (warrior: 1 Fighter / 2 Page / 3 Spearman; mage: 1 F-P / 2 I-L / 3 Cleric).
    private static int rollWarriorPath() {
        int roll = (int) (Math.random() * 100);
        if (roll < 44) return 3;       // SPEARMAN 44%
        else if (roll < 74) return 1;  // FIGHTER  30%  (44..73)
        else return 2;                 // PAGE     26%  (74..99)
    }

    private static int rollMagePath() {
        int roll = (int) (Math.random() * 100);
        if (roll < 44) return 3;       // CLERIC 44%  (bishops were overpowered)
        else if (roll < 74) return 2;  // I-L    30%  (44..73)
        else return 1;                 // F-P    26%  (74..99)
    }

    // Thief: assassin slightly over bandit. Bowman stays an even 50/50.
    private static boolean rollThiefAssassin() {
        return Math.random() < 0.55;
    }

    public static int selectJobForClass(int baseClass, int level) {
        // Beginner for levels 1-9
        if (level < 10) {
            return 0; // BEGINNER
        }

        // First job advancement (levels 10-29)
        if (level < 30) {
            switch (baseClass) {
                case 1:
                    return 100; // WARRIOR
                case 2:
                    return 200; // MAGICIAN
                case 3:
                    return 300; // BOWMAN
                case 4:
                    return 400; // THIEF
                default:
                    return 0;  // BEGINNER (fallback)
            }
        }

        // Second job advancement (levels 30-69)
        if (level < 70) {
            switch (baseClass) {
                case 1: // WARRIOR paths
                    int warriorPath = rollWarriorPath();
                    switch (warriorPath) {
                        case 1:
                            return 110; // FIGHTER
                        case 2:
                            return 120; // PAGE
                        case 3:
                            return 130; // SPEARMAN
                    }
                case 2: // MAGICIAN paths
                    int magePath = rollMagePath();
                    switch (magePath) {
                        case 1:
                            return 210; // FP_WIZARD
                        case 2:
                            return 220; // IL_WIZARD
                        case 3:
                            return 230; // CLERIC
                    }
                case 3: // BOWMAN paths
                    return (Math.random() < 0.5) ? 310 : 320; // HUNTER or CROSSBOWMAN
                case 4: // THIEF paths
                    return rollThiefAssassin() ? 410 : 420; // ASSASSIN or BANDIT
                default:
                    return 0; // BEGINNER (fallback)
            }
        }

        // Third job advancement (levels 70-119)
        if (level < 120) {
            switch (baseClass) {
                case 1: // WARRIOR paths
                    int warriorPath = rollWarriorPath();
                    switch (warriorPath) {
                        case 1:
                            return 111; // CRUSADER
                        case 2:
                            return 121; // WHITEKNIGHT
                        case 3:
                            return 131; // DRAGONKNIGHT
                    }
                case 2: // MAGICIAN paths
                    int magePath = rollMagePath();
                    switch (magePath) {
                        case 1:
                            return 211; // FP_MAGE
                        case 2:
                            return 221; // IL_MAGE
                        case 3:
                            return 231; // PRIEST
                    }
                case 3: // BOWMAN paths
                    return (Math.random() < 0.5) ? 311 : 321; // RANGER or SNIPER
                case 4: // THIEF paths
                    return rollThiefAssassin() ? 411 : 421; // HERMIT or CHIEFBANDIT
                default:
                    return 0; // BEGINNER (fallback)
            }
        }

        // Fourth job advancement (levels 120+)
        switch (baseClass) {
            case 1: // WARRIOR paths
                int warriorPath = rollWarriorPath();
                switch (warriorPath) {
                    case 1:
                        return 112; // HERO
                    case 2:
                        return 122; // PALADIN
                    case 3:
                        return 132; // DARKKNIGHT
                }
            case 2: // MAGICIAN paths
                int magePath = rollMagePath();
                switch (magePath) {
                    case 1:
                        return 212; // FP_ARCHMAGE
                    case 2:
                        return 222; // IL_ARCHMAGE
                    case 3:
                        return 232; // BISHOP
                }
            case 3: // BOWMAN paths
                return (Math.random() < 0.5) ? 312 : 322; // BOWMASTER or MARKSMAN
            case 4: // THIEF paths
                return rollThiefAssassin() ? 412 : 422; // NIGHTLORD or SHADOWER
            default:
                return 0; // BEGINNER (fallback)
        }
    }

    /**
     * Selects a gender at random with 50/50 probability.
     *
     * @return 0 for male, 1 for female
     */
    public static int selectRandomGender() {
        // Create a Random object
        Random random = new Random();

        // Return 0 (male) or 1 (female) with equal probability
        return random.nextInt(2);
    }

    public static void setBotVariables(Character bot) {
        BotTier tier = getRandomTier();
        bot.setTier(tier);
        int level = generateBotLevel(tier, 10, 80);
        int job = selectJobByLevel(level);
        bot.setGender(selectRandomGender());
        bot.setLevel(level);
        bot.setJob(Job.getById(job));

        BotDecorateBody.decorateBotBody(bot);

        if (EquipMetadataCache.isInitialized()) {
            // Full class-aware decoration is an in-memory cache lookup now, so it
            // runs inline at spawn. FULL_DECORATION_RATE preserves the population
            // mix of "kitted out" (full decoration) and "casual" (QuickEquip) bots.
            if (ThreadLocalRandom.current().nextDouble() < FULL_DECORATION_RATE) {
                BotDecorateEquips.decorateBotEquips(bot);
            } else {
                QuickEquip.apply(bot);
                // Safety net: if QuickEquip left the bot with no clothing (common
                // for low-level bots where the curated pool has no matching items),
                // run the full decoration so they don't walk around shirtless.
                if (!hasClothing(bot)) {
                    BotDecorateEquips.decorateBotEquips(bot);
                }
            }
        } else {
            // Cache not ready (bots spawned before environment init): quick
            // generic equip now, full decoration deferred to the queue.
            QuickEquip.apply(bot);
            if (!hasClothing(bot)
                    || ThreadLocalRandom.current().nextDouble() < FULL_DECORATION_RATE) {
                BotDecorationQueue.addBot("default", bot.getId());
            }
        }

        // NX cosmetic layer - runs on every bot regardless of which equip path
        // it took. Its own 30% base gate decides whether the bot actually gets
        // any NX pieces.
        BotDecorateNX.apply(bot);
    }

    public static void setBotVariables(Character bot, int baseClass, int minLevel, int maxLevel) {
        setBotVariables(bot, baseClass, minLevel, maxLevel, 0);
    }

    // forcedJobId > 0 pins the exact job (e.g. the GM 'trainhere hermit' test spawn); 0 = a random job
    // appropriate for the class + level. Everything else (level, gender, body, class-coherent gear, NX) is
    // identical, so a forced-job bot is decorated ONCE, correctly — no post-hoc re-decoration needed.
    public static void setBotVariables(Character bot, int baseClass, int minLevel, int maxLevel, int forcedJobId) {
        BotTier tier = getRandomTier();
        bot.setTier(tier);
        // generateBotLevel requires min < max; an exact-level spawn pins both ends.
        int level = (minLevel >= maxLevel) ? minLevel : generateBotLevel(tier, minLevel, maxLevel);
        int job = (forcedJobId > 0) ? forcedJobId : selectJobForClass(baseClass, level);
        bot.setGender(selectRandomGender());
        bot.setLevel(level);
        bot.setJob(Job.getById(job));

        BotDecorateBody.decorateBotBody(bot);

        // Level 1-9 beginners get curated starter gear and nothing else (no
        // class-aware pass, no deferred queue, no NX overlay) so they read as
        // plain newbies. Uses explicit item ids, so the cache state doesn't matter.
        if (BeginnerEquip.isBeginner(bot)) {
            BeginnerEquip.apply(bot);
            return;
        }

        if (EquipMetadataCache.isInitialized()) {
            BotDecorateEquips.decorateBotEquips(bot);
        } else {
            // Cache not ready (env still booting): dress generically now and defer
            // the full class-aware pass - same fallback the random path uses.
            QuickEquip.apply(bot);
            BotDecorationQueue.addBot("default", bot.getId());
        }

        BotDecorateNX.apply(bot);
    }

    private static boolean hasClothing(Character bot) {
        return bot.getInventory(InventoryType.EQUIPPED).getItem((short) -5) != null  // coat/overall
                || bot.getInventory(InventoryType.EQUIPPED).getItem((short) -6) != null; // pants
    }

    /**
     * Convenience method: add a bot to the deferred decoration queue under a specific category.
     * Call this after spawn if you want a category other than "default" (e.g. "fm", "henesys").
     */
    public static void addBotToDecorationQueue(String category, int botId) {
        BotDecorationQueue.addBot(category, botId);
    }
}
