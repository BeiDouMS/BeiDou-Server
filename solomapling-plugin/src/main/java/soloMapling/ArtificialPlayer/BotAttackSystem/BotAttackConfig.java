package soloMapling.ArtificialPlayer.BotAttackSystem;

import org.gms.client.Job;
import org.gms.client.inventory.WeaponType;
import org.gms.constants.skills.Archer;
import org.gms.constants.skills.Bandit;
import org.gms.constants.skills.Bishop;
import org.gms.constants.skills.Bowmaster;
import org.gms.constants.skills.ChiefBandit;
import org.gms.constants.skills.Cleric;
import org.gms.constants.skills.Crossbowman;
import org.gms.constants.skills.Crusader;
import org.gms.constants.skills.DragonKnight;
import org.gms.constants.skills.FPArchMage;
import org.gms.constants.skills.FPMage;
import org.gms.constants.skills.FPWizard;
import org.gms.constants.skills.Hermit;
import org.gms.constants.skills.Hero;
import org.gms.constants.skills.Hunter;
import org.gms.constants.skills.ILArchMage;
import org.gms.constants.skills.ILMage;
import org.gms.constants.skills.ILWizard;
import org.gms.constants.skills.Magician;
import org.gms.constants.skills.Marksman;
import org.gms.constants.skills.NightLord;
import org.gms.constants.skills.Paladin;
import org.gms.constants.skills.Priest;
import org.gms.constants.skills.Ranger;
import org.gms.constants.skills.Rogue;
import org.gms.constants.skills.Shadower;
import org.gms.constants.skills.Sniper;
import org.gms.constants.skills.Warrior;
import org.gms.constants.skills.WhiteKnight;

import java.util.EnumMap;
import java.util.Map;

import static soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackProfile.magic;
import static soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackProfile.magicAoe;
import static soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackProfile.melee;
import static soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackProfile.meleeAoe;
import static soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackProfile.meleeAoeVar;
import static soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackProfile.meleeMultiVar;
import static soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackProfile.meleeVar;
import static soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackProfile.ranged;
import static soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackProfile.rangedAoe;

/*
 * Per-job attack registry is original; the bot-combat concept is inspired by GreenCatMS. Credit: NutNNut for the idea.
 * Per-job bot attack registry. Each job lists up to three attacks it introduces - a single-target
 * one, a sustained AoE it mobs with, and (for a few 3rd/4th-job classes) a throttled full-map
 * ultimate; a bot inherits the most advanced of each across its job lineage. The split matters at
 * runtime: while the ultimate (Dragon Roar / Genesis / Blizzard / Meteor Shower) is on its long
 * cooldown, the bot keeps mobbing with the sustained AoE instead of dropping to single-target
 * (BotAttackDriver's AUTO). Weapon only refines the result (projectile, warrior sword/axe &
 * spear/pole-arm forms); the one weapon-driven choice is the 1st-job rogue (claw vs dagger).
 * Beginners (job 0) and 1st-job pirates have no entry and fall through to a plain skill-0 weapon
 * swing (resolve's final fallback). Damage is fixed per line, no stat math.
 */
public final class BotAttackConfig {

    /*
     * A job's introduced attacks; any field may be null (meaning "inherit from the lineage").
     * single = single-target, aoe = sustained mobbing swing, ultimate = throttled full-map nuke.
     */
    public record JobAttacks(BotAttackProfile single, BotAttackProfile aoe, BotAttackProfile ultimate) {}

    private static final Map<Job, JobAttacks> BY_JOB = new EnumMap<>(Job.class);

    // 1st-job rogue basics - chosen by weapon, since the Thief job alone is ambiguous.
    private static final BotAttackProfile ROGUE_CLAW = ranged(Rogue.LUCKY_SEVEN, 2);
    private static final BotAttackProfile ROGUE_DAGGER = melee(Rogue.DOUBLE_STAB, 2);

    // Dragon/Dark Knight's main attack (spear or pole-arm form), used as both single and mob swing.
    private static final BotAttackProfile CRUSHER = meleeMultiVar(DragonKnight.SPEAR_CRUSHER, DragonKnight.POLE_ARM_CRUSHER, 3, 3);

    static {
        // ===== Warrior (melee) - 3rd/4th sword|axe and spear|pole-arm forms via *Var =====
        put(Job.WARRIOR,      melee(Warrior.POWER_STRIKE, 1),                              meleeAoe(Warrior.SLASH_BLAST, 1));
        put(Job.CRUSADER,     meleeVar(Crusader.SWORD_PANIC, Crusader.AXE_PANIC, 1),       meleeAoeVar(Crusader.SWORD_COMA, Crusader.AXE_COMA, 1));
        put(Job.WHITEKNIGHT,  melee(WhiteKnight.CHARGE_BLOW, 1),                           null); // inherits Slash Blast
        // Crusher is a single-target skill in v83, but players main it for mobbing, so the bot uses it as
        // its primary AoE (3 mobs, 3 lines) in both the single and aoe slots; Dragon Roar is the throttled
        // full-map ultimate. Bots are synthetic/visual, so the multi-target mob cap is a free choice here.
        put(Job.DRAGONKNIGHT, CRUSHER, CRUSHER, meleeAoe(DragonKnight.DRAGON_ROAR, 1));
        put(Job.HERO,         melee(Hero.BRANDISH, 2),                                     null); // inherits Coma
        put(Job.PALADIN,      melee(Paladin.BLAST, 1),                                     meleeAoe(Paladin.HEAVENS_HAMMER, 1));
        // Dark Knight: no new attack - inherits Crusher (single + mob) + Dragon Roar ultimate from Dragon Knight.

        // ===== Magician (magic) =====
        put(Job.MAGICIAN,     magic(Magician.MAGIC_CLAW, 2),  null);
        put(Job.FP_WIZARD,    magic(FPWizard.FIRE_ARROW, 1),  null);
        put(Job.IL_WIZARD,    magic(ILWizard.COLD_BEAM, 1),   magicAoe(ILWizard.THUNDERBOLT, 1));
        put(Job.CLERIC,       magic(Cleric.HOLY_ARROW, 1),    null);
        put(Job.FP_MAGE,      null,                           magicAoe(FPMage.EXPLOSION, 1)); // inherits Fire Arrow single
        put(Job.IL_MAGE,      magic(ILMage.THUNDER_SPEAR, 1),  magicAoe(ILMage.ICE_STRIKE, 1)); // single = Thunder Spear (lightning), AoE = Ice Strike
        put(Job.PRIEST,       null,                           magicAoe(Priest.SHINING_RAY, 1)); // inherits Holy Arrow single
        // 4th-job mages: the ultimate (Meteor Shower / Blizzard) is a throttled full-map nuke in its own
        // slot; the aoe slot is left null so it inherits the class's sustained mob spell (Explosion / Ice
        // Strike), which the bot keeps casting while the ultimate cools. Bishop likewise inherits Priest's
        // Shining Ray to mob with while Genesis is on cooldown.
        put(Job.FP_ARCHMAGE,  magic(FPArchMage.BIG_BANG, 1),  null, magicAoe(FPArchMage.METEOR_SHOWER, 1)); // inherits Explosion
        put(Job.IL_ARCHMAGE,  magic(ILArchMage.BIG_BANG, 1),  null, magicAoe(ILArchMage.BLIZZARD, 1));      // inherits Ice Strike
        put(Job.BISHOP,       magic(Bishop.ANGEL_RAY, 1),     null, magicAoe(Bishop.GENESIS, 1));           // inherits Shining Ray

        // ===== Bowman (ranged) - weapon picks the projectile (bow->arrow, crossbow->bolt) =====
        put(Job.BOWMAN,       ranged(Archer.DOUBLE_SHOT, 2),  null);
        put(Job.HUNTER,       null,                           rangedAoe(Hunter.ARROW_BOMB, 1));     // inherits Double Shot
        put(Job.CROSSBOWMAN,  null,                           rangedAoe(Crossbowman.IRON_ARROW, 1));
        put(Job.RANGER,       ranged(Ranger.STRAFE, 4),       rangedAoe(Ranger.ARROW_RAIN, 1));
        put(Job.SNIPER,       ranged(Sniper.STRAFE, 4),       rangedAoe(Sniper.ARROW_ERUPTION, 1));
        put(Job.BOWMASTER,    ranged(Bowmaster.HURRICANE, 1), null); // inherits Arrow Rain
        put(Job.MARKSMAN,     ranged(Marksman.SNIPE, 1),      null); // inherits Arrow Eruption

        // ===== Thief - claw (stars/ranged) vs dagger (melee); 1st-job rogue seeded in resolve =====
        put(Job.HERMIT,       null,                           rangedAoe(Hermit.AVENGER, 1));        // claw: inherits Lucky Seven
        put(Job.NIGHTLORD,    ranged(NightLord.TRIPLE_THROW, 3), null);                             // inherits Avenger
        put(Job.BANDIT,       melee(Bandit.SAVAGE_BLOW, 6),   null);
        put(Job.CHIEFBANDIT,  null,                           meleeAoe(ChiefBandit.BAND_OF_THIEVES, 1)); // single inherits Bandit's Savage Blow (Assaulter dropped); BoT attackCount=1 (was wrongly 6)
        put(Job.SHADOWER,     melee(Shadower.ASSASSINATE, 3), meleeAoe(Shadower.BOOMERANG_STEP, 2));
    }

    private BotAttackConfig() {}

    // ---- per-class critical-hit identity ----
    // Crit is a class trait, not a per-attack one: it mirrors the engine's job-gated canCrit
    // (AbstractDealDamageHandler:738) but graded - thieves/bowmen crit often, warriors seldom,
    // mages rarely. A crit only bumps the displayed/applied damage (and so the knockback); it's
    // combat feel, not balance, so tune these freely. critChanceFor resolves by job lineage, so
    // advanced jobs (Night Lord, Hero, Bishop, ...) inherit their branch's chance.
    private static final double CRIT_THIEF   = 0.50;
    private static final double CRIT_BOWMAN  = 0.50;
    private static final double CRIT_WARRIOR = 0.01;
    private static final double CRIT_MAGE    = 0.01;
    private static final double CRIT_DEFAULT = 0.01; // beginners / pirates / anything unlisted

    /* A crit shows ~this multiple of the rolled damage (bigger number -> stronger client knockback). */
    public static final double CRIT_MULTIPLIER = 1.5;

    /* This job's per-line critical-hit chance (0..1), by class branch. */
    public static double critChanceFor(Job job) {
        if (job == null) {
            return 0.0;
        }
        if (job.isA(Job.ASSASSIN) || job.isA(Job.HERMIT) || job.isA(Job.NIGHTLORD)) {
            return CRIT_THIEF;
        }
        if (job.isA(Job.BOWMAN)) {
            return CRIT_BOWMAN;
        }
        if (job.isA(Job.WARRIOR)) {
            return CRIT_WARRIOR;
        }
        if (job.isA(Job.MAGICIAN)) {
            return CRIT_MAGE;
        }
        return CRIT_DEFAULT;
    }

    // A job with no ultimate leaves that slot null; the two-profile overload keeps the many
    // single/aoe entries above unchanged.
    private static void put(Job job, BotAttackProfile single, BotAttackProfile aoe) {
        put(job, single, aoe, null);
    }

    private static void put(Job job, BotAttackProfile single, BotAttackProfile aoe, BotAttackProfile ultimate) {
        BY_JOB.put(job, new JobAttacks(single, aoe, ultimate));
    }

    /*
     * The bot's effective {single, aoe, ultimate} attacks: the most advanced of each across its
     * lineage (highest job id wins, which is also the highest tier within a branch). Because the
     * three roles resolve independently, a class can carry a mid-tier mob spell inherited from an
     * earlier job (Bishop's Shining Ray, the arch-mages' Ice Strike / Explosion) alongside the
     * ultimate it introduces itself. 1st-job rogues get their basic seeded from the weapon. Any
     * field may be null.
     */
    public static JobAttacks resolve(Job job, WeaponType weapon) {
        if (job == null) {
            return new JobAttacks(null, null, null);
        }

        BotAttackProfile single = null, aoe = null, ultimate = null;
        int bestSingleId = -1, bestAoeId = -1, bestUltId = -1;
        for (Map.Entry<Job, JobAttacks> entry : BY_JOB.entrySet()) {
            Job j = entry.getKey();
            if (!job.isA(j)) {
                continue;
            }
            JobAttacks a = entry.getValue();
            if (a.single() != null && j.getId() > bestSingleId) {
                single = a.single();
                bestSingleId = j.getId();
            }
            if (a.aoe() != null && j.getId() > bestAoeId) {
                aoe = a.aoe();
                bestAoeId = j.getId();
            }
            if (a.ultimate() != null && j.getId() > bestUltId) {
                ultimate = a.ultimate();
                bestUltId = j.getId();
            }
        }

        // 1st-job rogues aren't in the table: their weapon decides the basic attack. Advanced
        // thief jobs are weapon-committed and registered, so this only fills the unadvanced gap.
        if (single == null && job.isA(Job.THIEF)) {
            single = (weapon == WeaponType.CLAW) ? ROGUE_CLAW : ROGUE_DAGGER;
        }

        // Beginners (job 0) and any job with no registered attack (e.g. a 1st-job pirate) fall back
        // to a plain skill-0 weapon swing. This is what lets a sub-level-10 beginner bot fight with
        // the sword the decorator gives it - the basic attack it would make before any job skills.
        if (single == null && aoe == null && ultimate == null) {
            single = BotAttackProfile.basicSwing();
        }
        return new JobAttacks(single, aoe, ultimate);
    }
}
