package soloMapling.ArtificialPlayer.BotTradeSystem;

import org.gms.client.Character;

public class TradeMessage {

    private final int tradeId;
    private final Character sender;
    private final String message;
    private final long timestamp;

    public TradeMessage(int tradeId, Character sender, String message, long timestamp) {
        this.tradeId = tradeId;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getTradeId() {
        return tradeId;
    }

    public Character getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" + tradeId + "] " + sender + ": " + message + " (" + timestamp + ")";
    }

}
