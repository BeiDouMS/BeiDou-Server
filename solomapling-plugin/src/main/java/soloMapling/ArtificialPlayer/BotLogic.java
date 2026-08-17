package soloMapling.ArtificialPlayer;

import org.gms.client.Character;
import org.gms.client.inventory.BodyPart;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.constants.inventory.ItemConstants;
import org.gms.server.ItemInformationProvider;
import org.gms.server.life.Monster;
import org.gms.server.life.NPC;
import org.gms.server.life.PlayerNPC;
import org.gms.server.maps.MapItem;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotCommandsPack.DropCommands;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static soloMapling.ArtificialPlayer.BotHelpers.checkSecondListInsideFirstList;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.FreeMarketValues.FM_ENTRANCE;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.FreeMarketValues.FM_ROOM_1;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.FreeMarketValues.FM_ROOM_22;
import static soloMapling.DebugUtilities.debugprint;

public class BotLogic {

    // BotLogic - things that are game thinking related, invisible to players

    public static Character waitForPlayerInRange(Character fakechar, int width, int height) {
        Rectangle rect = BotHelpers.createRectangle(fakechar.getPosition(), width, height);
        List<MapObject> playersInRange = getPlayersInRange(fakechar, rect);
        return findFirstPlayerInRange(fakechar, playersInRange);
    }

    // Helper method to get all players in the rectangle
    private static List<MapObject> getPlayersInRange(Character fakeChar, Rectangle rect) {
        return fakeChar.getMap().getMapObjectsInRect(rect, List.of(MapObjectType.PLAYER));
    }

    // Helper method to find the first valid player in the range
    private static Character findFirstPlayerInRange(Character fakeChar, List<MapObject> playersInRange) {
        for (MapObject obj : playersInRange) {
            if (obj instanceof Character) {
                Character player = (Character) obj;
                if (player.getId() != fakeChar.getId()) {
                    return player;
                }
            }
        }
        return null; // No valid player found
    }

    public static List<MapObject> checkForItemsOnFloor(Character chr, Point location, int[] itemIds) {
        return checkForItemsOnFloor(chr, location, 5000, itemIds);
    }

    public static List<MapObject> checkForItemsOnFloor(Character chr, Point location, double range, int[] itemIds) {
        // Return list of items on floor that matches itemId
        List<MapObject> itemsOnFloor = readItemOnFloor(chr, location, range);
        List<MapObject> items = filterDroppedItems(itemsOnFloor, itemIds);
        return items;
    }

    public static List<MapObject> checkForItemOnFloor(Character chr, Point location, int itemId) {
//        // Return list of item on floor that matches itemId
        int[] itemToCheck = {itemId};
        return checkForItemsOnFloor(chr, location, itemToCheck);
    }

    public static List<MapObject> readItemOnFloor(Character character, Point location, double range) {
        List<MapObject> items = character.getMap().getMapObjectsInRange(location, range, Arrays.asList(MapObjectType.ITEM));
        for (MapObject item : items) {
            MapItem mapItem = (MapItem) item;
            int mapItemId = mapItem.getItemId();
            int qty = mapItem.getItem().getQuantity();
            String itemName = BotHelpers.convertItemIdToName(mapItemId);
            Point itemLoc = mapItem.getPosition();
        }
        return items;
    }

    public static List<MapObject> filterDroppedItems(List<MapObject> items, int[] filterList) {
        // Returns list of items on Floor that ONLY contains items in filterList
        Iterator<MapObject> iterator = items.iterator();
        List<Integer> itemFilterList = Arrays.stream(filterList).boxed().collect(Collectors.toList());
        while (iterator.hasNext()) {
            MapItem mapItem = (MapItem) iterator.next();
            if (!itemFilterList.contains(mapItem.getItemId())) {
                iterator.remove();
            }
        }
        return items;
    }

    public static List<MapObject> filterPlayersDroppedItems(List<MapObject> items, Character player) {
        // Filter out items that do not belong to the player
        Iterator<MapObject> iterator = items.iterator();
        while (iterator.hasNext()) {
            MapItem mapItem = (MapItem) iterator.next();
            if (mapItem.getOwnerId() != player.getId()) {
                iterator.remove();
            }
        }
        return items;
    }

    public static boolean checkIfItemsOnFloorStill(List<MapObject> itemsToCheck, List<MapObject> itemsAtPlayer) {
        return checkSecondListInsideFirstList(itemsAtPlayer, itemsToCheck); // Check if items on Floor INSIDE Pot. A player may have dropped more items.
    }

    public static boolean isNpcPresent(MapleMap map, int NpcID) {
        for (MapObject mmo : map.getMapObjects()) {
            if (mmo instanceof NPC npc && npc.getId() == NpcID) {
                return true;
            }
        }
        return false;
    }

    public static Item generateCleanItemEquip(int id) {
        Item cleanItem;
        if (ItemConstants.getInventoryType(id) == InventoryType.EQUIP) {
            cleanItem = generateCleanEquip(id);
        } else {
            cleanItem = generateCleanItem(id);
        }
        return cleanItem;
    }

    public static Item generateCleanEquip(int itemId) {
        return ItemInformationProvider.getInstance().getEquipById(itemId);
    }

    public static Item generateCleanItem(int itemId) {
        return generateCleanItemWithQty(itemId, 1);
    }

    public static Item generateCleanItemWithQty(int itemId, int qty) {
        return new Item(itemId, (short) 0, (short) qty);
    }


    public static Equip readPlayerEquipBySlotName(Character fakechar, BodyPart bodyPartName) {
        return readPlayerEquipBySlotName(fakechar, bodyPartName, false);
    }

    public static Equip readPlayerCashEquipBySlotName(Character fakechar, BodyPart bodyPartName) {
        return readPlayerEquipBySlotName(fakechar, bodyPartName, true);
    }

    public static Equip readPlayerEquipBySlotName(Character fakechar, BodyPart bodyPartName, boolean cash) {
        short dst = getSlotDstValueByEquipSlotName(bodyPartName, cash);
        return readPlayerEquipBySlotId(fakechar, dst);
    }

    public static Equip readPlayerEquipBySlotId(Character fakechar, short slot) {
        Equip target = (Equip) fakechar.getInventory(InventoryType.EQUIPPED).getItem(slot); // Currently equipped item
        if (target != null) {
            return target;
        } else {
            return null;
        }
    }

    public static short getSlotDstValueByEquipSlotName(BodyPart eqSlot, boolean cash) {
        if (cash) {
            return (short) -(eqSlot.getValue() + BodyPart.CASH_BASE.getValue());
        }
        return (short) -eqSlot.getValue();
    }

    public static HashMap<String, Integer> makeItemIdFromNameMap(int[] items) {
        HashMap<String, Integer> itemNameToIdMap = new HashMap<>();
        for (int itemId : items) {
            String itemName = BotHelpers.convertItemIdToName(itemId).toLowerCase();
            itemNameToIdMap.put(itemName, itemId);
        }
        return itemNameToIdMap;
    }

    /**
     * Returns real (non-bot) players within a rectangle around the bot.
     * Lightweight — single spatial query + id filter.
     */
    public static List<Character> getRealPlayersInRange(Character fakechar, int width, int height) {
        Rectangle rect = BotHelpers.createRectangle(fakechar.getPosition(), width, height);
        List<MapObject> objects = getPlayersInRange(fakechar, rect);
        List<Character> realPlayers = new ArrayList<>();
        for (MapObject obj : objects) {
            if (obj instanceof Character player) {
                if (player.getId() != fakechar.getId() && !BotHelpers.isBot(player)) {
                    debugprint("Detected real player: ", player.getName());
                    realPlayers.add(player);
                }
            }
        }
        return realPlayers;
    }

    public static boolean isCharNear(Point p1, Point p2) {
        return isPointNear(p1, p2, 25);
    }

    // isNear()
    public static boolean isPointNear(Point p1, Point p2, double maxDistance) {
        double distance = p1.distance(p2);
        return distance <= maxDistance;
    }

    // To ensure same platform
    public static boolean isPointNearSameY(Point p1, Point p2, double maxXDistance, int yThreshold) {
        if (Math.abs(p1.y - p2.y) > yThreshold) {
            return false;
        }
        return Math.abs(p1.x - p2.x) <= maxXDistance;
    }


    public static Point getFurthestPointFromOrigin(Point basePoint, List<Point> pts) {
        if (pts == null || pts.isEmpty()) {
            return null; // Return null if the list is empty or null
        }

        Point furthestPoint = null;
        double maxDistance = -1;

        for (Point p : pts) {
            double distance = basePoint.distanceSq(p); // Using squared distance to avoid unnecessary sqrt computation
            if (distance > maxDistance) {
                maxDistance = distance;
                furthestPoint = p;
            }
        }

        return furthestPoint;
    }

    public static boolean isWithinPriceRange(double currentPrice, double marketPrice) {
        return isWithinPriceRange(currentPrice, marketPrice, 0.80); // Default 80% tolerance
    }

    public static boolean isWithinPriceRange(double currentPrice, double marketPrice, double tolerancePercentage) {
        if (tolerancePercentage < 0 || tolerancePercentage > 1) {
            throw new IllegalArgumentException("Tolerance percentage must be between 0 and 1");
        }

        double lowerBound = marketPrice * (1 - tolerancePercentage);
        double upperBound = marketPrice * (1 + tolerancePercentage);

        return currentPrice >= lowerBound && currentPrice <= upperBound;
    }

    public static boolean isInsideFMRooms(Character fakechar) {
        int map = fakechar.getMapId();
        return map >= FM_ROOM_1 && map <= FM_ROOM_22;
    }

    public static boolean isAtFMEntrance(Character fakechar) {
        return fakechar.getMapId() == FM_ENTRANCE;
    }

    public static boolean isInsideFM(Character fakechar) {
        return isInsideFMRooms(fakechar) || isAtFMEntrance(fakechar);
    }

    //

//    public static List<Integer> calculateFall(int originalY, int finalY, int seconds) {
//        List<Integer> yPositions = new ArrayList<>();
//
//        // Constants
//        double fallSpeedFactor = 1;
//        int distance = Math.abs(finalY - originalY);
//        double adjustedSeconds = seconds * fallSpeedFactor; // Scale time
//        double gravity = 2.0 * distance / (adjustedSeconds * adjustedSeconds); // Adjusted acceleration
//        int direction = (finalY > originalY) ? 1 : -1; // Determine fall direction
//
//        // Calculate Y position at each second
//        for (int t = 0; t <= seconds; t++) {
//            double scaledT = t * fallSpeedFactor; // Adjust time step for slower fall
//            double y = originalY + direction * (0.5 * gravity * scaledT * scaledT); // Update position
//            int roundedY = (int) Math.round(y); // Round to nearest integer
//
//            // Clamp value to avoid overshooting
//            if ((direction == 1 && roundedY > finalY) || (direction == -1 && roundedY < finalY)) {
//                roundedY = finalY;
//            }
//
//            yPositions.add(roundedY);
//        }
//
//        return yPositions;
//    }


    // THESE ARE CASINO BOT-NAMED METHODS ONLY
    // todo refactor to generic
    // todo casino bot only


    // todo refactor to generic
    // todo casino bot only
    public static StringBuilder cleanUpBetsString(List<MapObject> items, StringBuilder betString) {
        // Clean up string for Casino Stamps
        for (MapObject item : items) {
            MapItem mapItem = (MapItem) item;
            int mapItemId = mapItem.getItemId();
            int qty = mapItem.getItem().getQuantity();
            String itemName = BotHelpers.convertItemIdToName(mapItemId);
            betString.append(qty).append("x ").append(itemName).append(", ");
        }
        betString = new StringBuilder(betString.toString().replace("'s Stamp", ""));
        betString = new StringBuilder(betString.toString().replace(" Stamp", ""));
        betString = new StringBuilder(betString.toString().replace(" the Really Old", ""));
        betString = new StringBuilder(betString.toString().replace(" Pierce", ""));
        return betString;
    }

    // todo refactor to generic
    // todo casino bot only
    public static String announceBetString(Character better, List<MapObject> items) {
        StringBuilder betString = new StringBuilder();
        betString.append(better.getName()).append("'s Bet: ");
        betString = BotLogic.cleanUpBetsString(items, betString);
        return betString.toString();
    }

    // todo refactor to generic
    // todo casino bot only
    public static List<MapObject> readPlayersBetsStamps(Character player, double range) {
        return readPlayersBetsStamps(player, player.getPosition(), range);
    }

    // todo refactor to generic
    // todo casino bot only
    public static List<MapObject> readPlayersBetsStamps(Character player, Point location, double range) {
        List<MapObject> items = readItemOnFloor(player, location, range);
        items = filterPlayersDroppedItems(items, player);

        int[] stamps = {4002000, 4002001, 4002002, 4002003, 4031558, 4031559, 4031560, 4031561};
        items = filterDroppedItems(items, stamps);

        return items;
    }


}
