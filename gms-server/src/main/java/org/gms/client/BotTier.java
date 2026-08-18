package org.gms.client;

/**
 * Bot performance tiers for SoloMapling artificial players.
 * Tiers are ordered from highest (S) to lowest (D).
 */
public enum BotTier {
    S(5, "Elite"),
    A(4, "High"),
    B(3, "Above Average"),
    C(2, "Average"),
    D(1, "Below Average");

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

    public boolean isAtLeast(BotTier minimumTier) {
        return this.value >= minimumTier.value;
    }

    public boolean isBetterThan(BotTier otherTier) {
        return this.value > otherTier.value;
    }

    public boolean isWorseThan(BotTier otherTier) {
        return this.value < otherTier.value;
    }

    public BotTier orDefault(BotTier minimumTier, BotTier defaultTier) {
        return this.isAtLeast(minimumTier) ? this : defaultTier;
    }

    public BotTier boostIfBelow(BotTier threshold, BotTier boostTo) {
        return this.isAtLeast(threshold) ? this : boostTo;
    }

    public BotTier getNextHigherTier() {
        return switch (this) {
            case D -> C;
            case C -> B;
            case B -> A;
            case A, S -> S;
        };
    }

    public BotTier getNextLowerTier() {
        return switch (this) {
            case S -> A;
            case A -> B;
            case B -> C;
            case C, D -> D;
        };
    }

    public int getTierDifference(BotTier otherTier) {
        return this.value - otherTier.value;
    }

    public static BotTier fromString(String tierString) {
        try {
            return BotTier.valueOf(tierString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static BotTier getDefaultTier() {
        return S;
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }

    public static final class TierManager {

        private TierManager() {
        }

        public static BotTier safeTierSet(BotTier currentTier, BotTier newTier) {
            return newTier != null ? newTier : getDefaultTier();
        }

        public static BotTier getEffectiveTier(BotTier currentTier) {
            if (currentTier == null) {
                return getDefaultTier();
            }
            return currentTier.boostIfBelow(BotTier.C, BotTier.B);
        }

        public static boolean canPerformAction(BotTier currentTier, BotTier requiredTier) {
            return currentTier != null && requiredTier != null && currentTier.isAtLeast(requiredTier);
        }

        public static BotTier upgradeTier(BotTier currentTier) {
            return currentTier != null ? currentTier.getNextHigherTier() : getDefaultTier();
        }

        public static BotTier downgradeTier(BotTier currentTier) {
            return currentTier != null ? currentTier.getNextLowerTier() : getDefaultTier();
        }

        public static BotTier getSafeTier(BotTier tier) {
            return tier != null ? tier : getDefaultTier();
        }
    }
}
