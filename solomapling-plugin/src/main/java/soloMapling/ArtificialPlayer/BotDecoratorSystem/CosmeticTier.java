package soloMapling.ArtificialPlayer.BotDecoratorSystem;

/**
 * Represents the desirability/quality tier of a cosmetic item (hair, eyes, etc.).
 * Separate from BotTier — bot tier describes the player archetype,
 * cosmetic tier describes how fashionable/desirable the look is.
 *
 * 3 tiers for hair/eyes. Equipment can define its own tier scale (e.g. 4 tiers)
 * since each cosmetic category independently defines what makes sense.
 */
public enum CosmeticTier {
    PREMIUM,   // Popular, stylish — cash shop exclusive looks
    STANDARD,  // Solid, normal — common salon results
    BASIC      // Starter/default — character creation defaults
}
