package soloMapling.ArtificialPlayer.BotTradeSystem;

import org.gms.client.inventory.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotTradeWants {
    // What the bot is seeking for in his trades
    // e.g. "Looking for 5m", "Looking for Onyx Apple"

    private int mesoWanted = 1;
    private List<ItemQuantity> itemsWanted; // Changed from List<Integer>
    private int mesoOffering;

    public BotTradeWants() {
        itemsWanted = new ArrayList<>(9);
    }

    // Method to add an item with default quantity of 1
    public void addItemWanted(int itemId) {
        addItemWanted(itemId, 1);
    }

    // Method to add an item with specified quantity
    public void addItemWanted(int itemId, int quantity) {
        itemsWanted.add(new ItemQuantity(itemId, quantity));
    }

    // Method to get an item quantity by index
    public ItemQuantity getItemQuantityAt(int index) {
        if (index >= 0 && index < itemsWanted.size()) {
            return itemsWanted.get(index);
        }
        return null;
    }

    protected List<Integer> getListItemsWanted() {
        List<Integer> itemIds = new ArrayList<>(itemsWanted.size());
        for (ItemQuantity iq : itemsWanted) {
            itemIds.add(iq.getItemId());
        }
        return itemIds;
    }

    protected List<ItemQuantity> getItemsWanted() {
        return itemsWanted;
    }

    protected int getMesoWanted() {
        return mesoWanted;
    }

    public void setMesoWanted(int mesoWanted) {
        this.mesoWanted = mesoWanted;
    }

    protected void setItemWanted(int itemIdWanted) {
        addItemWanted(itemIdWanted);
    }

    protected void setItemsWanted(List<Integer> itemIds) {
        for (Integer itemId : itemIds) {
            setItemWanted(itemId);
        }
    }

    public void setWants(int mesoWanted, int singleItemWantItemId) {
        setWants(mesoWanted, List.of(singleItemWantItemId));
    }

    public void setWants(int mesoWanted, List<Integer> itemIds) {
        resetTradeWants();
        setMesoWanted(mesoWanted);
        setItemsWanted(itemIds);
    }

    public void setWants(int singleItemWantItemId) {
        setWants(0, List.of(singleItemWantItemId));
    }

    public void setWants(List<Integer> itemIds) {
        setWants(0, itemIds);
    }

    public boolean verifyTrade(int meso, List<Item> partnerItems) {
        return verifySufficientMeso(meso) && verifySufficientItems(partnerItems);
    }

    /**
     * Verifies if the provided mesos amount is sufficient compared to the required amount.
     * Allows for a small shortfall based on the magnitude of the required amount.
     *
     * @param mesos The amount of mesos provided
     * @return true if the amount is sufficient or reasonably close, false otherwise
     */
    protected boolean verifySufficientMeso(int mesos) {
        int mesoWanted = getMesoWanted();

        // If exact or greater amount, always return true
        if (mesos >= mesoWanted) {
            return true;
        }

        // Calculate shortfall
        int shortfall = mesoWanted - mesos;

        // Define acceptable shortfall based on magnitude of the required amount
        double acceptableShortfallPercentage;

        if (mesoWanted >= 1_000_000_000) {        // Billions
            acceptableShortfallPercentage = 0.01;  // 1% tolerance
        } else if (mesoWanted >= 100_000_000) {   // Hundred millions
            acceptableShortfallPercentage = 0.02;  // 2% tolerance
        } else if (mesoWanted >= 10_000_000) {    // Tens of millions
            acceptableShortfallPercentage = 0.05;  // 5% tolerance
        } else if (mesoWanted >= 1_000_000) {     // Millions
            acceptableShortfallPercentage = 0.1;   // 10% tolerance
        } else if (mesoWanted >= 100_000) {       // Hundred thousands
            acceptableShortfallPercentage = 0.15;  // 15% tolerance
        } else if (mesoWanted >= 10_000) {        // Tens of thousands
            acceptableShortfallPercentage = 0.2;   // 20% tolerance
        } else {                                  // Low amounts
            acceptableShortfallPercentage = 0.0;   // No tolerance for small amounts
        }

        int acceptableShortfall = (int)(mesoWanted * acceptableShortfallPercentage);

        return shortfall <= acceptableShortfall;
    }

//    protected boolean verifySufficientItems(List<Item> partnerItems) {
//        if (itemsWanted == null || itemsWanted.isEmpty()) {
//            return true;
//        }
//
//        if (partnerItems == null || partnerItems.isEmpty()) {
//            return false;
//        }
//
//        // Check if each wanted item exists in the partner items
//        for (Integer wantedItem : itemsWanted) {
//            boolean found = false;
//            int wantedItemId = wantedItem;
//
//            for (Item partnerItem : partnerItems) {
//                if (wantedItemId == (partnerItem.getItemId())) {
//                    found = true;
//                    break;
//                }
//            }
//
//            if (!found) {
//                return false; // Missing at least one wanted item
//            }
//        }
//
//        return true;
//    }

    protected boolean verifySufficientItems(List<Item> partnerItems) {
        if (itemsWanted == null || itemsWanted.isEmpty()) {
            return true;
        }

        if (partnerItems == null || partnerItems.isEmpty()) {
            return false;
        }

        // Create a map to count each item in partner's offering
        Map<Integer, Integer> partnerItemCounts = new HashMap<>();
        for (Item partnerItem : partnerItems) {
            int itemId = partnerItem.getItemId();
            // Get current count or default to 0 if not in map yet
            int currentCount = partnerItemCounts.getOrDefault(itemId, 0);
            // For stackable items, add the quantity; for non-stackable items, increment by 1
            int quantityToAdd = 1;
            if (partnerItem.getQuantity() > 1) {
                quantityToAdd = partnerItem.getQuantity();
            }
            partnerItemCounts.put(itemId, currentCount + quantityToAdd);
        }

        // Check if each wanted item exists in sufficient quantity
        for (ItemQuantity wantedItem : itemsWanted) {
            int wantedItemId = wantedItem.getItemId();
            int wantedQuantity = wantedItem.getQuantity();

            // Check if partner has this item and in sufficient quantity
            int availableQuantity = partnerItemCounts.getOrDefault(wantedItemId, 0);

            if (availableQuantity < wantedQuantity) {
                return false; // Insufficient quantity for this item
            }
        }

        return true;
    }

    public void resetTradeWants() {
        setMesoWanted(4206969);
        itemsWanted = new ArrayList<>(9);
    }

    /**
     * Gets the amount of mesos the bot is offering when buying items
     * (as opposed to getMesoWanted() which is what the bot wants when selling)
     */
    public int getMesoOffering() {
        return mesoOffering; // You'll need to add this field
    }

    /**
     * Sets the amount of mesos the bot is offering when buying items
     */
    public void setMesoOffering(int mesos) {
        this.mesoOffering = mesos;
    }

//    /**
//     * Gets the list of items the bot is offering in trade
//     * (as opposed to the items it wants)
//     */
//    public List<Item> getItemsOffering() {
//        return itemsOffering; // You'll need to add this field
//    }
//
//    /**
//     * Sets the items the bot is offering in trade
//     */
//    public void setItemsOffering(List<Item> items) {
//        this.itemsOffering = items;
//    }

//    /**
//     * Verifies that the partner has offered the exact items we want to buy
//     */
//    public boolean verifyItemsOffered(List<Item> partnerItems) {
//        if (partnerItems == null || partnerItems.isEmpty()) {
//            return false;
//        }
//
//        // Check that all items we want are present
//        for (Integer itemId : listItemsWanted) {
//            boolean found = false;
//            for (Item item : partnerItems) {
//                if (item.getItemId() == itemId) {
//                    found = true;
//                    break;
//                }
//            }
//            if (!found) return false;
//        }
//
//        return true;
//    }

}
