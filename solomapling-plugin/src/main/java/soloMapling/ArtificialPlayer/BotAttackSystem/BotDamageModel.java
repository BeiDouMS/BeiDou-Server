package soloMapling.ArtificialPlayer.BotAttackSystem;

import java.util.concurrent.ThreadLocalRandom;

/*
 * Original. The single place that decides how hard a bot hits. Damage is combat feel, not balance
 * (bots are decoration), so every number here is meant to be eyeballed in-game and tuned freely.
 *
 * The dominant axis is the bot's JOB TIER (0 beginner .. 4 fourth job): a beginner's basic swing
 * lands ~20-40, a 3rd job lands thousands. Level is a gentle secondary ramp WITHIN a tier, so a
 * level-70 2nd-job out-hits a level-35 one without erasing the tier jumps. Single/double-line
 * skills concentrate their damage into a bigger per-line hit (the heavy chunk that visibly shoves
 * a mob back); many-line flurries keep the base per-line band.
 */
public final class BotDamageModel {

    private BotDamageModel() {}

    // Per-line damage band by job tier (index 0..4 = beginner / 1st / 2nd / 3rd / 4th). First-pass,
    // believable-v83 values - recalibrate against live mob HP. {min, max}, both inclusive.
    private static final int[][] TIER_BAND = {
            {   20,    40 },  // 0 beginner - plain weapon swing
            {   70,   160 },  // 1 first job
            {  240,   620 },  // 2 second job
            {  900,  2300 },  // 3 third job
            { 2800,  6500 },  // 4 fourth job
    };

    // Gentle within-tier / cross-level ramp: adds level/LEVEL_RAMP_DIVISOR of the rolled base.
    // lv10 ~+4%, lv70 ~+28%, lv120 ~+48%. Tier stays the dominant axis.
    private static final double LEVEL_RAMP_DIVISOR = 250.0;

    // Single/double-line skills hit this much harder per line - the "single line bigger" feel and
    // the knockback chunk in one knob. Rolled per swing for natural variance. Many-line flurries
    // (Triple Throw, Savage Blow, Hurricane) spread their damage and skip this.
    private static final int SINGLE_LINE_MAX_LINES = 2;
    private static final double SINGLE_LINE_MULT_MIN = 2.0;
    private static final double SINGLE_LINE_MULT_MAX = 2.5;

    /* One per-line damage roll for a bot of this job tier and level, on a swing of this many lines. */
    public static int rollLine(int jobTier, int level, int numDamageLines) {
        int tier = Math.max(0, Math.min(TIER_BAND.length - 1, jobTier));
        int[] band = TIER_BAND[tier];
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        int base = (band[0] >= band[1]) ? band[0] : rng.nextInt(band[0], band[1] + 1);

        if (level > 0) {
            base = (int) Math.round(base * (1.0 + level / LEVEL_RAMP_DIVISOR));
        }
        if (numDamageLines <= SINGLE_LINE_MAX_LINES) {
            double mult = SINGLE_LINE_MULT_MIN + rng.nextDouble() * (SINGLE_LINE_MULT_MAX - SINGLE_LINE_MULT_MIN);
            base = (int) Math.round(base * mult);
        }
        return Math.max(1, base);
    }
}
