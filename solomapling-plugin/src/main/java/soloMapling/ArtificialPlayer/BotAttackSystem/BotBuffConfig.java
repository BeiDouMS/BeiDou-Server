package soloMapling.ArtificialPlayer.BotAttackSystem;

import org.gms.client.Job;
import org.gms.constants.skills.Archer;
import org.gms.constants.skills.Assassin;
import org.gms.constants.skills.Bandit;
import org.gms.constants.skills.Cleric;
import org.gms.constants.skills.Crossbowman;
import org.gms.constants.skills.FPWizard;
import org.gms.constants.skills.Fighter;
import org.gms.constants.skills.Hunter;
import org.gms.constants.skills.ILWizard;
import org.gms.constants.skills.Magician;
import org.gms.constants.skills.Page;
import org.gms.constants.skills.Spearman;
import org.gms.constants.skills.Warrior;
import org.gms.constants.skills.Crusader;
import org.gms.constants.skills.WhiteKnight;
import org.gms.constants.skills.DragonKnight;
import org.gms.constants.skills.Priest;
import org.gms.constants.skills.Hermit;
import org.gms.constants.skills.ChiefBandit;
import org.gms.constants.skills.Hero;
import org.gms.constants.skills.Paladin;
import org.gms.constants.skills.DarkKnight;
import org.gms.constants.skills.FPMage;
import org.gms.constants.skills.FPArchMage;
import org.gms.constants.skills.ILMage;
import org.gms.constants.skills.ILArchMage;
import org.gms.constants.skills.Bishop;
import org.gms.constants.skills.Bowmaster;
import org.gms.constants.skills.Marksman;
import org.gms.constants.skills.NightLord;
import org.gms.constants.skills.Shadower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/*
 * Bot-buff concept inspired by GreenCatMS; this per-job registry and the cosmetic (no stat/MP/cooldown) approach are original. Credit: NutNNut for the idea.
 * Per-job bot buff registry. Each entry lists only the buffs gained at that advancement;
 * a bot's full buff set is the union over every job in its lineage (so a Priest inherits
 * Magician + Cleric + Priest buffs). Keyed by job rather than class branch because a Cleric
 * and an I/L Wizard are both Magicians yet keep different buffs.
 */
public final class BotBuffConfig {

    // Each Job -> the buff skill ids it ADDS at that advancement (inherited ones
    // come from the lineage walk in buffsForJob). EnumMap iterates in enum-decl
    // order, so lower-tier buffs naturally resolve before higher-tier ones.
    private static final Map<Job, int[]> BUFFS_BY_JOB = new EnumMap<>(Job.class);

    static {
        // ---- 1st job ----
        put(Job.WARRIOR,  Warrior.IRON_BODY);                          // 1001003 - W.Def up
        put(Job.MAGICIAN, Magician.MAGIC_GUARD, Magician.MAGIC_ARMOR); // 2001002 / 2001003
        put(Job.BOWMAN,   Archer.FOCUS);                               // 3001003 - acc/avoid up
        // THIEF 1st job: only Dark Sight (hides the bot) -> intentionally none.
        // PIRATE 1st job: none yet.

        // ---- 2nd job (only what each job ADDS; 1st-job buffs come via lineage) ----
        // Warrior branch (+ weapon booster - attack speed up; the booster effect looks
        // the same regardless of weapon, so the representative one is fine for the visual)
        put(Job.FIGHTER,  Fighter.RAGE, Fighter.POWER_GUARD, Fighter.SWORD_BOOSTER);        // 1101006 (party atk), 1101007, 1101004
        put(Job.PAGE,     Page.POWER_GUARD, Page.SWORD_BOOSTER);                            // 1201007, 1201004
        put(Job.SPEARMAN, Spearman.IRON_WILL, Spearman.HYPER_BODY, Spearman.SPEAR_BOOSTER); // 1301006, 1301007 (party), 1301004
        // Magician branch (Spell Booster is 3rd job - see below)
        put(Job.FP_WIZARD, FPWizard.MEDITATION);                       // 2101001 (party m.atk)
        put(Job.IL_WIZARD, ILWizard.MEDITATION);                       // 2201001 (party m.atk)
        put(Job.CLERIC,    Cleric.BLESS, Cleric.INVINCIBLE);           // 2301004 (party), 2301003
        // Bowman branch (+ booster)
        put(Job.HUNTER,      Hunter.SOUL_ARROW, Hunter.BOW_BOOSTER);            // 3101004, 3101002
        put(Job.CROSSBOWMAN, Crossbowman.SOUL_ARROW, Crossbowman.CROSSBOW_BOOSTER); // 3201004, 3201002
        // Thief branch (+ booster)
        put(Job.ASSASSIN, Assassin.HASTE, Assassin.CLAW_BOOSTER);      // 4101004, 4101003
        put(Job.BANDIT,   Bandit.HASTE, Bandit.DAGGER_BOOSTER);        // 4201003, 4201002

        // ---- 3rd job ----
        // Warrior branch
        put(Job.CRUSADER,     Crusader.COMBO);                             // 1111002 - combo (self)
        put(Job.WHITEKNIGHT,  WhiteKnight.SWORD_FIRE_CHARGE);              // 1211003 - elemental charge (self)
        put(Job.DRAGONKNIGHT, DragonKnight.DRAGON_BLOOD);                  // 1311008 - atk up (self)
        // Magician branch (F/P and I/L get Spell Booster at 3rd job; Cleric line doesn't)
        put(Job.FP_MAGE,      FPMage.SPELL_BOOSTER);                       // 2111005 - cast speed up
        put(Job.IL_MAGE,      ILMage.SPELL_BOOSTER);                       // 2211005 - cast speed up
        put(Job.PRIEST,       Priest.HOLY_SYMBOL);                         // 2311003 - exp/drop (party)
        // Thief branch
        put(Job.HERMIT,       Hermit.SHADOW_PARTNER, Hermit.MESO_UP);      // 4111002 (self), 4111001 (party meso)
        put(Job.CHIEFBANDIT,  ChiefBandit.MESO_GUARD);                     // 4211005 - meso shield (self)
        // (Ranger/Sniper inherit Soul Arrow; F/P & I/L 3rd inherit Meditation)

        // ---- 4th job (Maple Warrior for everyone + each class's signature buff) ----
        put(Job.HERO,        Hero.MAPLE_WARRIOR, Hero.ENRAGE);             // 1121000 (party all-stat), 1121010 (self)
        put(Job.PALADIN,     Paladin.MAPLE_WARRIOR, Paladin.SWORD_HOLY_CHARGE); // 1221000 (party), 1221003 (self charge - fires after the inherited fire charge)
        put(Job.DARKKNIGHT,  DarkKnight.MAPLE_WARRIOR, DarkKnight.BERSERK);// 1321000 (party), 1320006 (self)
        put(Job.FP_ARCHMAGE, FPArchMage.MAPLE_WARRIOR, FPArchMage.INFINITY, FPArchMage.MANA_REFLECTION); // 2121000 (party), 2121004, 2121002 (self)
        put(Job.IL_ARCHMAGE, ILArchMage.MAPLE_WARRIOR, ILArchMage.INFINITY, ILArchMage.MANA_REFLECTION); // 2221000 (party), 2221004, 2221002 (self)
        put(Job.BISHOP,      Bishop.MAPLE_WARRIOR, Bishop.HOLY_SHIELD, Bishop.INFINITY, Bishop.MANA_REFLECTION); // 2321000, 2321005 (party), 2321004, 2321002 (self)
        put(Job.BOWMASTER,   Bowmaster.MAPLE_WARRIOR, Bowmaster.SHARP_EYES, Bowmaster.CONCENTRATE); // 3121000 (party), 3121002 (party crit), 3121008 (self atk)
        put(Job.MARKSMAN,    Marksman.MAPLE_WARRIOR, Marksman.SHARP_EYES);  // 3221000 (party), 3221002 (party crit)
        put(Job.NIGHTLORD,   NightLord.MAPLE_WARRIOR, NightLord.SHADOW_STARS); // 4121000 (party), 4121006 (self - infinite stars)
        put(Job.SHADOWER,    Shadower.MAPLE_WARRIOR);                      // 4221000 (party)  [Smoke Screen skipped]
    }

    private BotBuffConfig() {}

    private static void put(Job job, int... skillIds) {
        BUFFS_BY_JOB.put(job, skillIds);
    }

    /*
     * The full buff list for a bot of this job: the union of every configured job
     * in the bot's lineage (Job.isA(Job)), lower-tier first. Empty if the
     * job (and its ancestors) have nothing configured.
     */
    public static List<Integer> buffsForJob(Job job) {
        if (job == null) return Collections.emptyList();

        List<Integer> result = new ArrayList<>();
        for (Map.Entry<Job, int[]> entry : BUFFS_BY_JOB.entrySet()) {
            if (job.isA(entry.getKey())) {
                for (int id : entry.getValue()) {
                    result.add(id);
                }
            }
        }
        return result;
    }
}
