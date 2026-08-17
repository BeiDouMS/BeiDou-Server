package soloMapling.ArtificialPlayer.BotDecoratorSystem;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotCustomization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// Hand-curated starter gear for level 1-9 beginner bots. Beginners all share the
// classless BEGINNER job, so the normal class-aware decoration pass picks random
// level-0 junk for them. This instead gives the recognisable default-newbie look:
// a basic 1h weapon, a default top/pants/shoes by gender, and the occasional cap.
// Everything is equipped by explicit item id, so it needs no EquipMetadataCache
// and is safe to run before/after environment init.
public class BeginnerEquip {

    private static final int SHOES = 1072005; // default starter shoes - both genders

    // Default starter tops (coat slot), split by gender.
    private static final int[] MALE_TOPS = {
            1040002, // white undershirt
            1040006, // undershirt
            1040010, // grey t-shirt
            1040036  // blue striped undershirt
    };
    private static final int[] FEMALE_TOPS = {
            1041002, // white tubetop
            1041006, // yellow t-shirt
            1041010, // green t-shirt
            1041011  // red striped top
    };

    // Cap groups, each entry is {itemId, minLevel}. A single cumulative roll picks
    // a group (or none); the totals (8 + 8 + 4 + 1 = 21%) land close to the
    // intended "roughly 1 in 5 bots wears a cap".
    private static final int[][] CAP_SKULL = {     // 8%
            {1002008, 5}, // brown skull cap
            {1002053, 5}, // green skullcap
            {1002054, 5}  // red skullcap
    };
    private static final int[][] CAP_HEADBAND = {  // 8%
            {1002014, 5}, // red headband
            {1002066, 5}, // black headband
            {1002069, 5}  // blue headband
    };
    private static final int[][] CAP_SPORTY = {    // 4%
            {1002424, 5}, // red sporty cap
            {1002425, 5}, // blue sporty cap
            {1002418, 0}  // newspaper hat (no level requirement)
    };
    private static final int[][] CAP_RARE = {      // 1% - novelty headwear
            {1002419, 0}, // mark of the beta
            {1002515, 0}, // maple bandana (white)
            {1002516, 0}, // maple bandana (yellow)
            {1002517, 0}, // maple bandana (red)
            {1002518, 0}  // maple bandana (blue)
    };

    // Beginners are levels 1-9 (job 0 / BEGINNER). Gate on level so this also
    // catches any bot whose job was never advanced past beginner.
    public static boolean isBeginner(Character bot) {
        return bot.getLevel() < 10;
    }

    // Full beginner outfit: weapon, top, pants, shoes, and a chance of a cap.
    // Deliberately nothing else - low-level newbies look plain.
    public static void apply(Character bot) {
        boolean male = bot.isMale();
        int level = bot.getLevel();

        equipWeapon(bot, level);
        equipTop(bot, male);
        equipPants(bot, level, male);
        BotCustomization.EquipBot(bot, SHOES);
        equipCap(bot, level);
    }

    private static void equipWeapon(Character bot, int level) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        List<Integer> pool = new ArrayList<>();
        pool.add(1302000); // sword
        pool.add(1312004); // hand axe
        pool.add(1322005); // wooden club
        // Razor and fruit knife are the nicer rolls: only a 25% chance to even
        // enter the pool, and only once the bot meets their level requirement.
        if (level >= 5 && rng.nextDouble() < 0.25) pool.add(1332005); // razor
        if (level >= 8 && rng.nextDouble() < 0.25) pool.add(1332007); // fruit knife
        BotCustomization.EquipBot(bot, pool.get(rng.nextInt(pool.size())));
    }

    private static void equipTop(Character bot, boolean male) {
        int[] tops = male ? MALE_TOPS : FEMALE_TOPS;
        BotCustomization.EquipBot(bot, tops[ThreadLocalRandom.current().nextInt(tops.length)]);
    }

    private static void equipPants(Character bot, int level, boolean male) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        List<Integer> pool = new ArrayList<>();
        if (male) {
            pool.add(1060002);
            pool.add(1060006);
            if (level >= 5) pool.add(1060007); // level 5+ only
        } else {
            pool.add(1061002);
            pool.add(1061008);
        }
        BotCustomization.EquipBot(bot, pool.get(rng.nextInt(pool.size())));
    }

    private static void equipCap(Character bot, int level) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int roll = rng.nextInt(100);
        int[][] group;
        if (roll < 8)       group = CAP_SKULL;     // 8%
        else if (roll < 16) group = CAP_HEADBAND;  // 8%
        else if (roll < 20) group = CAP_SPORTY;    // 4%
        else if (roll < 21) group = CAP_RARE;      // 1%
        else return;                               // ~79% - no cap

        Integer cap = pickEligible(group, level, rng);
        if (cap != null) BotCustomization.EquipBot(bot, cap);
    }

    // Picks uniformly among the group's items the bot is high enough level to wear.
    // Returns null when none qualify (e.g. a level 1-4 bot rolling a 5+ cap group),
    // in which case the bot simply goes capless.
    private static Integer pickEligible(int[][] group, int level, ThreadLocalRandom rng) {
        List<Integer> eligible = new ArrayList<>();
        for (int[] item : group) {
            if (level >= item[1]) eligible.add(item[0]);
        }
        if (eligible.isEmpty()) return null;
        return eligible.get(rng.nextInt(eligible.size()));
    }
}
