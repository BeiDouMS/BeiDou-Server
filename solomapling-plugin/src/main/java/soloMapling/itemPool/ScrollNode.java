package soloMapling.itemPool;

import java.util.List;
import java.util.Map;

public class ScrollNode extends ItemNode {
    private int successRate;
    private int statBonus;

    public ScrollNode(String item, List<Integer> variantId, Map<Integer, String> tier, Map<Integer, Integer> price,
                      int successRate, int statBonus) {
        super(item, variantId, tier, price);
        this.successRate = successRate;
        this.statBonus = statBonus;
    }

    public int getSuccessRate() {
        return this.successRate;
    }

    public int getStatBonus() {
        return this.statBonus;
    }
}
