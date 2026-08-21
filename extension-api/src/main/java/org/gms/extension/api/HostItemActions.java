package org.gms.extension.api;

/**
 * Authoritative host operations for map loot and inventory-backed player drops.
 * Character and map-item ids are used so plugins do not depend on engine types.
 */
public interface HostItemActions {

    PickupResult pickup(int characterId, int mapItemObjectId);

    /**
     * Drops an item from a positive backpack slot to a character on the same map.
     * Inventory type uses the host protocol values: equip=1, use=2, setup=3, etc=4.
     */
    DropResult dropToCharacter(
            int sourceCharacterId,
            int targetCharacterId,
            int inventoryType,
            int slot,
            int quantity
    );

    record PickupResult(boolean success, String code, int itemId, int quantity) {
        public static PickupResult succeeded(int itemId, int quantity) {
            return new PickupResult(true, "OK", itemId, quantity);
        }

        public static PickupResult failed(String code) {
            return new PickupResult(false, code, 0, 0);
        }
    }

    record DropResult(
            boolean success,
            String code,
            int itemId,
            int quantity,
            int mapItemObjectId
    ) {
        public static DropResult succeeded(int itemId, int quantity, int mapItemObjectId) {
            return new DropResult(true, "OK", itemId, quantity, mapItemObjectId);
        }

        public static DropResult failed(String code) {
            return new DropResult(false, code, 0, 0, 0);
        }
    }
}
