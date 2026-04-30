package org.gms.client.fake;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.*;

/**
 * 假人系统配置
 * <p>
 * 从 fake-player-config.yml 加载配置
 * </p>
 *
 * @author BeiDou
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "fake-player")
@PropertySource(value = "classpath:fake-player-config.yml", factory = YamlPropertySourceFactory.class)
public class FakePlayerConfig {

    /**
     * 是否启用假人系统
     */
    private boolean enabled = true;

    /**
     * 地图 ID -> 假人数量映射
     */
    private Map<Integer, Integer> maps = new HashMap<>();

    /**
     * 聊天配置
     */
    private Chat chat = new Chat();

    /**
     * 外观配置
     */
    private Appearance appearance = new Appearance();

    /**
     * 装备配置
     */
    private Equipment equipment = new Equipment();

    /**
     * 职业配置
     */
    private Jobs jobs = new Jobs();

    /**
     * 名字配置
     */
    private Names names = new Names();

    /**
     * 公会配置
     */
    private Guild guild = new Guild();

    /**
     * 人气配置
     */
    private Fame fame = new Fame();

    /**
     * 自动刷新配置
     */
    private AutoRefresh autoRefresh = new AutoRefresh();

    /**
     * 行为配置
     */
    private Behavior behavior = new Behavior();

    /**
     * 玩家交互配置
     */
    private Interaction interaction = new Interaction();

    /**
     * 聊天配置
     */
    @Data
    public static class Chat {
        private boolean enabled = true;
        private double probability = 0.1;
        private int cooldown = 30;
        private List<String> messages = new ArrayList<>();
    }

    /**
     * 外观配置
     */
    @Data
    public static class Appearance {
        private FaceIds faces = new FaceIds();
        private HairIds hairs = new HairIds();
        private Map<String, Integer> skinWeights = new LinkedHashMap<>();
    }

    @Data
    public static class FaceIds {
        private List<Integer> male = new ArrayList<>();
        private List<Integer> female = new ArrayList<>();
    }

    @Data
    public static class HairIds {
        private List<Integer> male = new ArrayList<>();
        private List<Integer> female = new ArrayList<>();
    }

    /**
     * 装备配置
     */
    @Data
    public static class Equipment {
        private List<Integer> hats = new ArrayList<>();
        private List<Integer> faceAccessories = new ArrayList<>();
        private List<Integer> eyeAccessories = new ArrayList<>();
        private List<Integer> earrings = new ArrayList<>();
        private List<Integer> tops = new ArrayList<>();
        private List<Integer> overalls = new ArrayList<>();
        private List<Integer> bottoms = new ArrayList<>();
        private List<Integer> shoes = new ArrayList<>();
        private List<Integer> gloves = new ArrayList<>();
        private List<Integer> shields = new ArrayList<>();
        private List<Integer> capes = new ArrayList<>();
        private Weapons weapons = new Weapons();
    }

    @Data
    public static class Weapons {
        private List<Integer> oneHandedSword = new ArrayList<>();
        private List<Integer> oneHandedAxe = new ArrayList<>();
        private List<Integer> oneHandedBlunt = new ArrayList<>();
        private List<Integer> dagger = new ArrayList<>();
        private List<Integer> dualBlade = new ArrayList<>();
        private List<Integer> twoHandedSword = new ArrayList<>();
        private List<Integer> twoHandedAxe = new ArrayList<>();
        private List<Integer> twoHandedBlunt = new ArrayList<>();
        private List<Integer> spear = new ArrayList<>();
        private List<Integer> polearm = new ArrayList<>();
        private List<Integer> bow = new ArrayList<>();
        private List<Integer> crossbow = new ArrayList<>();
        private List<Integer> claw = new ArrayList<>();
        private List<Integer> knuckle = new ArrayList<>();
        private List<Integer> gun = new ArrayList<>();
    }

    /**
     * 职业配置
     */
    @Data
    public static class Jobs {
        private List<String> firstJob = new ArrayList<>();
        private SecondJob secondJob = new SecondJob();
        private ThirdJob thirdJob = new ThirdJob();
        private FourthJob fourthJob = new FourthJob();
        private Map<String, LevelRange> levelRanges = new HashMap<>();
    }

    @Data
    public static class SecondJob {
        private List<String> warrior = new ArrayList<>();
        private List<String> magician = new ArrayList<>();
        private List<String> bowman = new ArrayList<>();
        private List<String> thief = new ArrayList<>();
        private List<String> pirate = new ArrayList<>();
    }

    @Data
    public static class ThirdJob {
        private List<String> warrior = new ArrayList<>();
        private List<String> magician = new ArrayList<>();
        private List<String> bowman = new ArrayList<>();
        private List<String> thief = new ArrayList<>();
        private List<String> pirate = new ArrayList<>();
    }

    @Data
    public static class FourthJob {
        private List<String> warrior = new ArrayList<>();
        private List<String> magician = new ArrayList<>();
        private List<String> bowman = new ArrayList<>();
        private List<String> thief = new ArrayList<>();
        private List<String> pirate = new ArrayList<>();
    }

    @Data
    public static class LevelRange {
        private int min;
        private int max;
    }

    /**
     * 名字配置
     */
    @Data
    public static class Names {
        private NameList prefixes = new NameList();
        private NameList suffixes = new NameList();
        private boolean addNumberSuffix = true;
        private NumberRange numberRange = new NumberRange();
    }

    @Data
    public static class NameList {
        private List<String> male = new ArrayList<>();
        private List<String> female = new ArrayList<>();
    }

    @Data
    public static class NumberRange {
        private int min = 100;
        private int max = 9999;
    }

    /**
     * 公会配置
     */
    @Data
    public static class Guild {
        private boolean enabled = true;
        private double probability = 0.7;
        private List<String> names = new ArrayList<>();
    }

    /**
     * 人气配置
     */
    @Data
    public static class Fame {
        private int min = -100;
        private int max = 1000;
    }

    /**
     * 自动刷新配置
     */
    @Data
    public static class AutoRefresh {
        private boolean enabled = false;
        private int interval = 60;
    }

    /**
     * 行为配置
     */
    @Data
    public static class Behavior {
        private boolean allowMovement = false;
        private int movementRange = 200;
        private int movementInterval = 10;
    }

    /**
     * 玩家交互配置
     */
    @Data
    public static class Interaction {
        private boolean showOnEnter = true;
        private boolean allowInspect = true;
        private InspectInfo inspectInfo = new InspectInfo();
    }

    @Data
    public static class InspectInfo {
        private boolean showLevel = true;
        private boolean showJob = true;
        private boolean showFame = true;
        private boolean showGuild = true;
    }

    /**
     * 检查指定地图是否需要假人
     *
     * @param mapId 地图 ID
     * @return 如果需要假人返回 true
     */
    public boolean shouldSpawnInMap(int mapId) {
        return enabled && maps.containsKey(mapId) && maps.get(mapId) > 0;
    }

    /**
     * 获取指定地图的假人数量
     *
     * @param mapId 地图 ID
     * @return 假人数量
     */
    public int getFakeCount(int mapId) {
        return maps.getOrDefault(mapId, 0);
    }
}
