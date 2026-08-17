package soloMapling.ArtificialPlayer.BotTradeSystem;

import org.gms.client.Character;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TradeChat {

    private final BlockingQueue<TradeMessage> messageQueue = new LinkedBlockingQueue<>();
    private final int tradeId;

    public TradeChat(int tradeId) {
        this.tradeId = tradeId;
    }

    protected void addMessage(Character sender, String message) {
        messageQueue.add(new TradeMessage(tradeId, sender, message, System.currentTimeMillis()));
    }

    protected TradeMessage getMessage() throws InterruptedException {
        return messageQueue.take(); // Blocks until a message is available
    }

    protected TradeMessage getMessageNonBlocking() throws InterruptedException {
        return messageQueue.poll(); // Blocks until a message is available
    }

    protected TradeMessage getMessageWithTimeout(long timeout, TimeUnit unit) throws InterruptedException {
        return messageQueue.poll(timeout, unit); // Waits for a message within the timeout period
    }

    protected TradeMessage peek() {
        return messageQueue.peek(); // Returns the next message without removing it
    }

    protected BlockingQueue<TradeMessage> getQueue() {
        return messageQueue; // Returns the entire queue reference
    }

    protected boolean isEmpty() {
        return messageQueue.isEmpty();
    }

    protected int getTradeId() {
        return tradeId;
    }

}
