package soloMapling.FreeMarket.ShopOfferSystem;

public class HaggleSession {

    private static final int MAX_ATTEMPTS = 3;
    private static final long EXPIRY_MS = 60_000;

    private final int playerId;
    private final int shopOwnerId;
    private int attempts;
    private long counterPrice;
    private long lastActivityTime;

    public HaggleSession(int playerId, int shopOwnerId) {
        this.playerId = playerId;
        this.shopOwnerId = shopOwnerId;
        this.attempts = 0;
        this.counterPrice = -1;
        this.lastActivityTime = System.currentTimeMillis();
    }

    public int getPlayerId() { return playerId; }
    public int getShopOwnerId() { return shopOwnerId; }
    public int getAttempts() { return attempts; }
    public long getCounterPrice() { return counterPrice; }

    public void incrementAttempt() {
        attempts++;
        lastActivityTime = System.currentTimeMillis();
    }

    public void setCounterPrice(long price) {
        this.counterPrice = price;
    }

    public boolean hasExceededAttempts() {
        return attempts >= MAX_ATTEMPTS;
    }

    public boolean hasCounterPending() {
        return counterPrice > 0;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - lastActivityTime > EXPIRY_MS;
    }

    public void touch() {
        lastActivityTime = System.currentTimeMillis();
    }
}
