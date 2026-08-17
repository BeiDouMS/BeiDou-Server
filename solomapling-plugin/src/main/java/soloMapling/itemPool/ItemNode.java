package soloMapling.itemPool;

import java.util.List;
import java.util.Map;

import static soloMapling.itemPool.ItemSelector.getPriceForVersion;

public class ItemNode {
    private String item;
    private List<Integer> variantId;
    private Map<Integer, String> tier;
    private Map<Integer, Integer> price;

    // Constructor to initialize fields directly
    public ItemNode(String item, List<Integer> variantId, Map<Integer, String> tier, Map<Integer, Integer> price) {
        this.item = item;
        this.variantId = variantId;
        this.tier = tier;
        this.price = price;
    }

    public String getItem() {
        return this.item;
    }

    public List<Integer> getVariantId() {
        return this.variantId;
    }

    public Map<Integer, String> getTier() {
        return this.tier;
    }

    public Map<Integer, Integer> getPriceMap() {
        return this.price;
    }

    public int getCurrentPrice() {
        return getPriceForVersion(this.getPriceMap());
    }
}

