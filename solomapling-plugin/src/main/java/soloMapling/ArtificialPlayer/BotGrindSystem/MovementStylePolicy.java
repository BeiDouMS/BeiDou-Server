package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;

// Maps a bot's job to how it moves while grinding. Gated on job (which implies job-tier/level), so low-level
// bots stay PLANTED (slow = organic) and only advanced classes blink/dash. Ours.
public final class MovementStylePolicy {
    private MovementStylePolicy() {
    }

    public static MovementStyle forBot(Character bot) {
        if (bot == null || bot.getJob() == null) {
            return MovementStyle.PLANTED;
        }
        int id = bot.getJob().getId();
        if (isTeleportMage(id)) {
            return MovementStyle.TELEPORT;
        }
        if (id == 411 || id == 412) { // Hermit, Night Lord
            return MovementStyle.FLASH_JUMP;
        }
        if (id == 410 || id == 420 || id == 421 || id == 422) { // Assassin, Bandit, Chief Bandit, Shadower (haste thieves)
            return MovementStyle.JUMP_ATTACK;
        }
        if (isRangedBowman(id)) {
            return MovementStyle.RANGED;
        }
        return MovementStyle.PLANTED;
    }

    // Bowman 2nd job and above: Hunter/Ranger/Bowmaster (310-312), Crossbowman/Sniper/Marksman (320-322).
    // 1st-job Bowman (300) stays PLANTED so low-levels read organic.
    private static boolean isRangedBowman(int id) {
        return (id >= 310 && id <= 312) || (id >= 320 && id <= 322);
    }

    // Magician 2nd job and above (base Magician 200 has no Teleport): 210-212 F/P, 220-222 I/L, 230-232 Cleric.
    private static boolean isTeleportMage(int id) {
        return (id >= 210 && id <= 212) || (id >= 220 && id <= 222) || (id >= 230 && id <= 232);
    }
}
