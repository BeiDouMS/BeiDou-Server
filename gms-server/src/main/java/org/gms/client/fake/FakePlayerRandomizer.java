package org.gms.client.fake;

import org.gms.client.Job;
import org.gms.client.SkinColor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 假人随机生成器
 * <p>
 * 从名字库文件加载玩家名和公会名，支持多种生成方式
 * </p>
 *
 * @author BeiDou
 * @since 1.0.0
 */
@Slf4j
public final class FakePlayerRandomizer {

    /**
     * 名字库文件路径
     */
    private static final String PLAYER_NAMES_FILE = "names/player-names.txt";
    private static final String GUILD_NAMES_FILE = "names/guild-names.txt";

    /**
     * 从文件加载的名字库
     */
    private static final List<String> PLAYER_NAMES = loadNames(PLAYER_NAMES_FILE);
    private static final List<String> GUILD_NAMES = loadGuildNames(GUILD_NAMES_FILE);

    /**
     * 职业池
     */
    private static final Job[] JOBS = {
            Job.WARRIOR, Job.MAGICIAN, Job.BOWMAN, Job.THIEF, Job.PIRATE,
            Job.FIGHTER, Job.PAGE, Job.SPEARMAN,
            Job.FP_WIZARD, Job.IL_WIZARD, Job.CLERIC,
            Job.HUNTER, Job.CROSSBOWMAN,
            Job.ASSASSIN, Job.BANDIT,
            Job.BRAWLER, Job.GUNSLINGER,
            Job.CRUSADER, Job.WHITEKNIGHT, Job.DRAGONKNIGHT,
            Job.FP_MAGE, Job.IL_MAGE, Job.PRIEST,
            Job.RANGER, Job.SNIPER,
            Job.HERMIT, Job.CHIEFBANDIT,
            Job.MARAUDER, Job.OUTLAW,
            Job.HERO, Job.PALADIN, Job.DARKKNIGHT,
            Job.FP_ARCHMAGE, Job.IL_ARCHMAGE, Job.BISHOP,
            Job.BOWMASTER, Job.MARKSMAN,
            Job.NIGHTLORD, Job.SHADOWER,
            Job.BUCCANEER, Job.CORSAIR
    };

    private static final int[][] JOB_LEVEL_RANGE = {
            {10, 30}, {30, 70}, {70, 120}, {120, 200}
    };

    private static final int[] FACES = {
            20000, 20001, 20002, 20003, 20004, 20005, 20006, 20007, 20008, 20009,
            20010, 20011, 20012, 20013, 20014, 20015, 20016, 20017, 20018, 20019,
            20020, 20021, 20022, 20023, 20024, 20025, 20026, 20027, 20028, 20029
    };

    private static final int[] HAIRS = {
            30000, 30010, 30020, 30030, 30040, 30050, 30060, 30070, 30080, 30090,
            30100, 30110, 30120, 30130, 30140, 30150, 30160, 30170, 30180, 30190,
            30200, 30210, 30220, 30230, 30240, 30250, 30260, 30270, 30280, 30290
    };

    private static final int[][] EQUIP_POOLS = {
            {1002000, 1002001, 1002002, 1002003, 1002004, 1002005, 1002006, 1002007},
            {1012000, 1012001, 1012002, 1012003},
            {1022000, 1022001, 1022002, 1022003},
            {1032000, 1032001, 1032002, 1032003},
            {1040000, 1040001, 1040002, 1040003, 1040004, 1040005},
            {1050000, 1050001, 1050002, 1050003},
            {1060000, 1060001, 1060002, 1060003},
            {1072000, 1072001, 1072002, 1072003, 1072004, 1072005},
            {1082000, 1082001, 1082002, 1082003},
            {1092000, 1092001, 1092002, 1092003},
            {1102000, 1102001, 1102002, 1102003}
    };

    private static final int[][] WEAPON_POOLS = {
            {1302000, 1302001, 1302002, 1302003},
            {1312000, 1312001, 1312002},
            {1322000, 1322001, 1322002},
            {1332000, 1332001, 1332002},
            {1342000, 1342001},
            {1452000, 1452001, 1452002},
            {1462000, 1462001, 1462002},
            {1432000, 1432001, 1432002},
            {1442000, 1442001, 1442002},
            {1472000, 1472001, 1472002},
            {1482000, 1482001, 1482002},
            {1492000, 1492001, 1492002}
    };


    /**
     * 从 classpath 加载名字文件（忽略 # 注释和空行）
     */
    private static List<String> loadNames(String resourcePath) {
        List<String> names = new ArrayList<>();
        try (InputStream is = FakePlayerRandomizer.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.warn("名字库文件不存在: {}，使用默认词库", resourcePath);
                return names;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        names.add(line);
                    }
                }
            }
            log.info("从 {} 加载了 {} 个名字", resourcePath, names.size());
        } catch (IOException e) {
            log.error("加载名字库失败: {}", resourcePath, e);
        }
        return names;
    }

    private static List<String> loadGuildNames(String resourcePath) {
        return loadNames(resourcePath);
    }

    private FakePlayerRandomizer() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 生成随机假人
     */
    public static FakePlayer generateRandom() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        String name = generateRandomName(rand);
        FakePlayer player = new FakePlayer(name);

        Job job = JOBS[rand.nextInt(JOBS.length)];
        player.setJob(job);
        player.setLevel(generateLevelForJob(job, rand));
        player.setFame(rand.nextInt(-100, 1001));
        player.setLook(generateRandomLook(rand));

        if (rand.nextDouble() < 0.7) {
            player.setGuildId(rand.nextInt(1, 10000));
            player.setGuildName(generateGuildName(rand));
        }

        return player;
    }

    public static FakePlayer generateRandom(Job job) {
        FakePlayer player = generateRandom();
        player.setJob(job);
        player.setLevel(generateLevelForJob(job, ThreadLocalRandom.current()));
        return player;
    }

    public static FakePlayer generateRandom(int minLevel, int maxLevel) {
        FakePlayer player = generateRandom();
        player.setLevel(ThreadLocalRandom.current().nextInt(minLevel, maxLevel + 1));
        return player;
    }

    /**
     * 生成随机名字（从名字库加载，30%概率加数字后缀）
     */
    private static String generateRandomName(ThreadLocalRandom rand) {
        if (PLAYER_NAMES.isEmpty()) {
            return "冒险家" + rand.nextInt(1000, 99999);
        }
        String name = PLAYER_NAMES.get(rand.nextInt(PLAYER_NAMES.size()));
        if (rand.nextDouble() < 0.3) {
            return name + rand.nextInt(10, 9999);
        }
        return name;
    }

    /**
     * 生成随机公会名（从公会库加载）
     */
    private static String generateGuildName(ThreadLocalRandom rand) {
        if (GUILD_NAMES.isEmpty()) {
            return "公会" + rand.nextInt(100, 9999);
        }
        return GUILD_NAMES.get(rand.nextInt(GUILD_NAMES.size()));
    }

    private static int generateLevelForJob(Job job, ThreadLocalRandom rand) {
        int jobIndex = getJobTier(job);
        if (jobIndex < 0 || jobIndex >= JOB_LEVEL_RANGE.length) {
            return rand.nextInt(10, 200);
        }
        int[] range = JOB_LEVEL_RANGE[jobIndex];
        return rand.nextInt(range[0], range[1] + 1);
    }

    private static int getJobTier(Job job) {
        int jobId = job.getId();
        if (jobId < 100) return 0;
        if (jobId < 600) return Math.min(jobId % 100 / 10, 3);
        if (jobId == 112 || jobId == 122 || jobId == 132 ||
                jobId == 212 || jobId == 222 || jobId == 232 ||
                jobId == 312 || jobId == 322 ||
                jobId == 412 || jobId == 422 ||
                jobId == 512 || jobId == 522) {
            return 3;
        }
        if (jobId >= 1100 && jobId < 1200) {
            int sub = jobId % 100;
            return sub < 10 ? 1 : (sub < 100 ? 2 : 3);
        }
        return ThreadLocalRandom.current().nextInt(4);
    }

    private static FakePlayerLook generateRandomLook(ThreadLocalRandom rand) {
        byte gender = (byte) rand.nextInt(2);
        int skin = SkinColor.values()[rand.nextInt(SkinColor.values().length)].getId();
        int face = FACES[rand.nextInt(FACES.length)];
        int hair = HAIRS[rand.nextInt(HAIRS.length)];

        int equipCount = rand.nextInt(1, 5);
        int[] equips = new int[equipCount * 2];
        for (int i = 0; i < equipCount; i++) {
            int poolIndex = rand.nextInt(EQUIP_POOLS.length);
            int[] pool = EQUIP_POOLS[poolIndex];
            equips[i * 2] = poolIndex + 1;
            equips[i * 2 + 1] = pool[rand.nextInt(pool.length)];
        }

        int weaponPool = rand.nextInt(WEAPON_POOLS.length);
        int weapon = WEAPON_POOLS[weaponPool][rand.nextInt(WEAPON_POOLS[weaponPool].length)];

        return new FakePlayerLook(gender, skin, face, hair, equips, weapon);
    }
}
