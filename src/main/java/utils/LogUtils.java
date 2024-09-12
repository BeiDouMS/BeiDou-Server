package utils;

import client.Character;
import client.inventory.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {
    private static final Logger dropAndPickLog = LoggerFactory.getLogger("dropAndPickLog");
    public static void dropItem(Character chr, Item item) {
        dropAndPickLog.info("[{}]{} 在 [{}]{} 丢出道具 {} x{}", chr.getId(), chr.getName(), chr.getMapId(), chr.getMap().getMapName(), item.getItemId(), item.getQuantity());
    }

    public static void pickUpItem(Character chr, Item item) {
        dropAndPickLog.info("[{}]{} 在 [{}]{} 捡到道具 {} x{} 由 [{}]{} {}", chr.getId(), chr.getName(), chr.getMapId(), chr.getMap().getMapName(), item.getItemId(), item.getQuantity(), item.getDropperId(), item.getDropperName(), item.getDropWay());
    }

    public static void dropMeso(Character chr, int quantity) {
        dropAndPickLog.info("[{}]{} 在 [{}]{} 丢出金币 x{}", chr.getId(), chr.getName(), chr.getMapId(), chr.getMap().getMapName(), quantity);
    }

    public static void pickUpMeso(Character chr, int quantity) {
        dropAndPickLog.info("[{}]{} 在 [{}]{} 捡到金币 x{}", chr.getId(), chr.getName(), chr.getMapId(), chr.getMap().getMapName(), quantity);
    }
}
