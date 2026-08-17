package soloMapling.FreeMarket;

import org.gms.client.inventory.Equip;

public class FMEquip {
    private Equip equip;
    private int price; // Already 2.147b limit accounted for

    public FMEquip(Equip equip, int price) {
        this.equip = equip;
        this.price = price;
    }

    // Constructor with only equip, sets default price to -1 or another placeholder
    public FMEquip(Equip equip) {
        this.equip = equip;
        this.price = -1; // -1 as a placeholder to indicate an unset price
    }

    public Equip getEquip() {
        return equip;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "FMEquip{" +
                "equip=" + equip +
                ", price=" + price +
                '}';
    }
}
