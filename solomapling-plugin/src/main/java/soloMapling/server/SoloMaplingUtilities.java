package soloMapling.server;

import org.gms.client.Character;
import org.gms.net.server.Server;
import org.gms.net.server.channel.Channel;
import org.gms.net.server.world.World;
import org.gms.server.maps.MapleMap;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class SoloMaplingUtilities {

    public static Random random = new Random();
    public static final Channel channel = Server.getInstance().getChannel(SoloMaplingConstants.GameConstants.WORLD_SCANIA, SoloMaplingConstants.GameConstants.CHANNEL_1);
    public static final World world = Server.getInstance().getWorld(SoloMaplingConstants.GameConstants.WORLD_SCANIA);

    // A 1-in-N dice roll, NOT a percent chance: rollChanceInverse(20) is true one time in 20.
    // The bigger the number, the lower the chance. For percent semantics use chance(percent).
    public static boolean rollChanceInverse(int outOf) {
        return new Random().nextInt(outOf) == 0;
    }

    // Use this for easy boolean % Chance calculating
    public static boolean chance(double percent) {
        return ThreadLocalRandom.current().nextDouble(100) < percent;
    }

    public static String pickRandomItem(List<String> items) {
        return items.get(random.nextInt(items.size()));
    }

    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static <T> T getRandomElement(List<T> list) {
        try {
            Random random = new Random();
            return list.get(random.nextInt(list.size()));
        } catch (IllegalArgumentException e) {
            return null; // or some default value
        }
    }

    public static org.gms.client.Character getChr(int userId) {
        Character chr = SoloMaplingConstants.mainChannel.getPlayerStorage().getCharacterById(userId);
        return chr;
    }

    public static Character getChr(String name) {
        Character chr = SoloMaplingConstants.mainChannel.getPlayerStorage().getCharacterByName(name);
        return chr;
    }

    public static String getTextAfterColon(String input) {
        int colonIndex = input.indexOf(":");
        if (colonIndex != -1 && colonIndex < input.length() - 1) {
            return input.substring(colonIndex + 1).trim();
        }
        return null; // Return null if no colon or text is found
    }

    public static boolean isNumeric(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Waits for a condition to become true with retry logic
     * @param condition The boolean condition to check (as a Supplier)
     * @param maxAttempts Maximum number of attempts
     * @param delayMs Delay between attempts in milliseconds
     * @return true if condition became true, false if max attempts reached
     */
    public static boolean waitForCondition(Supplier<Boolean> condition, int maxAttempts, long delayMs) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (condition.get()) {
                System.out.println("Condition met on attempt " + attempt);
                return true;
            }

            if (attempt < maxAttempts) {
                System.out.println("Attempt " + attempt + " failed, waiting " + delayMs + "ms...");
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        System.out.println("Max attempts (" + maxAttempts + ") reached, condition not met");
        return false;
    }

    /**
     * Overloaded method with default values (15 attempts, 2 seconds)
     */
    public static boolean waitForCondition(Supplier<Boolean> condition) {
        return waitForCondition(condition, 15, 2000);
    }

    public static int getRandomNumber(int x, int y) {
        return generateRandomNumber(x, y);
    }

    public static int generateRandomNumber(int x, int y) {
        if (x > y) {
            throw new IllegalArgumentException("x must be less than or equal to y.");
        }
        Random random = new Random();
        // Generate a random number between x and y (inclusive)
        return random.nextInt(y - x + 1) + x;
    }

    public static int getRandomNumber(List<Integer> numbers) {
        Random rand = new Random();
        int index = rand.nextInt(numbers.size()); // pick a random index
        return numbers.get(index);
    }

    public static MapleMap getMapleMapById(int id) {
        MapleMap map = Server.getInstance().getChannel(0, 1).getMapFactory().getMap(id);
        return map;
    }

}
