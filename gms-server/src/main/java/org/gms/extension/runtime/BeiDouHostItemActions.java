package org.gms.extension.runtime;

import org.gms.client.Character;
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.ModifyInventory;
import org.gms.constants.inventory.ItemConstants;
import org.gms.extension.api.HostItemActions;
import org.gms.net.server.Server;
import org.gms.server.ItemInformationProvider;
import org.gms.server.maps.MapItem;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.FieldLimit;
import org.gms.util.PacketCreator;

import java.util.Collections;
import java.util.function.IntFunction;

public final class BeiDouHostItemActions implements HostItemActions {
    private static final short FORBIDDEN_FLAGS = ItemConstants.LOCK
            | ItemConstants.UNTRADEABLE
            | ItemConstants.ACCOUNT_SHARING
            | ItemConstants.MERGE_UNTRADEABLE;

    private final IntFunction<Character> characterResolver;

    public BeiDouHostItemActions() {
        this(BeiDouHostItemActions::findOnlineCharacter);
    }

    BeiDouHostItemActions(IntFunction<Character> characterResolver) {
        this.characterResolver = characterResolver;
    }

    @Override
    public PickupResult pickup(int characterId, int mapItemObjectId) {
        Character character = characterResolver.apply(characterId);
        if (character == null || character.getMap() == null) {
            return PickupResult.failed("CHARACTER_NOT_FOUND");
        }

        MapObject object = character.getMap().getMapObject(mapItemObjectId);
        if (!(object instanceof MapItem mapItem)) {
            return PickupResult.failed("MAP_ITEM_NOT_FOUND");
        }

        int itemId;
        int quantity;
        mapItem.lockItem();
        try {
            if (mapItem.isPickedUp()) {
                return PickupResult.failed("ALREADY_PICKED_UP");
            }
            if (System.currentTimeMillis() - mapItem.getDropTime() < 400) {
                return PickupResult.failed("DROP_NOT_READY");
            }
            itemId = mapItem.getItemId();
            quantity = mapItem.getMeso() > 0 ? mapItem.getMeso() : mapItem.getItem().getQuantity();
        } finally {
            mapItem.unlockItem();
        }

        // This is the engine's normal authoritative path. It performs ownership,
        // quest and inventory checks and calls InventoryManipulator.addFromDrop.
        character.pickupItem(mapItem);

        mapItem.lockItem();
        try {
            return mapItem.isPickedUp()
                    ? PickupResult.succeeded(itemId, quantity)
                    : PickupResult.failed("PICKUP_REJECTED");
        } finally {
            mapItem.unlockItem();
        }
    }

    @Override
    public DropResult dropToCharacter(
            int sourceCharacterId,
            int targetCharacterId,
            int inventoryType,
            int slot,
            int quantity
    ) {
        Character source = characterResolver.apply(sourceCharacterId);
        Character target = characterResolver.apply(targetCharacterId);
        if (source == null) {
            return DropResult.failed("SOURCE_NOT_FOUND");
        }
        if (target == null) {
            return DropResult.failed("TARGET_NOT_FOUND");
        }
        MapleMap map = source.getMap();
        if (map == null || target.getMap() != map) {
            return DropResult.failed("NOT_ON_SAME_MAP");
        }
        if (FieldLimit.DROP_LIMIT.check(map.getFieldLimit())) {
            return DropResult.failed("MAP_DISALLOWS_DROPS");
        }
        if (quantity <= 0 || slot <= 0 || slot > Short.MAX_VALUE) {
            return DropResult.failed("INVALID_QUANTITY_OR_SLOT");
        }

        InventoryType type = InventoryType.getByType((byte) inventoryType);
        if (type == InventoryType.UNDEFINED
                || type == InventoryType.EQUIPPED
                || type == InventoryType.CASH
                || type == InventoryType.CANHOLD) {
            return DropResult.failed("INVALID_INVENTORY");
        }

        Inventory inventory = source.getInventory(type);
        inventory.lockInventory();
        try {
            Item sourceItem = inventory.getItem((short) slot);
            if (sourceItem == null) {
                return DropResult.failed("SLOT_EMPTY");
            }
            String restriction = dropRestriction(sourceItem);
            if (restriction != null) {
                return DropResult.failed(restriction);
            }
            if (sourceItem.getQuantity() < quantity
                    || (sourceItem.getItemType() == 1 && quantity != 1)) {
                return DropResult.failed("INVALID_QUANTITY");
            }

            short originalQuantity = sourceItem.getQuantity();
            boolean wholeStack = quantity == originalQuantity;
            Item droppedItem = wholeStack ? sourceItem : sourceItem.copy();
            droppedItem.setQuantity((short) quantity);

            if (wholeStack) {
                inventory.removeSlot((short) slot);
            } else {
                sourceItem.setQuantity((short) (originalQuantity - quantity));
            }

            MapItem mapItem;
            try {
                mapItem = map.spawnOwnerOnlyItemDrop(source, target, droppedItem, target.getPosition());
            } catch (RuntimeException e) {
                // A thrown map-spawn may have partially registered the object; restoring
                // here could duplicate the item, so fail closed and leave it removed.
                return DropResult.failed("DROP_SPAWN_FAILED");
            }
            if (mapItem == null) {
                if (wholeStack) {
                    sourceItem.setPosition((short) slot);
                    inventory.addItemFromDB(sourceItem);
                } else {
                    sourceItem.setQuantity(originalQuantity);
                }
                return DropResult.failed("DROP_SPAWN_FAILED");
            }

            source.getClient().sendPacket(PacketCreator.modifyInventory(
                    true,
                    Collections.singletonList(new ModifyInventory(wholeStack ? 3 : 1, sourceItem))));
            return DropResult.succeeded(droppedItem.getItemId(), quantity, mapItem.getObjectId());
        } finally {
            inventory.unlockInventory();
        }
    }

    static String dropRestriction(Item item) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        return dropRestriction(
                ii.isCash(item.getItemId()),
                ii.isQuestItem(item.getItemId()) || ii.isDropRestricted(item.getItemId()),
                item.getPetId() >= 0,
                item.isUntradeable(),
                item.getFlag());
    }

    static String dropRestriction(
            boolean cash,
            boolean questOrDropRestricted,
            boolean pet,
            boolean untradeable,
            short flags
    ) {
        if (cash) {
            return "CASH_ITEM";
        }
        if (questOrDropRestricted) {
            return "DROP_RESTRICTED";
        }
        if (pet) {
            return "PET_ITEM";
        }
        if (untradeable || (flags & FORBIDDEN_FLAGS) != 0) {
            return "BOUND_ITEM";
        }
        return null;
    }

    private static Character findOnlineCharacter(int characterId) {
        return Server.getInstance().getWorlds().stream()
                .map(world -> world.getPlayerStorage().getCharacterById(characterId))
                .filter(character -> character != null)
                .findFirst()
                .orElse(null);
    }
}
