package soloMapling.ArtificialPlayer.BotCommandsPack;

import org.gms.client.Character;
import org.gms.client.Skill;
import org.gms.client.SkillFactory;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.WeaponType;
import org.gms.server.ItemInformationProvider;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackData;
import org.gms.util.PacketCreator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.facingLeft;

/**
 * Server-side attack animation helper for bots. 
 * Credit to NuTNNuT for attack animation values and code reference
 *
 * Bots have no real client, so they don't generate close-range-damage packets
 * the way players do. This helper synthesizes the broadcast packet directly so
 * other clients see the swing animation. We do NOT couple this to any actual
 * damage application — when the swing should also hit a reactor or monster,
 * the caller invokes the relevant logic separately (e.g. CustomReactor.hitReactor).
 *
 * Packet byte semantics in this Cosmic build (verified against AbstractDealDamageHandler.parseDamage):
 *   direction byte = body action id from Character/00002000.img (e.g. swingO1 = 5, swingP1 = 13)
 *   stance    byte = facing mask (0x80 = facing left, 0x00 = facing right)
 *   display   byte = 0 for a basic (non-skill) attack
 *   speed     byte = weapon attackSpeed (2..9; lower is faster). 4 is a safe default.
 *
 * The body-action id MUST match the equipped weapon class — a 1H swingO1 on a polearm
 * renders nothing. {@link BotAttackData#randomActionFor} picks the right variant.
 */
public final class BotAttack {

    /** Equip slot id for the main-hand weapon in v83. */
    private static final short EQUIP_SLOT_WEAPON = -11;

    private BotAttack() {}

    /**
     * Broadcast a basic Ctrl-attack swing animation. Pure visual; no damage,
     * no targets, no skill. Caller is responsible for any follow-up damage
     * application (e.g. reactor hit). The animation is selected based on the
     * bot's currently equipped weapon class.
     */
    public static void basicSwing(Character chr) {
        if (chr == null) return;

        int facingMask = facingLeft(chr) ? BotAttackData.FACING_LEFT_MASK : BotAttackData.FACING_RIGHT_MASK;
        WeaponType weaponType = resolveEquippedWeaponType(chr);
        int bodyActionId = BotAttackData.randomActionFor(weaponType);

        Map<Integer, List<Integer>> emptyTargets = Collections.emptyMap();
        chr.getMap().broadcastMessage(
                chr,
                PacketCreator.closeRangeAttack(
                        chr,
                        /* skill        */ 0,
                        /* skilllevel   */ 0,
                        /* stance       */ facingMask,
                        /* numAttackedAndDamage */ 0,
                        /* targets      */ emptyTargets,
                        /* speed        */ BotAttackData.DEFAULT_ATTACK_SPEED,
                        /* direction    */ bodyActionId,
                        /* display      */ 0
                ),
                /* repeatToSource */ false
        );
    }

    /**
     * Broadcast a cosmetic SKILL swing at the air - the character plays the skill's attack pose
     * (Brandish, Savage Blow, etc.) with no targets and no damage. Same packet the real melee path
     * builds (BotAttackEffects.meleeStrike) but with an empty target map, so nothing is hit. The
     * body-action id comes from the skill's own keyframe (BotAttackData.actionFor), falling back to
     * the weapon default. Pure visual. For magic-class skills use {@link #magicSwing} instead.
     */
    public static void skillSwing(Character chr, int skillId) {
        if (chr == null || chr.getMap() == null) return;
        if (skillId <= 0) { basicSwing(chr); return; }

        int facingMask = facingLeft(chr) ? BotAttackData.FACING_LEFT_MASK : BotAttackData.FACING_RIGHT_MASK;
        WeaponType weaponType = resolveEquippedWeaponType(chr);
        int bodyActionId = BotAttackData.actionFor(skillId, weaponType);

        Map<Integer, List<Integer>> emptyTargets = Collections.emptyMap();
        chr.getMap().broadcastMessage(
                chr,
                PacketCreator.closeRangeAttack(chr, skillId, resolveSkillLevel(skillId), facingMask,
                        /* numAttackedAndDamage */ 0, emptyTargets, BotAttackData.DEFAULT_ATTACK_SPEED,
                        bodyActionId, /* display */ 0),
                false
        );
    }

    /**
     * Broadcast a cosmetic MAGIC skill cast at the air - the character plays the spell cast with no
     * targets and no damage. Carries the charge int (BotAttackData.magicChargeFor); omitting it on a
     * keydown CHARGE skill (Big Bang) over-reads the packet and CRASHES viewers, so it must be sent.
     * Pure visual.
     */
    public static void magicSwing(Character chr, int skillId) {
        if (chr == null || chr.getMap() == null || skillId <= 0) return;

        int facingMask = facingLeft(chr) ? BotAttackData.FACING_LEFT_MASK : BotAttackData.FACING_RIGHT_MASK;
        WeaponType weaponType = resolveEquippedWeaponType(chr);
        int bodyActionId = BotAttackData.actionFor(skillId, weaponType);

        Map<Integer, List<Integer>> emptyTargets = Collections.emptyMap();
        chr.getMap().broadcastMessage(
                chr,
                PacketCreator.magicAttack(chr, skillId, resolveSkillLevel(skillId), facingMask,
                        /* numAttackedAndDamage */ 0, emptyTargets, BotAttackData.magicChargeFor(skillId),
                        BotAttackData.DEFAULT_ATTACK_SPEED, bodyActionId, /* display */ 0),
                false
        );
    }

    // The skill's max level, so the fanciest form of the effect renders. Bots don't learn skills, so
    // there's no real level to read; 1 is a safe fallback if the skill can't be resolved.
    private static int resolveSkillLevel(int skillId) {
        Skill skill = SkillFactory.getSkill(skillId);
        return skill != null ? skill.getMaxLevel() : 1;
    }

    /** The bot's currently equipped main-hand weapon class, or null if unarmed. */
    public static WeaponType resolveEquippedWeaponType(Character chr) {
        Item weapon = chr.getInventory(InventoryType.EQUIPPED).getItem(EQUIP_SLOT_WEAPON);
        if (weapon == null) return null;
        return ItemInformationProvider.getInstance().getWeaponType(weapon.getItemId());
    }
}
