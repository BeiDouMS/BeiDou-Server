package soloMapling.itemPool;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import com.esotericsoftware.yamlbeans.YamlReader;


public class ItemQuantityConfig {
    public static class TierRange {
        public int min;
        public int max;
    }

    public static class ItemType {
        public Map<String, TierRange> tiers;
    }

    public Map<String, ItemType> itemQuantities;


    public static ItemQuantityConfig readYaml(String filePath) {
        try {
            YamlReader reader = new YamlReader(new FileReader(filePath));
            return reader.read(ItemQuantityConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

