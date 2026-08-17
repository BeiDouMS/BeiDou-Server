package soloMapling.FreeMarket;


public class FMItem {
    private int itemId;
    private int price; // Already 2.147b limit accounted for
    private int qty;

    public FMItem(int itemId, int price, int qty) {
        this.itemId = itemId;
        this.price = price;
        this.qty = qty;
    }

    // Constructor with only equip, sets default price to -1 or another placeholder
    public FMItem(int itemId) {
        this.itemId = itemId;
        this.price = -1; // -1 as a placeholder to indicate an unset price
        this.qty = 1;
    }

    public int getItemId() {
        return itemId;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
}

