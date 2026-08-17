package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;
import org.gms.client.Job;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.net.server.world.Party;
import org.gms.net.server.world.PartyCharacter;
import org.gms.server.ItemInformationProvider;
import org.gms.server.maps.FieldLimit;
import org.gms.server.maps.MapleMap;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

// Ported from GreenCatMS. Credit: NutNNut.
record BotMovementProfile(int totalSpeedStat, int totalJumpStat, boolean snowShoes)
        implements Serializable {
    // Serialized inside cached BotNavigationGraph instances; keep explicit so
    // cache compatibility is controlled by GRAPH_VERSION instead of compiler-generated UIDs.
    @Serial
    private static final long serialVersionUID = 1L;

    static final int BASE_TOTAL_STAT = 100;
    static final int STAT_BUCKET_SIZE = 5;
    static final int MAX_EFFECTIVE_SPEED_STAT = 200;
    static final int MAX_EFFECTIVE_JUMP_STAT = 123;
    static final BotMovementProfile BASE = new BotMovementProfile(BASE_TOTAL_STAT, BASE_TOTAL_STAT);

    // Class-aware "effective Haste". Bots' Haste buff is cosmetic (no stat), so thief speed/jump is
    // modelled here: haste-thieves ramp to the cap early, and a bot sharing a party with a high-level
    // haste-thief inherits max speed. See Documents/Movement - 2026-07-06 Class-Aware Speed and Jump.
    static final int HASTE_MAX_SPEED = 155;                    // our effective speed cap (≈ real 140% held-down feel)
    static final int HASTE_MAX_JUMP = MAX_EFFECTIVE_JUMP_STAT; // 123
    static final int YOUNG_THIEF_SPEED = 150;                  // 2nd-job thief (30-54): hasted, not yet capped
    static final int YOUNG_THIEF_JUMP = 115;
    static final int HASTE_SELF_MAX_LEVEL = 55;                // haste-thief at/above this = max speed+jump
    static final int PARTY_HASTE_THIEF_LEVEL = 60;             // a haste-thief party member at/above this grants party Haste

    BotMovementProfile {
        totalSpeedStat = bucketStat(totalSpeedStat);
        totalJumpStat = bucketStat(totalJumpStat);
        totalSpeedStat = Math.min(totalSpeedStat, MAX_EFFECTIVE_SPEED_STAT);
        totalJumpStat = Math.min(totalJumpStat, MAX_EFFECTIVE_JUMP_STAT);
    }

    BotMovementProfile(int totalSpeedStat, int totalJumpStat) {
        this(totalSpeedStat, totalJumpStat, false);
    }

    static BotMovementProfile base() {
        return BASE;
    }

    static BotMovementProfile fromCharacter(Character character) {
        if (character == null) {
            return BASE;
        }
        if (hasForcedBaseMovementStats(character)) {
            return BASE;
        }
        // SoloMapling: scale the walk-speed / jump baseline by bot level AND class so higher-level and
        // Haste-class (thief) bots roam faster and jump higher. The baseline replaces the flat 100; any
        // equip/real-buff speed/jump (getTotal*Stat - 100) still stacks on top. Bucketed to the nearest 5
        // by the canonical constructor, so the tiers below land on exact graph buckets.
        int level = character.getLevel();
        boolean hasteThief = hasHasteSkill(character.getJob());

        int speedBaseline;
        int jumpBaseline;
        if (hasteThief && level >= HASTE_SELF_MAX_LEVEL) {
            speedBaseline = HASTE_MAX_SPEED;   // capped thief: full effective Haste
            jumpBaseline = HASTE_MAX_JUMP;
        } else if (hasteThief) {
            speedBaseline = YOUNG_THIEF_SPEED; // 2nd-job thief (30-54): Haste in effect, ramping to cap
            jumpBaseline = YOUNG_THIEF_JUMP;
        } else {
            speedBaseline = levelSpeedStat(level);
            jumpBaseline = levelJumpStat(level);
        }
        // Party Haste (immersion): a bot partied with a high-level haste-thief inherits max SPEED (not
        // jump). Pure roster logic — the thief never travels to cast it. max() so a young thief still gets 155.
        if (partyGrantsHaste(character)) {
            speedBaseline = Math.max(speedBaseline, HASTE_MAX_SPEED);
        }

        int totalSpeed = speedBaseline + (character.getTotalMoveSpeedStat() - BASE_TOTAL_STAT);
        int totalJump = jumpBaseline + (character.getTotalJumpStat() - BASE_TOTAL_STAT);
        return new BotMovementProfile(totalSpeed, totalJump, wearsSnowShoes(character));
    }

    // Haste-thief lineage: Assassin→Hermit→Night Lord and Bandit→Chief Bandit→Shadower all learn Haste at
    // 2nd job (level 30+). isA covers the whole lineage and excludes first-job Rogue (no Haste).
    private static boolean hasHasteSkill(Job job) {
        return job != null && (job.isA(Job.ASSASSIN) || job.isA(Job.BANDIT));
    }

    // True if the bot shares an active party with an online, level-PARTY_HASTE_THIEF_LEVEL+ haste-thief —
    // the bot is treated as receiving that thief's party Haste. Roster-only; no map/proximity requirement.
    private static boolean partyGrantsHaste(Character character) {
        Party party = character.getParty();
        if (party == null) {
            return false;
        }
        for (PartyCharacter member : party.getMembers()) {
            if (member == null || member.getId() == character.getId() || !member.isOnline()) {
                continue;
            }
            if (member.getLevel() >= PARTY_HASTE_THIEF_LEVEL && hasHasteSkill(member.getJob())) {
                return true;
            }
        }
        return false;
    }

    // Level -> walk-speed % baseline. Values are multiples of STAT_BUCKET_SIZE so they land exactly
    // on graph buckets and stay under MAX_EFFECTIVE_SPEED_STAT.
    private static int levelSpeedStat(int level) {
        if (level <= 9) {
            return 115;
        }
        if (level <= 29) {
            return 125;
        }
        if (level <= 50) {
            return 130;
        }
        if (level <= 69) {
            return 135;
        }
        if (level <= 100) {
            return 145;
        }
        return 155;
    }

    // Level -> jump % baseline (non-thief). Parallels levelSpeedStat within the 100..123 jump range so
    // higher-level bots also leap higher; ≤9 stays 100 so low-level bots are unchanged. Multiples of
    // STAT_BUCKET_SIZE (except the 123 cap) so they land on exact graph buckets.
    private static int levelJumpStat(int level) {
        if (level <= 9) {
            return 100;
        }
        if (level <= 29) {
            return 105;
        }
        if (level <= 50) {
            return 110;
        }
        if (level <= 69) {
            return 115;
        }
        if (level <= 100) {
            return 120;
        }
        return HASTE_MAX_JUMP; // 123
    }

    /* Snowshoes carry WZ info/fs (e.g. 10) on the worn shoe and cancel field
     *  slipperiness client-side — the wearer gets normal walk physics on snow/ice maps. */
    private static boolean wearsSnowShoes(Character character) {
        try {
            Item shoe = character.getInventory(InventoryType.EQUIPPED).getItem((short) -7);
            if (shoe == null) {
                return false;
            }
            Map<String, Integer> stats =
                    ItemInformationProvider.getInstance().getEquipStats(shoe.getItemId());
            return stats != null && stats.getOrDefault("fs", 0) >= 1;
        } catch (Throwable t) {
            return false; // WZ/equip data unavailable (unit tests, partial mocks)
        }
    }

    private static boolean hasForcedBaseMovementStats(Character character) {
        MapleMap map = character.getMap();
        return map != null && FieldLimit.MOVEMENTSKILLS.check(map.getFieldLimit());
    }

    private static int bucketStat(int stat) {
        int clamped = Math.max(1, stat);
        if (clamped < STAT_BUCKET_SIZE) {
            return clamped;
        }
        // Nearest bucket, not floor: a 144% bot plays on the 145 graph — halves the
        // worst-case drift between real stats and the physics/graph profile.
        return (int) (Math.round(clamped / (double) STAT_BUCKET_SIZE) * STAT_BUCKET_SIZE);
    }

    double speedMultiplier() {
        return totalSpeedStat / (double) BASE_TOTAL_STAT;
    }

    double jumpMultiplier() {
        return totalJumpStat / (double) BASE_TOTAL_STAT;
    }

    double walkVelocityPxs() {
        return BotMovementManager.cfg.WALK_VEL * speedMultiplier();
    }

    double hForcePxs() {
        return BotPhysicsEngine.cfg.HFORCE_PXS * speedMultiplier();
    }

    float jumpSpeedPxs() {
        return (float) (BotPhysicsEngine.cfg.JUMP_SPEED_PXS * jumpMultiplier());
    }

    float ropeJumpSpeedPxs() {
        return (float) (BotPhysicsEngine.cfg.JUMP_ROPE_PXS * jumpMultiplier());
    }
}
