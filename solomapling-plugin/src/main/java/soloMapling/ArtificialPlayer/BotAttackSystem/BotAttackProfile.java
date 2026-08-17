package soloMapling.ArtificialPlayer.BotAttackSystem;

import org.gms.client.Job;

/*
 * Original fixed-damage attack profile; the attack-plan concept is inspired by GreenCatMS. Credit: NutNNut for the idea.
 * One bot attack: its route, the skill that renders, how many mobs/lines it hits, its reach box,
 * and timing. Pure data - build them with the per-route factories (melee/meleeAoe/magic/ranged/...)
 * so reach/cooldown defaults stay in one place. Per-line damage is NOT a property of the profile;
 * it comes from the bot's job tier + level via BotDamageModel (see rollDamage). altSkillId covers
 * warrior skills with a second weapon form (sword vs axe, spear vs pole-arm).
 */
public final class BotAttackProfile {

    public enum Route { CLOSE, RANGED, MAGIC }

    public final Route route;
    public final int numAttacked;   // mobs hit (1 = single-target, >1 = AoE)
    public final int numDamage;     // damage lines per mob
    public final int reachX;
    public final int reachY;
    public final int cooldownMs;
    public final short hitDelayMs;
    public final int speed;
    public final int skillId;       // 0 = no-skill basic swing
    public final int skillLevel;
    public final int altSkillId;    // weapon-alternate skill (axe/pole-arm); 0 = none

    private BotAttackProfile(Route route, int numAttacked, int numDamage, int reachX, int reachY,
                             int cooldownMs, short hitDelayMs, int speed,
                             int skillId, int altSkillId) {
        this.route = route;
        this.numAttacked = numAttacked;
        this.numDamage = numDamage;
        this.reachX = reachX;
        this.reachY = reachY;
        this.cooldownMs = cooldownMs;
        this.hitDelayMs = hitDelayMs;
        this.speed = speed;
        this.skillId = skillId;
        this.skillLevel = skillId == 0 ? 0 : SKILL_LEVEL;
        this.altSkillId = altSkillId;
    }

    // Level only identifies the skill to the viewer's client; the line/mob count is sent
    // in the packet. 20 is a valid level for every 1st-4th job attack skill.
    private static final int SKILL_LEVEL = 20;
    private static final int AOE_MOBS = 6;          // mobs an AoE swing hits
    private static final int MELEE_SPEED = 4;

    // ----- per-route factories (route defaults live here, not in the registry) -----
    public static BotAttackProfile melee(int skill, int lines) {
        return new BotAttackProfile(Route.CLOSE, 1, lines, 90, 60, 720, (short) 300, MELEE_SPEED, skill, 0);
    }

    public static BotAttackProfile meleeVar(int skill, int altSkill, int lines) {
        return new BotAttackProfile(Route.CLOSE, 1, lines, 90, 60, 720, (short) 300, MELEE_SPEED, skill, altSkill);
    }

    public static BotAttackProfile meleeAoe(int skill, int lines) {
        return new BotAttackProfile(Route.CLOSE, AOE_MOBS, lines, 130, 80, 780, (short) 320, MELEE_SPEED, skill, 0);
    }

    public static BotAttackProfile meleeAoeVar(int skill, int altSkill, int lines) {
        return new BotAttackProfile(Route.CLOSE, AOE_MOBS, lines, 130, 80, 780, (short) 320, MELEE_SPEED, skill, altSkill);
    }

    // A multi-target primary swing with a caller-set mob cap - a single-target skill that players
    // main for mobbing (Spear/Pole-Arm Crusher). Faster cooldown than the AoE factories since it's the
    // bread-and-butter attack, not an occasional pack-clear. Reach is a fallback only; melee reads the
    // skill's real WZ box when it has one.
    public static BotAttackProfile meleeMultiVar(int skill, int altSkill, int mobs, int lines) {
        return new BotAttackProfile(Route.CLOSE, mobs, lines, 130, 80, 720, (short) 300, MELEE_SPEED, skill, altSkill);
    }

    // A no-skill basic swing (skillId 0) - the plain melee attack a Beginner makes with a sword
    // before any job skills exist. Single target, one line, melee reach. The render path uses the
    // weapon's own swing keyframe (BotAttackData.actionFor) and the profile's reach box, since there
    // is no skill animation or WZ range to read.
    public static BotAttackProfile basicSwing() {
        return new BotAttackProfile(Route.CLOSE, 1, 1, 90, 60, 720, (short) 300, MELEE_SPEED, 0, 0);
    }

    public static BotAttackProfile ranged(int skill, int lines) {
        return new BotAttackProfile(Route.RANGED, 1, lines, 450, 150, 780, (short) 340, MELEE_SPEED, skill, 0);
    }

    public static BotAttackProfile rangedAoe(int skill, int lines) {
        return new BotAttackProfile(Route.RANGED, AOE_MOBS, lines, 450, 200, 820, (short) 360, MELEE_SPEED, skill, 0);
    }

    public static BotAttackProfile magic(int skill, int lines) {
        return new BotAttackProfile(Route.MAGIC, 1, lines, 450, 150, 840, (short) 360, MELEE_SPEED, skill, 0);
    }

    public static BotAttackProfile magicAoe(int skill, int lines) {
        return new BotAttackProfile(Route.MAGIC, AOE_MOBS, lines, 450, 210, 900, (short) 380, MELEE_SPEED, skill, 0);
    }

    /* This profile's skill for the given weapon - the alternate form when one is set and the weapon calls for it. */
    public int skillFor(org.gms.client.inventory.WeaponType weapon) {
        return (altSkillId != 0 && BotAttackData.usesAltWarriorVariant(weapon)) ? altSkillId : skillId;
    }

    /*
     * Roll one per-line damage for a bot of this job and level. The band comes from the bot's job
     * tier (beginner..4th job) with a gentle level ramp, and single/double-line swings concentrate
     * into a heavier per-line hit - all owned by BotDamageModel, the one place damage is tuned.
     */
    public int rollDamage(int level, Job job) {
        int tier = (job != null) ? job.getJobTier() : 0;
        return BotDamageModel.rollLine(tier, level, numDamage);
    }
}
