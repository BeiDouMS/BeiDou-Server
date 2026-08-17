package soloMapling.ArtificialPlayer.BotTradeSystem;

public class ItemQuantity {
    private final int itemId;
    private final int quantity;

    public ItemQuantity(int itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity > 0 ? quantity : 1; // Default to 1 if invalid quantity
    }

    public int getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }
}