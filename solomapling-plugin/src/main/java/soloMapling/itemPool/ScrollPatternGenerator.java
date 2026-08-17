package soloMapling.itemPool;

import soloMapling.server.MapleVersionManager;

import java.util.List;
import java.util.Random;

public class ScrollPatternGenerator {

    private static final Random random = new Random();
    private final static ScrollPatternGenerator instance = new ScrollPatternGenerator();

    public static ScrollPatternGenerator getInstance() {
        return instance;
    }

    // Helper class to encapsulate a probability rule
    private static class ProbabilityRule {
        final int passes;
        final int chance;

        ProbabilityRule(int passes, int chance) {
            this.passes = passes;
            this.chance = chance;
        }
    }

    // Generates the scroll pattern based on slot count and configuration
    public static int[] generateScrollPattern(int slots, List<ProbabilityRule> tenPercentRules, List<ProbabilityRule> sixtyPercentRules) {
        int tenPercentPasses = determinePasses(tenPercentRules);
        int remainingSlots = slots - tenPercentPasses;
        int sixtyPercentPasses = (remainingSlots > 0) ? determinePasses(sixtyPercentRules, remainingSlots) : 0;

        return new int[]{tenPercentPasses, sixtyPercentPasses};
    }

    // Determines passes based on weighted probability from a list of rules
    private static int determinePasses(List<ProbabilityRule> rules) {
        return determinePasses(rules, Integer.MAX_VALUE); // no slot limit
    }

    private static int determinePasses(List<ProbabilityRule> rules, int slotLimit) {
        int roll = random.nextInt(100) + 1;
        int cumulativeChance = 0;

        for (ProbabilityRule rule : rules) {
            cumulativeChance += rule.chance;
            if (roll <= cumulativeChance) {
                return Math.min(rule.passes, slotLimit);
            }
        }
        return 1; // Default fallback (should rarely happen if chances add up to 100)
    }

    public static int[] getScrollPattern(int slots) {
        if (MapleVersionManager.getVersion() >= 40) {
            return getScrollPatternVersion40(slots);
        } else {
            return getScrollPatternVersion1(slots);
        }
    }

    // Sample method calls for different versions of scrolling patterns
    public static int[] getScrollPatternVersion1(int slots) {
        List<ProbabilityRule> tenPercentRules = List.of(
                new ProbabilityRule(3, 3),
                new ProbabilityRule(2, 35),
                new ProbabilityRule(1, 62)
        );

        List<ProbabilityRule> sixtyPercentRules = List.of(
                new ProbabilityRule(5, 10),
                new ProbabilityRule(4, 30),
                new ProbabilityRule(3, 30),
                new ProbabilityRule(2, 20),
                new ProbabilityRule(1, 10)
        );

        return generateScrollPattern(slots, tenPercentRules, sixtyPercentRules);
    }

    public static int[] getScrollPatternVersion40(int slots) {
        List<ProbabilityRule> tenPercentRules = List.of(
                new ProbabilityRule(4, 15),
                new ProbabilityRule(3, 25),
                new ProbabilityRule(2, 45),
                new ProbabilityRule(1, 15)
        );

        List<ProbabilityRule> sixtyPercentRules = List.of(
                new ProbabilityRule(5, 10),
                new ProbabilityRule(4, 30),
                new ProbabilityRule(3, 30),
                new ProbabilityRule(2, 30)
//                new ProbabilityRule(1, 10)
        );

        return generateScrollPattern(slots, tenPercentRules, sixtyPercentRules);
    }

    // TODO - add more patterns based on lower tiers, i.e. 60%'s only, only 1 10% max at low rate (A-B tier)

    public static void main(String[] args) {
        // Test the patterns
        int slots = 7;

        for (int x = 0; x < 10; x++) {
            System.out.println("Round: " + x);
            int[] pattern1 = getScrollPatternVersion1(slots);
            System.out.printf("Pattern Version 1 for %d slots: 10%% passes = %d, 60%% passes = %d%n", slots, pattern1[0], pattern1[1]);

            int[] pattern2 = getScrollPatternVersion40(slots);
            System.out.printf("Pattern Version 2 for %d slots: 10%% passes = %d, 60%% passes = %d%n", slots, pattern2[0], pattern2[1]);
        }
    }
}
