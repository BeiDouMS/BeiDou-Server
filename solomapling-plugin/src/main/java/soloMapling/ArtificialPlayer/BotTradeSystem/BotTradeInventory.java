package soloMapling.ArtificialPlayer.BotTradeSystem;

import org.gms.client.inventory.Item;

import java.util.ArrayList;
import java.util.List;

public class BotTradeInventory {

    private List<Item> itemsForSale;

    public BotTradeInventory() {
        itemsForSale = new ArrayList<>(9);
    }

    protected List<Item> getListItemsForSale() {
        return this.itemsForSale;
    }

    public void setItemForSaleMain(Item item) {
        if (getListItemsForSale().isEmpty()) {
            getListItemsForSale().add(item); // Add if empty
        } else {
            getListItemsForSale().set(0, item); // Replace if there's an item at index 0
        }
    }

    protected void addItemForSale(Item item) {
        getListItemsForSale().add(item);
    }

    protected void removeItemForSale(Item item) {
        getListItemsForSale().remove(item);
    }

    public Item getMainItemForSale() {
        if (!getListItemsForSale().isEmpty()) {
            return getListItemsForSale().getFirst();
        }
        return null;
    }

    protected void resetItemsForSale() {
        this.itemsForSale = new ArrayList(9);
    }

}
