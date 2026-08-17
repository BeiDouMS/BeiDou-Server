package soloMapling.ArtificialPlayer.BotMessagingSystem;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;

public class ChatMessage {
    private final Character sender;
    private final String content;
    private MapleMap map;
    private final long timestamp;

    public ChatMessage(Character sender, String content) {
        this.sender = sender;
        this.content = content;
        this.map = this.sender.getMap();
        this.timestamp = System.currentTimeMillis();
    }

    public Character getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    protected MapleMap getMap() {
        return map;
    }

    protected long getTimestamp() {
        return timestamp;
    }
}