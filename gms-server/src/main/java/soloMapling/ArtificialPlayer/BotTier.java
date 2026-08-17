package soloMapling.ArtificialPlayer;

/**
 * Represents bot performance tiers with ordering and utility methods.
 * Tiers are ordered from highest (S) to lowest (D).
 */
public enum BotTier {
    S(5, "Elite"),      // Top tier, exceptional performance
    A(4, "High"),       // High tier, very good performance
    B(3, "Above Average"), // Above average tier
    C(2, "Average"),    // Average tier
    D(1, "Below Average"); // Below average tier

    private final int value;
    private final String description;

    BotTier(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }



    /**
     * Checks if this tier is at least as good as the specified minimum tier.
     * @param minimumTier the minimum tier to compare against
     * @return true if this tier is >= minimumTier
     */
    public boolean isAtLeast(BotTier minimumTier) {
        return this.value >= minimumTier.value;
    }

    /**
     * Checks if this tier is better than the specified tier.
     * @param otherTier the tier to compare against
     * @return true if this tier is better than otherTier
     */
    public boolean isBetterThan(BotTier otherTier) {
        return this.value > otherTier.value;
    }

    /**
     * Checks if this tier is worse than the specified tier.
     * @param otherTier the tier to compare against
     * @return true if this tier is worse than otherTier
     */
    public boolean isWorseThan(BotTier otherTier) {
        return this.value < otherTier.value;
    }

    /**
     * Returns a default tier if this tier is below the minimum threshold.
     * @param minimumTier the minimum acceptable tier
     * @param defaultTier the tier to return if below minimum
     * @return this tier if >= minimum, otherwise the default tier
     */
    public BotTier orDefault(BotTier minimumTier, BotTier defaultTier) {
        return this.isAtLeast(minimumTier) ? this : defaultTier;
    }

    /**
     * Returns a boosted tier if this tier is below the threshold.
     * Common use case: boost D tier to B tier.
     * @param threshold the tier threshold to check against
     * @param boostTo the tier to boost to if below threshold
     * @return this tier if >= threshold, otherwise the boosted tier
     */
    public BotTier boostIfBelow(BotTier threshold, BotTier boostTo) {
        return this.isAtLeast(threshold) ? this : boostTo;
    }

    /**
     * Gets the next higher tier, or returns the same tier if already at maximum.
     * @return the next higher tier
     */
    public BotTier getNextHigherTier() {
        switch (this) {
            case D: return C;
            case C: return B;
            case B: return A;
            case A: return S;
            case S: return S; // Already at top
            default: return this;
        }
    }

    /**
     * Gets the next lower tier, or returns the same tier if already at minimum.
     * @return the next lower tier
     */
    public BotTier getNextLowerTier() {
        switch (this) {
            case S: return A;
            case A: return B;
            case B: return C;
            case C: return D;
            case D: return D; // Already at bottom
            default: return this;
        }
    }

    /**
     * Calculates the tier difference between this and another tier.
     * Positive values mean this tier is higher, negative means lower.
     * @param otherTier the tier to compare against
     * @return the difference in tier values
     */
    public int getTierDifference(BotTier otherTier) {
        return this.value - otherTier.value;
    }

    /**
     * Creates a BotTier from a string representation.
     * @param tierString the string representation ("S", "A", "B", "C", "D")
     * @return the corresponding BotTier, or null if invalid
     */
    public static BotTier fromString(String tierString) {
        try {
            return BotTier.valueOf(tierString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Gets the default tier for new characters.
     * @return the default tier (C - Average)
     */
    public static BotTier getDefaultTier() {
        return S;
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }

    /**
     * Utility class for managing BotTier operations on characters.
     * Use this to avoid modifying existing Character.java code extensively.
     */
    public static class TierManager {

        /**
         * Safely sets a tier, using default if null is provided.
         * @param currentTier the current tier value
         * @param newTier the new tier to set
         * @return the tier to set (newTier or default if null)
         */
        public static BotTier safeTierSet(BotTier currentTier, BotTier newTier) {
            return newTier != null ? newTier : getDefaultTier();
        }

        /**
         * Gets the effective tier with boost applied.
         * @param currentTier the current tier
         * @return boosted tier if below threshold, otherwise current tier
         */
        public static BotTier getEffectiveTier(BotTier currentTier) {
            if (currentTier == null) return getDefaultTier();
            // Boost D tier to B tier for better gameplay experience
            return currentTier.boostIfBelow(BotTier.C, BotTier.B);
        }

        /**
         * Checks if a character can perform an action based on tier requirement.
         * @param currentTier the character's current tier
         * @param requiredTier the minimum tier required
         * @return true if character's tier meets the requirement
         */
        public static boolean canPerformAction(BotTier currentTier, BotTier requiredTier) {
            if (currentTier == null || requiredTier == null) return false;
            return currentTier.isAtLeast(requiredTier);
        }

        /**
         * Upgrades a tier by one level.
         * @param currentTier the current tier
         * @return the upgraded tier
         */
        public static BotTier upgradeTier(BotTier currentTier) {
            return currentTier != null ? currentTier.getNextHigherTier() : getDefaultTier();
        }

        /**
         * Downgrades a tier by one level.
         * @param currentTier the current tier
         * @return the downgraded tier
         */
        public static BotTier downgradeTier(BotTier currentTier) {
            return currentTier != null ? currentTier.getNextLowerTier() : getDefaultTier();
        }

        /**
         * Gets a safe tier value, returning default if null.
         * @param tier the tier to check
         * @return the tier or default if null
         */
        public static BotTier getSafeTier(BotTier tier) {
            return tier != null ? tier : getDefaultTier();
        }
    }

}