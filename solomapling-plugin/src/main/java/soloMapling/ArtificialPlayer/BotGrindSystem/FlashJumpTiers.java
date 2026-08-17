package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;

// Level tier for the flash-jump dash: how much of the full (maxed) dash a bot may use. Skill-authentic
// flavor knob — a Hermit still leveling Flash Jump dashes visibly shorter than a Night Lord — resolved
// once per grind episode (GrindBrain.start) and passed into GCMovement.flashJump as the scale cap.
// The per-jump platform fit (GCMovementSkills) may downshift below this cap; it never exceeds it.
// Policy-level like MovementStylePolicy, so it lives here, not in the GC package. Ours (SoloMapling).
public final class FlashJumpTiers {

    private FlashJumpTiers() {
    }

    private static final int HERMIT = 411;
    private static final int NIGHT_LORD = 412;
    private static final int MAXED_LEVEL = 100;       // Hermit at/above this = maxed Flash Jump
    private static final float DEVELOPING_SCALE = 0.75f; // Hermit 70-99: still leveling the skill

    // The dash-scale cap for this bot: Night Lord (and Hermit 100+) = full dash, younger Hermit = 0.75.
    // Non-FLASH_JUMP jobs return 1 (harmless — only FLASH_JUMP-style bots ever dash).
    public static float capFor(Character bot) {
        if (bot == null || bot.getJob() == null) {
            return 1f;
        }
        int id = bot.getJob().getId();
        if (id == HERMIT) {
            return bot.getLevel() >= MAXED_LEVEL ? 1f : DEVELOPING_SCALE;
        }
        return 1f; // Night Lord, or any other caller
    }
}
