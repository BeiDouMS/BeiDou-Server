package org.gms.client.fake;

import org.gms.client.Client;
import java.awt.Point;
import org.gms.net.packet.Packet;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 假人管理器
 * <p>
 * 管理假人生命周期，处理假人与真实玩家的交互
 * </p>
 *
 * @author BeiDou
 * @since 1.0.0
 */
@Slf4j
public final class FakePlayerManager {

    private static final FakePlayerManager INSTANCE = new FakePlayerManager();

    /**
     * 地图 ID -> 该地图的假人列表
     */
    private final Map<Integer, List<FakePlayer>> mapFakes = new ConcurrentHashMap<>();

    /**
     * 所有假人缓存
     */
    private final Map<Integer, FakePlayer> allFakes = new ConcurrentHashMap<>();

    private FakePlayerManager() {
        log.info("假人管理器初始化完成");
    }

    /**
     * 获取管理器单例
     *
     * @return 管理器实例
     */
    public static FakePlayerManager getInstance() {
        return INSTANCE;
    }

    /**
     * 在指定地图生成随机假人
     *
     * @param mapId     地图 ID
     * @param count     生成数量
     * @param positions 可选的位置列表（为 null 则随机）
     * @return 生成的假人列表
     */
    public List<FakePlayer> spawnFakes(int mapId, int count, List<Point> positions) {
        List<FakePlayer> spawned = new ArrayList<>();
        List<FakePlayer> mapList = mapFakes.computeIfAbsent(mapId, k -> new CopyOnWriteArrayList<>());

        for (int i = 0; i < count; i++) {
            FakePlayer fake = FakePlayerRandomizer.generateRandom();
            fake.setMapId(mapId);

            if (positions != null && !positions.isEmpty()) {
                fake.setPosition(positions.get(i % positions.size()));
            } else {
                int x = ThreadLocalRandom.current().nextInt(-400, 401);
                fake.setPosition(new Point(x, 0));
            }

            mapList.add(fake);
            allFakes.put(fake.getId(), fake);
            spawned.add(fake);

            log.info("生成假人: {} (ID:{}) 在地图 {}", fake.getName(), fake.getId(), mapId);
        }

        return spawned;
    }

    /**
     * 在指定地图生成单个随机假人
     *
     * @param mapId 地图 ID
     * @return 生成的假人对象
     */
    public FakePlayer spawnFake(int mapId) {
        return spawnFakes(mapId, 1, null).get(0);
    }

    /**
     * 生成指定配置的假人
     *
     * @param mapId    地图 ID
     * @param position 位置
     * @param fake     假人对象
     * @return 生成的假人对象
     */
    public FakePlayer spawnFake(int mapId, Point position, FakePlayer fake) {
        fake.setMapId(mapId);
        if (position != null) {
            fake.setPosition(position);
        }

        List<FakePlayer> mapList = mapFakes.computeIfAbsent(mapId, k -> new CopyOnWriteArrayList<>());
        mapList.add(fake);
        allFakes.put(fake.getId(), fake);

        log.info("生成指定假人: {} (ID:{}) 在地图 {}", fake.getName(), fake.getId(), mapId);
        return fake;
    }

    /**
     * 将假人展示给指定玩家
     *
     * @param client 玩家客户端
     * @param fake   假人对象
     */
    public void showFakeToPlayer(Client client, FakePlayer fake) {
        Packet spawnPacket = FakePlayerPacketCreator.spawnFakePlayer(fake);
        client.sendPacket(spawnPacket);
    }

    /**
     * 将地图上所有假人展示给指定玩家
     *
     * @param client 玩家客户端
     * @param mapId  地图 ID
     */
    public void showAllFakesInMap(Client client, int mapId) {
        List<FakePlayer> fakes = mapFakes.get(mapId);
        if (fakes != null) {
            for (FakePlayer fake : fakes) {
                showFakeToPlayer(client, fake);
            }
            log.debug("向玩家展示 {} 个假人在地图 {}", fakes.size(), mapId);
        }
    }

    /**
     * 从指定玩家视野中移除假人
     *
     * @param client 玩家客户端
     * @param fake   假人对象
     */
    public void hideFakeFromPlayer(Client client, FakePlayer fake) {
        Packet removePacket = FakePlayerPacketCreator.removeFakePlayer(fake.getId());
        client.sendPacket(removePacket);
    }

    /**
     * 移除指定地图上的所有假人
     *
     * @param mapId 地图 ID
     * @return 移除的假人数量
     */
    public int removeAllFakes(int mapId) {
        List<FakePlayer> fakes = mapFakes.remove(mapId);
        if (fakes != null) {
            int count = fakes.size();
            for (FakePlayer fake : fakes) {
                allFakes.remove(fake.getId());
                log.info("移除假人: {} (ID:{}) 从地图 {}", fake.getName(), fake.getId(), mapId);
            }
            return count;
        }
        return 0;
    }

    /**
     * 移除单个假人
     *
     * @param fakeId 假人 ID
     * @return 如果移除成功返回 true
     */
    public boolean removeFake(int fakeId) {
        FakePlayer fake = allFakes.remove(fakeId);
        if (fake != null) {
            List<FakePlayer> mapList = mapFakes.get(fake.getMapId());
            if (mapList != null) {
                mapList.remove(fake);
            }
            log.info("移除假人: {} (ID:{})", fake.getName(), fakeId);
            return true;
        }
        return false;
    }

    /**
     * 获取指定地图的所有假人
     *
     * @param mapId 地图 ID
     * @return 假人列表
     */
    public List<FakePlayer> getFakesInMap(int mapId) {
        return mapFakes.getOrDefault(mapId, Collections.emptyList());
    }

    /**
     * 获取假人对象
     *
     * @param fakeId 假人 ID
     * @return 假人对象，如果不存在返回 null
     */
    public FakePlayer getFake(int fakeId) {
        return allFakes.get(fakeId);
    }

    /**
     * 检查 ID 是否是假人
     *
     * @param id 角色 ID
     * @return 如果是假人返回 true
     */
    public boolean isFakePlayer(int id) {
        return id < 0 && allFakes.containsKey(id);
    }

    /**
     * 获取所有假人数量
     *
     * @return 假人总数
     */
    public int getTotalFakeCount() {
        return allFakes.size();
    }

    /**
     * 刷新指定地图的假人（重新随机生成外观）
     *
     * @param mapId 地图 ID
     */
    public void refreshFakes(int mapId) {
        List<FakePlayer> oldFakes = mapFakes.remove(mapId);
        if (oldFakes != null) {
            List<Point> positions = new ArrayList<>();
            for (FakePlayer old : oldFakes) {
                positions.add(old.getPosition());
                allFakes.remove(old.getId());
            }
            spawnFakes(mapId, positions.size(), positions);
            log.info("刷新地图 {} 的假人，共 {} 个", mapId, positions.size());
        }
    }

    /**
     * 处理玩家点击假人（返回假人信息）
     *
     * @param fakeId 假人 ID
     * @return 假人信息数据包，如果假人不存在返回 null
     */
    public Packet handleFakeClick(int fakeId) {
        FakePlayer fake = allFakes.get(fakeId);
        if (fake != null) {
            return FakePlayerPacketCreator.fakeCharInfo(fake);
        }
        return null;
    }
}
