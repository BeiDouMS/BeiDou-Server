package soloMapling.ArtificialPlayer.BotCommandsPack;

import org.gms.client.Character;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.Item;
import org.gms.net.packet.Packet;
import org.gms.server.maps.MapItem;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.server.TimerManager;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotLogic;
import org.gms.util.PacketCreator;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static soloMapling.ArtificialPlayer.BotLogic.checkForItemsOnFloor;

public class DropCommands {

    /*
        // Drop type 0 = 0 is DROP_USEROWN = 15 second delay
        // 2 = (DROP_NOOWN) = can loot instantly
        // playerDrop false = pet lootable (for owner, or if magic scales)

        [x] Meso Drop
            Equip Drop
            Item Drop (Qty 1-x)
        [x] Throw (Drop to specific location)
        [x] Throw to owner - Lootable ONLY by owner for 15s

        [later] Pet Lootable - dropType = 0 (user own) (15 second delay)
                     - dropType = 2 (no owner) - lootable instantly
                     - playerDrop false = pet lootable
                        // dropType 2 & playerDrop false = pet lootable
     */

    // Meso, Equip, Item, Item Qty - FFA Player Drop - Anyone can loot


    public static void botDropMeso(Character fakechar, int meso) {
        fakechar.getMap().spawnMesoDrop(meso, fakechar.getPosition(), fakechar, fakechar, true, (byte) 2, (short) 0);
    }

    public static void botDropEquip(Character fakechar, int itemId) {
        Item itemToDrop = BotLogic.generateCleanItemEquip(itemId);
        fakechar.getMap().spawnItemDrop(fakechar, fakechar, itemToDrop,
                fakechar.getPosition(), true, true);
    }

    public static void botDropItem(Character fakechar, int itemId) {
        Item itemToDrop = BotLogic.generateCleanItem(itemId);
        fakechar.getMap().spawnItemDrop(fakechar, fakechar, itemToDrop,
                fakechar.getPosition(), true, true);
    }

    private static final long PICKUP_GRACE_PERIOD_MS = 3000;

    public static MapItem botDropItemWithExpiry(Character fakechar, int itemId, boolean isEquip, long expiryMs) {
        Item itemToDrop = isEquip ? BotLogic.generateCleanItemEquip(itemId) : BotLogic.generateCleanItem(itemId);
        MapItem drop = fakechar.getMap().spawnItemDropNoExpire(fakechar, fakechar, itemToDrop,
                fakechar.getPosition(), true, true);
        if (drop != null && expiryMs > 0) {
            TimerManager.getInstance().schedule(() -> {
                if (drop.isPickedUp()) return;
                fakechar.getMap().broadcastMessage(
                        PacketCreator.removeItemFromMap(drop.getObjectId(), 0, 0),
                        drop.getPosition());
            }, expiryMs);

            TimerManager.getInstance().schedule(() -> {
                fakechar.getMap().makeDisappearItemFromMap(drop);
            }, expiryMs + PICKUP_GRACE_PERIOD_MS);
        }
        return drop;
    }

    public static void botDropItemQty(Character fakechar, int itemId, int qty) {
        Item itemToDrop = BotLogic.generateCleanItemWithQty(itemId, qty);
        fakechar.getMap().spawnItemDrop(fakechar, fakechar, itemToDrop,
                fakechar.getPosition(), true, true);
    }

    // Meso, Equip, Item, Item Qty - Throw to specific location - Anyone can loot
    // Set playerDrop = false for pet-lootable (by everybody I think)

    public static void botThrowMeso(Character fakechar, int meso, Point throwPos) {
        fakechar.getMap().spawnMesoDrop(meso, throwPos, fakechar, fakechar, false, (byte) 2, (short) 0);
    }

    public static void botThrowEquip(Character fakechar, Equip eq, Point throwPos) {
        fakechar.getMap().spawnItemDrop(fakechar, fakechar, eq,
                throwPos, true, false);
    }

    public static void botThrowEquip(Character fakechar, int itemId, Point throwPos) {
        Item itemToDrop = BotLogic.generateCleanItemEquip(itemId);
        fakechar.getMap().spawnItemDrop(fakechar, fakechar, itemToDrop,
                throwPos, true, false);
    }

    public static Item botThrowItem(Character fakechar, int itemId, Point throwPos) {
        Item itemToDrop = BotLogic.generateCleanItem(itemId);
        fakechar.getMap().spawnItemDrop(fakechar, fakechar, itemToDrop,
                throwPos, true, false);
        return itemToDrop;
    }

    public static MapItem botThrowItemNoExpire(Character fakechar, int itemId, Point throwPos) {
        Item itemToDrop = BotLogic.generateCleanItem(itemId);
        return fakechar.getMap().spawnItemDropNoExpire(fakechar, fakechar, itemToDrop,
                throwPos, true, false);
    }

    public static MapItem botThrowItemNoExpireOwnerOnly(Character fakechar, int itemId, Point throwPos) {
        Item itemToDrop = BotLogic.generateCleanItem(itemId);
        MapItem drop = fakechar.getMap().spawnItemDropNoExpire(fakechar, fakechar, itemToDrop,
                throwPos, false, false);
        if (drop != null) drop.setPermanentOwner(true);
        return drop;
    }

    public static void botDropItemQtyOwnerOnly(Character fakechar, int itemId, int qty) {
        Item itemToDrop = BotLogic.generateCleanItemWithQty(itemId, qty);
        MapItem drop = fakechar.getMap().spawnItemDropNoExpire(fakechar, fakechar, itemToDrop,
                fakechar.getPosition(), false, false);
        if (drop != null) drop.setPermanentOwner(true);
    }

    public static void botThrowItemQty(Character fakechar, int itemId, int qty, Point throwPos) {
        Item itemToDrop = BotLogic.generateCleanItemWithQty(itemId, qty);
        fakechar.getMap().spawnItemDrop(fakechar, fakechar, itemToDrop,
                throwPos, true, false);
    }

    public static void botThrowItemToOwner(Character dropper, int itemId, Point throwPos, Character owner) {
        Item itemToDrop = BotLogic.generateCleanItem(itemId);
        MapItem drop = dropper.getMap().spawnItemDropNoExpire(dropper, owner, itemToDrop,
                throwPos, false, false);
        if (drop != null) drop.setPermanentOwner(true);
    }

    // Meso, Equip, Item, Item Qty - Throw to Owner - Owner only can loot
    // dropType 0 = userOwn
    // playerDrop false = pet lootable

    public static void botThrowToOwnerMeso(Character fakechar, int meso, Character owner) {
        fakechar.getMap().spawnMesoDrop(meso, owner.getPosition(), fakechar, owner, false, (byte) 0, (short) 0);
    }

    public static void botThrowToOwnerEquip(Character fakechar, int itemId, Character owner) {
        Item itemToDrop = BotLogic.generateCleanItemEquip(itemId);
        fakechar.getMap().spawnItemDrop(fakechar, owner, itemToDrop,
                owner.getPosition(), false, false);
    }

    public static void botThrowToOwnerItem(Character fakechar, int itemId, Character owner) {
        Item itemToDrop = BotLogic.generateCleanItem(itemId);
        fakechar.getMap().spawnItemDrop(fakechar, owner, itemToDrop,
                owner.getPosition(), false, false);
    }

    public static void botThrowToOwnerItemQty(Character fakechar, int itemId, int qty, Character owner) {
        Item itemToDrop = BotLogic.generateCleanItemWithQty(itemId, qty);
        fakechar.getMap().spawnItemDrop(fakechar, owner, itemToDrop,
                owner.getPosition(), false, false);
    }

    // Throwing multiple items in a row (for display purposes)

    public static void botThrowEquipsInARow(Character fakechar, int[] items, Point centerPos, int spacing) {
        int itemCount = items.length;
        int startX = centerPos.x - (itemCount / 2) * spacing; // Calculate starting X position

        for (int i = 0; i < itemCount; i++) {
            int itemId = items[i];
            int adjustedX = startX + i * spacing; // Adjust X position based on index
            Point adjustedPos = new Point(adjustedX, centerPos.y); // Maintain the same Y position
            botThrowEquip(fakechar, itemId, adjustedPos);
        }
    }

    // Bot pickup commands

    // BotLoot at feet
    // BotPickUp
    public static void botLoot(Character fakechar, double range) {
        botLoot(fakechar, fakechar.getPosition(), range);
    }

    public static void botLoot(Character fakechar, Point itemLocation, double range) {
        // 1000 Range = next to feet // 3000 = 2.5 character widths length
        List<MapObject> items = fakechar.getMap().getMapObjectsInRange(itemLocation, range, Arrays.asList(MapObjectType.ITEM));
        for (MapObject item : items) {
            MapItem mapItem = (MapItem) item;
            final Packet pickupPacket = PacketCreator.removeItemFromMap(mapItem.getObjectId(), 2, fakechar.getId());
            fakechar.getMap().pickItemDrop(pickupPacket, mapItem);
        }
    }

    public static void botLootLocation(Character fakechar, Point itemLocation) {
        double range = 1000; // 1000 = Next to feet
        botLoot(fakechar, itemLocation, range);
    }

    // Bot loots their own items at item location position within specified range
    public static void botLootOwnerItems(Character fakechar, Point itemLocation, double range) {
        botLootTargetCharactersItems(fakechar, fakechar, itemLocation, range);
    }

    // Bot loots target character's items at item location within specified range
    public static void botLootTargetCharactersItems(Character fakechar, Character targetCharacter, Point itemLocation, double range) {
        List<MapObject> items = fakechar.getMap().getMapObjectsInRange(itemLocation, range, Arrays.asList(MapObjectType.ITEM));
        for (MapObject item : items) {
            MapItem mapItem = (MapItem) item;
            if (mapItem.getOwnerId() == targetCharacter.getId()) {
                final Packet pickupPacket = PacketCreator.removeItemFromMap(mapItem.getObjectId(), 2, fakechar.getId());
                fakechar.getMap().pickItemDrop(pickupPacket, mapItem);
            }
        }
    }

    // Deliberate blocking leaf primitive: per-item pickup stagger, item count is
    // data-driven. Callers (blackjack payouts, loot scripts) rely on the hold.
    public static void lootItemListOnFloor(Character fakechar, List<MapObject> items) {
        for (MapObject item : items) {
            MapItem mapItem = (MapItem) item;
            final Packet pickupPacket = PacketCreator.removeItemFromMap(mapItem.getObjectId(), 2, fakechar.getId());
            fakechar.getMap().pickItemDrop(pickupPacket, mapItem);
            BotHelpers.blockingSleep(100);
        }
    }

    public static void botLootSelectedItems(Character chr, int[] itemsToLoot) {
        List<MapObject> itemsOnFloor = checkForItemsOnFloor(chr, chr.getPosition(), 10000, itemsToLoot);
        if (!itemsOnFloor.isEmpty()) {
            DropCommands.lootItemListOnFloor(chr, itemsOnFloor);
        }
    }

    // Owner-protected drops (drop type != 2) become free-for-all after this long; matches vanilla.
    private static final long FFA_OWNER_PROTECT_MS = 15000;

    // True when a bot is entitled to loot this drop: its own drops, or a free-for-all drop (spawned with
    // no owner, or whose owner-protection window has expired). Lets bots grab their kills plus loot a real
    // player left on the floor, without stealing another player's still-protected drop.
    public static boolean botCanLoot(Character fakechar, MapItem mapItem) {
        if (mapItem == null || mapItem.isPickedUp() || mapItem.getPosition() == null) {
            return false;
        }
        if (mapItem.getOwnerId() == fakechar.getId()) {
            return true; // the bot's own drop
        }
        return mapItem.getDropType() == 2
                || System.currentTimeMillis() - mapItem.getDropTime() >= FFA_OWNER_PROTECT_MS;
    }

    // Pick up one specific drop (organic single-item loot, paced by the caller for a natural look).
    public static void botLootSingleDrop(Character fakechar, MapItem mapItem) {
        if (mapItem == null || mapItem.isPickedUp()) {
            return;
        }
        final Packet pickupPacket = PacketCreator.removeItemFromMap(mapItem.getObjectId(), 2, fakechar.getId());
        fakechar.getMap().pickItemDrop(pickupPacket, mapItem);
    }

    // Sweep all drops the bot is entitled to (own + free-for-all) within range. Used as a tidy-up when a
    // bot leaves a spot so it doesn't trail a pile; for in-combat looting use the staggered single pickups.
    public static void botLootOwnAndFreeForAll(Character fakechar, Point itemLocation, double range) {
        List<MapObject> items = fakechar.getMap().getMapObjectsInRange(itemLocation, range, Arrays.asList(MapObjectType.ITEM));
        for (MapObject item : items) {
            MapItem mapItem = (MapItem) item;
            if (botCanLoot(fakechar, mapItem)) {
                botLootSingleDrop(fakechar, mapItem);
            }
        }
    }

    // obsolete can delete
//    public static void jackpotRoulette(Character fakechar) {
//        List<Integer> random_drop_list = Arrays.asList();
//        Point center2 = fakechar.getPosition();
//        int length = generateRandomNumber(29, 41);
//        for (int currIndex = 0; currIndex < length; currIndex++) {
//            int item_id = getRandomId(random_drop_list);
////            botThrowItem(fakechar, item_id); // todo
//        }
//    }


}
