package soloMapling.server.EventMessageSystem;

import org.gms.client.Character;
import org.gms.client.inventory.Item;

/*
Creates events based on type
 */

public class EventFactory {

    public static GameEvent createGachaponEvent(Character mapleCharacter, Item item) {
        return new GameEvent(mapleCharacter, EventType.GACHAPON_REWARD, null, item, null);
    }

    public static GameEvent createLevelUpEvent(Character mapleCharacter) {
        return new GameEvent(mapleCharacter, EventType.LEVEL_UP, null, null, null);
    }

    public static GameEvent createScrollEvent(Character mapleCharacter, Item item, boolean success) {
        return new GameEvent(mapleCharacter, EventType.SCROLLING, null, item, success);
    }

    public static GameEvent createItemMegaphoneEvent(Character mapleCharacter, Item item, String message) {
        return new GameEvent(mapleCharacter, EventType.ITEM_MEGAPHONE, message, item, null);
    }

    public static GameEvent createChatMegaphoneEvent(Character mapleCharacter, String message) {
        return new GameEvent(mapleCharacter, EventType.CHAT_MEGAPHONE, message, null, null);
    }

    public static GameEvent createChatGeneralEvent(Character mapleCharacter, String message) {
        return new GameEvent(mapleCharacter, EventType.CHAT_GENERAL, message, null, null);
    }

}
