package soloMapling.ArtificialPlayer.BotFlavorSystem;

import org.gms.client.Character;
import org.gms.client.inventory.WeaponType;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackConfig;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackProfile;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotBuffConfig;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotBuffEffects;
import soloMapling.ArtificialPlayer.BotCommandsPack.BotAttack;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;

// The idle-expression layer: makes an unoccupied town/social bot occasionally DO something (emote,
// flex a buff, swing a skill at the air) so it doesn't freeze into an image object between scripted
// exchanges. Entry point is maybeExpress(bot), called from a bot's existing idle tick - no new thread,
// no new ticker. Everything here is one bot deciding to do one purely-visual thing, hard-gated on a
// real player being able to see it. The illusion of a living crowd emerges from many independent
// fidgeters, not from any coordination.
//
// Skill/buff selection reuses the existing per-job registries (BotAttackConfig / BotBuffConfig) rather
// than re-listing skills, so a bot flexes exactly what its job would own.
public final class BotFlavor {

    private BotFlavor() {}

    // ---- tuning knobs (candidates for a live !env flavor readout) ----
    // Per eligible observed tick, the chance to express. With 2-6s observed ticks this lands a first
    // expression within ~10s of a player arriving, then the cooldown keeps it occasional.
    public static volatile double FLAVOR_CHANCE = 0.35;
    public static volatile long FLAVOR_COOLDOWN_MIN_MS = 15_000;
    public static volatile long FLAVOR_COOLDOWN_MAX_MS = 40_000;

    // Friendly/neutral face ids (F-keys 1..7). We deliberately skip the sour ones by default so idle
    // townsfolk read as pleasant; widen this if we want moodier crowds.
    private static final int[] FRIENDLY_EMOTES = {1, 2, 5, 6};

    // Min spacing between one bot's expressions. Keyed by character id (bots are only ever ticked one
    // at a time by the wheel, but the map is shared across all bots, so keep it concurrent).
    private static final Map<Integer, Long> cooldownUntil = new ConcurrentHashMap<>();

    // Called from an idle/available bot tick. Rolls the gates, and if they pass, performs one weighted
    // expression and arms the cooldown. Cheap early-return when unobserved - flavor is packets, and we
    // gate packets, never simulation.
    public static void maybeExpress(BotSM bot) {
        if (bot == null) {
            return;
        }
        Character chr = bot.getChr();
        if (chr == null || chr.getMap() == null) {
            return;
        }
        // Hard gate: only ever express when a real player is actually on the map to see it.
        if (!GCMovement.isMapObserved(chr.getMapId())) {
            return;
        }

        long now = System.currentTimeMillis();
        Long until = cooldownUntil.get(chr.getId());
        if (until != null && now < until) {
            return;
        }
        if (ThreadLocalRandom.current().nextDouble() >= FLAVOR_CHANCE) {
            return; // rolled a quiet tick; try again next tick (no cooldown consumed)
        }

        dispatch(chr, pickAction());

        long span = Math.max(1, FLAVOR_COOLDOWN_MAX_MS - FLAVOR_COOLDOWN_MIN_MS);
        long cd = FLAVOR_COOLDOWN_MIN_MS + (long) (ThreadLocalRandom.current().nextDouble() * span);
        cooldownUntil.put(chr.getId(), now + cd);
    }

    // Drop a bot's cooldown record when it's torn down / converted, so the map doesn't leak entries.
    public static void forget(Character chr) {
        if (chr != null) {
            cooldownUntil.remove(chr.getId());
        }
    }

    // Test entry (!test flavor): perform one expression immediately, skipping the observed / cooldown /
    // chance gates. Lets a GM force the whole crowd to react on demand.
    public static void forceExpress(BotSM bot) {
        forceExpress(bot, pickAction());
    }

    // Test entry (!test flavor <action>): force one specific action immediately.
    public static void forceExpress(BotSM bot, FlavorAction action) {
        if (bot == null || action == null) {
            return;
        }
        Character chr = bot.getChr();
        if (chr == null || chr.getMap() == null) {
            return;
        }
        dispatch(chr, action);
    }

    private static FlavorAction pickAction() {
        int total = 0;
        for (FlavorAction a : FlavorAction.values()) {
            total += a.weight;
        }
        int roll = ThreadLocalRandom.current().nextInt(total);
        for (FlavorAction a : FlavorAction.values()) {
            roll -= a.weight;
            if (roll < 0) {
                return a;
            }
        }
        return FlavorAction.EMOTE;
    }

    private static void dispatch(Character chr, FlavorAction action) {
        switch (action) {
            case EMOTE -> doEmote(chr);
            case BUFF_FLEX -> doBuffFlex(chr);
            case SKILL_SWING -> doSkillSwing(chr);
        }
    }

    private static void doEmote(Character chr) {
        int id = FRIENDLY_EMOTES[ThreadLocalRandom.current().nextInt(FRIENDLY_EMOTES.length)];
        BotEmote(chr, id);
    }

    // Flex a self-buff the bot's job would own (Iron Body, Haste, Meditation, ...). Pure VFX - the
    // cast animation + aura, no stats. Falls back to an emote for jobs with no configured buffs
    // (beginners / early pirates).
    private static void doBuffFlex(Character chr) {
        List<Integer> buffs = BotBuffConfig.buffsForJob(chr.getJob());
        if (buffs.isEmpty()) {
            doEmote(chr);
            return;
        }
        int skillId = buffs.get(ThreadLocalRandom.current().nextInt(buffs.size()));
        BotBuffEffects.showBuff(chr, skillId);
    }

    // Swing one of the bot's job attack skills at the air (Brandish, an AoE, a spell cast). No targets,
    // no damage. markAlerted keeps the pose from being cancelled by the next idle-refresh packet.
    private static void doSkillSwing(Character chr) {
        WeaponType weapon = BotAttack.resolveEquippedWeaponType(chr);
        BotAttackConfig.JobAttacks atk = BotAttackConfig.resolve(chr.getJob(), weapon);
        BotAttackProfile profile = pickProfile(atk);
        GCMovement.markAlerted(chr);
        if (profile == null) {
            BotAttack.basicSwing(chr);
            return;
        }
        int skillId = profile.skillFor(weapon);
        if (profile.route == BotAttackProfile.Route.MAGIC) {
            BotAttack.magicSwing(chr, skillId);
        } else {
            // CLOSE and RANGED both render through the close-range pose path; skillSwing falls back to
            // a plain weapon swing when the skill id is 0.
            BotAttack.skillSwing(chr, skillId);
        }
    }

    private static BotAttackProfile pickProfile(BotAttackConfig.JobAttacks atk) {
        List<BotAttackProfile> options = new ArrayList<>();
        if (atk.single() != null) {
            options.add(atk.single());
        }
        if (atk.aoe() != null) {
            options.add(atk.aoe());
        }
        if (atk.ultimate() != null) {
            options.add(atk.ultimate());
        }
        if (options.isEmpty()) {
            return null;
        }
        return options.get(ThreadLocalRandom.current().nextInt(options.size()));
    }
}
