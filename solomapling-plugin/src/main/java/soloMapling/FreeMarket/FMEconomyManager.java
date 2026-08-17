package soloMapling.FreeMarket;

import org.gms.client.Character;
import org.gms.client.Job;
import org.gms.server.maps.HiredMerchant;

import java.awt.*;
import java.util.Map;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class FMEconomyManager {

    public static FMShopInfoManager fmInfo = new FMShopInfoManager();

    private static final int[] HOT_ROOMS = {910000001, 910000002, 910000007};

    public static boolean isHotRoom(int mapId) {
        for (int hotRoom : HOT_ROOMS) {
            if (mapId == hotRoom) return true;
        }
        return false;
    }

    private static final Map<Integer, Double> mapIdMultipliers = Map.ofEntries(
            Map.entry(910000001, 1.25),
            Map.entry(910000002, 1.20),
            Map.entry(910000003, 1.10),

            Map.entry(910000007, 1.15),

            Map.entry(910000014, 0.97),
            Map.entry(910000015, 0.95),
            Map.entry(910000016, 0.93),
            Map.entry(910000017, 0.91),

            Map.entry(910000018, 0.9),
            Map.entry(910000019, 0.88),
            Map.entry(910000020, 0.86),
            Map.entry(910000021, 0.84),
            Map.entry(910000022, 0.82)
    );

    public static int FMRoomPremiumCalculator(int mapId, int adjustedPrice) {
        // Specific FM room premium
        double multiplier = mapIdMultipliers.getOrDefault(mapId, 1.0);
        adjustedPrice = (int) (adjustedPrice * multiplier);
        return adjustedPrice;
    }

    public static int FMRoomDoorSpotPremiumCalculator(int mapId, Point pos, int adjustedPrice) {
        // Door Spot premium
        if (fmInfo.getRegionFMMapId("henesys").contains(mapId)) { // door spots
            if (isPointWithinArea(pos, new Point(300, -80), new Point(640, 34))) {
                adjustedPrice = (int) (adjustedPrice * 1.2);
            }
        }

        if (fmInfo.getRegionFMMapId("ludi").contains(mapId)) { // door spots
            if (isPointWithinArea(pos, new Point(-709, -100), new Point(-109, 102))) {
                adjustedPrice = (int) (adjustedPrice * 1.2);
            }
        }
        return adjustedPrice;
    }


    public static int calcPriceBasedOnDay(int price) {
        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();

        // Apply the price adjustment based on the day
        if (currentDay == DayOfWeek.FRIDAY || currentDay == DayOfWeek.SATURDAY || currentDay == DayOfWeek.SUNDAY) {
            return (int) (price * 1.10); // Increase by 10%
        } else if (currentDay == DayOfWeek.MONDAY || currentDay == DayOfWeek.TUESDAY) {
            return (int) (price * 0.95); // Decrease by 5%
        }

        return price; // No adjustment for other days
    }

    public static int randomizePriceAdjustment(int price) {
        Random random = new Random();

        // Define weighted probabilities for the adjustments
        int[] adjustments = {-10, -5, 0, 5, 10}; // Adjustments in percentages
        int[] weights = {10, 20, 40, 20, 10};    // Weights corresponding to the adjustments

        // Calculate the total weight
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }

        // Generate a random number in the range [1, totalWeight]
        int randomValue = random.nextInt(totalWeight) + 1;

        // Determine which adjustment to apply based on the random value
        int cumulativeWeight = 0;
        int chosenAdjustment = 0;
        for (int i = 0; i < adjustments.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue <= cumulativeWeight) {
                chosenAdjustment = adjustments[i];
                break;
            }
        }

        // Apply the chosen adjustment to the price
        double multiplier = 1 + (chosenAdjustment / 100.0);
        return (int) (price * multiplier);
    }


    public static int adjustFMPrices(HiredMerchantArtificial merchant, int price) {
        int adjustedPrice = price;
        Point pos = merchant.getPosition();
        int mapId = merchant.getMapId();
        return priceAdjustmentRules(adjustedPrice, mapId, pos);
    }

    public static int priceAdjustmentRules(int adjustedPrice) {
        return priceAdjustmentRules(adjustedPrice, null, null);
    }

    public static int priceAdjustmentRules(int adjustedPrice, Integer mapId, Point pos) {
        adjustedPrice = calcPriceBasedOnMarket(adjustedPrice); // Adjust price based on market Value
        adjustedPrice = calcPriceBasedOnDay(adjustedPrice);
        adjustedPrice = randomizePriceAdjustment(adjustedPrice);

        if (mapId != null) {
            adjustedPrice = FMRoomPremiumCalculator(mapId, adjustedPrice);
        }
        if (mapId != null && pos != null) {
            adjustedPrice = FMRoomDoorSpotPremiumCalculator(mapId, pos, adjustedPrice);
        }

        // format price number
        adjustedPrice = getPriceStylingNotation(adjustedPrice);
        return adjustedPrice;
    }

    public static int adjustFMQuantity(HiredMerchantArtificial merchant, int quantity) {
        int adjustedQuantity = quantity;
        if (adjustedQuantity == 1) {
            return 1;
        }
        Point pos = merchant.getPosition();
        int mapId = merchant.getMapId();

        adjustedQuantity = FMRoomPremiumCalculator(mapId, adjustedQuantity);
        adjustedQuantity = FMRoomDoorSpotPremiumCalculator(mapId, pos, adjustedQuantity);

        return adjustedQuantity;

    }

    public static boolean isPointWithinArea(Point point, Point topleft, Point botright) {
        return point.x >= topleft.x && point.x <= botright.x &&
                point.y >= topleft.y && point.y <= botright.y;
    }


    /*
       stock price system, fluctuates between 75% - 125% of market.
    */
    private static final int HOURS_IN_A_DAY = 24;
    private static final int UPDATE_INTERVAL_HOURS = 2;
    private static final int VALUES_COUNT = HOURS_IN_A_DAY / UPDATE_INTERVAL_HOURS;
    private static final double MIN_INDEX = 0.85;
    private static final double MAX_INDEX = 1.15;
    private static List<Double> marketIndices = null;

    private static final Random random = new Random();

    // Method to generate or retrieve the market index list
    public static List<Double> getMarketIndices() {
        // If the list hasn't been generated yet, generate it
        if (marketIndices == null) {
            generateMarketIndices();
        }
        return marketIndices;
    }

    // Generate the list of market indices with a sine wave + randomness pattern
    private static void generateMarketIndices() {
        marketIndices = new ArrayList<>();
        double amplitude = (MAX_INDEX - MIN_INDEX) / 2.0;
        double baseline = (MAX_INDEX + MIN_INDEX) / 2.0;

        for (int i = 0; i < VALUES_COUNT; i++) {
            // Sine wave component for smooth fluctuation
            double sineComponent = Math.sin((2 * Math.PI / VALUES_COUNT) * i);

            // Add small random noise for variability
            double randomNoise = (random.nextDouble() - 0.5) * 0.05; // +/- 0.025

            // Calculate the final index value
            double index = baseline + amplitude * sineComponent + randomNoise;
            marketIndices.add(clamp(index, MIN_INDEX, MAX_INDEX)); // Ensure it's within bounds
        }
    }

    // Clamp a value to the min and max range
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // Get the current market index based on the time of day
    public static double getCurrentMarketIndex() {
        List<Double> indices = getMarketIndices();
        int currentHour = LocalTime.now().getHour();
        int indexPosition = (currentHour / UPDATE_INTERVAL_HOURS) % VALUES_COUNT;
        return indices.get(indexPosition);
    }

    public static int calcPriceBasedOnMarket(int price) {
        return (int) (price * getCurrentMarketIndex());
    }

    /**
     * Converts an integer price to a concise English notation (k for thousands, m for millions, b for billions)
     * with customizable decimal precision.
     *
     * @param price The integer price to convert
     * @param decimalPlaces The number of decimal places to display (0 for none)
     * @return A string representation of the price in English notation
     */
    public static String formatPriceToShorthand(int price, int decimalPlaces) {
        if (price < 1000) {
            return String.valueOf(price);
        }

        final String[] suffixes = {"", "k", "m", "b"};
        int suffixIndex = 0;
        double formattedPrice = price;

        while (formattedPrice >= 1000 && suffixIndex < suffixes.length - 1) {
            formattedPrice /= 1000;
            suffixIndex++;
        }

        // Format with specified decimal places
        if (decimalPlaces <= 0) {
            // No decimals, just round to integer
            return Math.round(formattedPrice) + suffixes[suffixIndex];
        } else {
            // Use specified decimal places
            String formatPattern = "%." + decimalPlaces + "f";
            String formatted = String.format(formatPattern, formattedPrice);

            // Remove trailing zeros after the decimal point
            if (formatted.contains(".")) {
                formatted = formatted.replaceAll("0+$", "").replaceAll("\\.$", "");
            }

            return formatted + suffixes[suffixIndex];
        }
    }

    public static String formatPriceToShorthand(int price) {
        return formatPriceToShorthand(price, 1);
    }

    public static int getPriceStylingNotation(int number) {
        int formatType = random.nextInt(3);
        number = roundToNearestCleanNumber(number);
        // Switch-like statement
        switch (formatType) {
            case 0:
                return formatWithTrailingZeros(number);
            case 1:
                return formatWithTrailingNines(number);
            case 2:
                return formatWithTrailingMainNumber(number);
            default:
                return number;
        }
    }

    public static int formatWithTrailingZeros(int num) {
        return num;
    }

    public static int formatWithTrailingNines(int num) {
        return num - 1;
    }

    public static int formatWithTrailingMainNumber(int number) {
        // Convert the number to a string to make it iterable
        String numStr = String.valueOf(number);

        // Find the last non-zero digit
        int lastNonZeroIndex = -1;
        for (int i = numStr.length() - 1; i >= 0; i--) {
            if (numStr.charAt(i) != '0') {
                lastNonZeroIndex = i;
                break;
            }
        }

        // If no non-zero digit is found, return the original number
        if (lastNonZeroIndex == -1) {
            return number;
        }

        // Get the last non-zero digit
        char lastNonZeroDigit = numStr.charAt(lastNonZeroIndex);

        // Replace all subsequent zeros with the last non-zero digit
        StringBuilder newNumStr = new StringBuilder(numStr.substring(0, lastNonZeroIndex + 1));
        for (int i = lastNonZeroIndex + 1; i < numStr.length(); i++) {
            newNumStr.append(lastNonZeroDigit);
        }

        // Convert the new string back to an integer
        return Integer.parseInt(newNumStr.toString());
    }

    public static int roundToNearestCleanNumber(int number) {
        // Determine the number of digits
        int numDigits = String.valueOf(Math.abs(number)).length();

        // Define the number of significant digits based on the number of digits
        int significantDigits;
        if (6 <= numDigits && numDigits <= 9) {
            significantDigits = 2;
        } else if (numDigits == 10) {
            significantDigits = 3;
        } else {
            // If the number of digits is less than 6 or greater than 10, return the original number
            return number;
        }

        // Calculate the most significant digits
        int mostSignificant = Integer.parseInt(String.valueOf(number).substring(0, significantDigits));

        // Calculate the order of magnitude
        int orderMagnitude = (int) Math.pow(10, numDigits - significantDigits);

        // Calculate the rounded number
        int roundedNumber = Math.round(number / orderMagnitude) * orderMagnitude;

        return roundedNumber;
    }


    // Method to determine which tier to pick based on room number
    public static String getTierForRoom(int roomNumber) {
        Random random = new Random();

        if (isHotRoom(roomNumber)) {
            int[] weightsHot = {85, 13, 2};  // 85% S, 13% A, 2% B
            return weightedRandomSelection(weightsHot, random);
        } else if (roomNumber == 910000003) {
            int[] weightsWarm = {60, 35, 5};  // 60% S, 35% A, 5% B
            return weightedRandomSelection(weightsWarm, random);
        } else if (roomNumber >= 910000004 && roomNumber <= 910000006 || roomNumber >= 910000008 && roomNumber <= 910000012) {
            // A-tier is more likely
            int[] weightsA = {25, 60, 15};
            return weightedRandomSelection(weightsA, random);
        } else if (roomNumber == 910000013) {
            // Balanced distribution for S, A, and B
            int[] weightsBalanced = {33, 33, 34};
            return weightedRandomSelection(weightsBalanced, random);
        } else if (roomNumber >= 910000014 && roomNumber <= 910000017) {
            // B-tier is more likely
            int[] weightsB = {15, 30, 55};
            return weightedRandomSelection(weightsB, random);
        } else if (roomNumber >= 910000018 && roomNumber <= 910000022) {
            int[] weightsRandom = {20, 40, 40};
            return weightedRandomSelection(weightsRandom, random);
        }
        return "Unknown"; // Default case, should not occur if room number is valid
    }

    // Helper method for weighted random selection
    private static String weightedRandomSelection(int[] weights, Random random) {
        int totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }

        int randomValue = random.nextInt(totalWeight);  // Random number between 0 and totalWeight
        int cumulativeWeight = 0;

        if (randomValue < cumulativeWeight + weights[0]) {
            return "S";  // Select S
        }
        cumulativeWeight += weights[0];
        if (randomValue < cumulativeWeight + weights[1]) {
            return "A";  // Select A
        }
        cumulativeWeight += weights[1];
        return "B";  // Select B
    }


    public static int multiplyWzPriceByJobStyle(int price, Job jobStyle) {
        int adjustedPrice = (int) (price * 1.5);
        if (jobStyle == Job.WARRIOR) {
            adjustedPrice = (int) (adjustedPrice * 1.4);
        } else if (jobStyle == Job.MAGICIAN) {
            adjustedPrice = (int) (adjustedPrice * 1.35);
        } else if (jobStyle == Job.BOWMAN) {
            adjustedPrice = (int) (adjustedPrice * 1.30);
        } else if (jobStyle == Job.THIEF) {
            adjustedPrice = (int) (adjustedPrice * 1.5);
        } else if (jobStyle == Job.PIRATE) {
            adjustedPrice = (int) (adjustedPrice * 1.25);
        } else {
            adjustedPrice = (int) (adjustedPrice * 1.30);
        }
        return adjustedPrice;
    }


    // Test the method
    public static void main(String[] args) {
        int[] testRooms = {1, 4, 13, 18, 7};
        for (int room : testRooms) {
            String tier = getTierForRoom(room);
            System.out.println("Room " + room + " selected tier: " + tier);
        }
    }

//    public static void main(String[] args) {
//        // Example usage
//        System.out.println("Current Market Index: " + getCurrentMarketIndex());
//        System.out.println("Full Market Index List:");
//        getMarketIndices().forEach(System.out::println);
//    }


}
