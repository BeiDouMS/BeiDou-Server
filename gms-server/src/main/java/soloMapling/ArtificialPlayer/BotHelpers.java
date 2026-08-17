package soloMapling.ArtificialPlayer;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.net.server.Server;
import org.gms.net.server.channel.Channel;
import org.gms.server.ItemInformationProvider;
import org.gms.server.maps.MapItem;
import org.gms.server.maps.MapObject;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class BotHelpers {

    // BotHelpers - bot related stuff with regards to programming (object manipulation, etc)

    public static Character getCharFromChannelStorage(int cid) {
        // just as a catch incacse i enter the cid without 20000
        if (cid < 1000) {
            cid = cid + 20000;
        }

        Channel channel = Server.getInstance().getChannel(0, 1);
        Character fakechar = channel.getPlayerStorage().getCharacterById(cid);
        if (isBot(fakechar)) {
            return (fakechar);
        }
        return null;
    }

    public static boolean isBot(Character chr) {
        return isBot(chr.getId());
    }

    private static boolean isBot(int id) {
        return isArtificial(id) || isConsole(id);
    }

    private static boolean isArtificial(int id) {
        return id > 20000;
    }

    private static boolean isConsole(int id) {
        return id == 999;
    }

    public static String convertItemIdToName(int itemId) {
        String itemName = ItemInformationProvider.getInstance().getName(itemId);
        if (itemName == null) {
            return "NULL";
        }
        return itemName;
    }

    public static Point adjustCenterPositionXAxis(Point center, int currIndex, int initialIncrement, int subsequentIncrement, int offset) {
        // initialIncrement = How many units it will go left
        // SubsequentIncrement = How many units it will go right (Usually 2x initial Increment for an even "spread"
        // Offset = how much space between each item
        if (currIndex < initialIncrement) {
            center.x += offset;
        } else {
            int adjustedIndex = currIndex - initialIncrement;
            int cycle = (adjustedIndex / subsequentIncrement) % 2;

            if (cycle == 0) { // Even cycle, increment by 30
                if (adjustedIndex % subsequentIncrement < subsequentIncrement) {
                    center.x -= offset;
                }
            } else { // Odd cycle, decrement by 30
                if (adjustedIndex % subsequentIncrement < subsequentIncrement) {
                    center.x += offset;
                }
            }
        }
        return center;
    }

    public static boolean checkSecondListInsideFirstList(List<MapObject> list1, List<MapObject> list2) {
        if (list1.size() < list2.size()) {
            System.out.println("Current List is greater than 1st");
            return false;
        }

        for (MapObject obj2 : list2) {
            boolean found = false;
            for (MapObject obj1 : list1) {
                if (areObjectsEqual(obj1, obj2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("Item Not Found");
                return false;
            }
        }
        return true;
    }

    private static boolean areObjectsEqual(MapObject obj1a, MapObject obj2b) {
        MapItem obj1 = (MapItem) obj1a;
        MapItem obj2 = (MapItem) obj2b;
        if (obj1 == obj2) return true;
        if (obj1 == null || obj2 == null) return false;

        return obj1.getItemId() == obj2.getItemId() &&
                obj1.getOwnerId() == obj2.getOwnerId() &&
                obj1.getItem().getQuantity() == (obj2.getItem().getQuantity());
    }

    // Blocks the current thread for the given number of milliseconds.
    // ONLY for deliberate synchronous choreography: scripts whose steps have
    // data-driven durations (dialogue playback, movement recordings, blocking
    // warps) run sequentially on a virtual thread and are MEANT to hold it.
    // Every such call site carries a "deliberate" comment saying why.
    // For everything else use the BotTiming toolkit (decision table in its
    // header): pacing the FSM's next action = BotSM.waitFor, one delayed
    // side-effect = BotTiming.after, scripted beats = BotTiming.chain.
    // Returns true if the sleep completed, false if the thread was interrupted
    // (interrupt flag restored so the caller can bail).
    public static boolean blockingSleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static Rectangle createRectangle(Point center, int width, int height) {
        // Calculate half dimensions
        int halfWidth = width / 2;
        int halfHeight = height / 2;

        // Calculate the vertical offset: 20% of the height
        int verticalOffset = (int) (height * 0.2);

        // Adjust the center point vertically to place it closer to the bottom
        int centerYAdjusted = center.y - halfHeight + verticalOffset;

        // Calculate top-left corner of the rectangle
        int topLeftX = center.x - halfWidth;
        int topLeftY = centerYAdjusted - halfHeight;

        // Create and return the Rectangle
        return new Rectangle(topLeftX, topLeftY, width, height);
    }

    public static boolean isPointWithinRectangle(Point[] rectangle, Point point) {
        if (rectangle.length != 2) {
            throw new IllegalArgumentException("Rectangle must have exactly two points: top-left and bottom-right.");
        }

        Point topLeft = rectangle[0];
        Point bottomRight = rectangle[1];

        // Check if the point is within the bounds of the rectangle
        return point.x >= topLeft.x && point.x <= bottomRight.x &&
                point.y >= topLeft.y && point.y <= bottomRight.y;
    }

    // Blocks the replaying thread for the gap between two recorded packet timestamps.
    // Deliberately blocking: recording replay is data-driven synchronous choreography
    // and runs on a virtual thread. Returns false if the thread was interrupted
    // (interrupt flag restored) so the replay loop can bail; true if the wait completed.
    // Diff is clamped at 0 so an out-of-order timestamp can't throw from Thread.sleep.
    public static boolean waitBetweenTwoLong(long timestamp1, long timestamp2) {
        long diff = Math.max(0, timestamp2 - timestamp1);
        if (diff > 2000) {
            System.out.println("More than 2 seconds waiting");
        }
        try {
            Thread.sleep(diff);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static Point getRandomizedPointXAxis(Point original) {
        return getRandomizedPointXAxis(original, 50);
    }

    public static Point getRandomizedPointXAxis(Point original, int range) {
        int minX = original.x - range;
        int maxX = original.x + range;
        int randomX = new Random().nextInt(maxX - minX + 1) + minX;
        return new Point(randomX, original.y);
    }

    /*
        // Lasts for about 10 seconds, no ereve bird
        #r TEST #k = red text
        #b TEST #k = blue text
        #g TEST #k = green text
        \r\n = new line
        String msg = "#rTEST#k\r\n1. #bTest 1#k\r\n2. #rTest 2#k\r\n3. #bTest 3#k\r\n4. #rTest 4#k";
    */

}
