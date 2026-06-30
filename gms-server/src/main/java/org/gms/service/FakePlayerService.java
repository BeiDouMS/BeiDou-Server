package org.gms.service;

import org.gms.client.Client;
import org.gms.client.fake.FakePlayer;
import org.gms.client.fake.FakePlayerConfig;
import org.gms.client.fake.FakePlayerManager;
import org.gms.server.maps.MapleMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 假人服务
 * <p>
 * 集成到游戏系统，懒加载模式：玩家进入地图时才生成假人
 * </p>
 *
 * @author BeiDou
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "fake-player", name = "enabled", havingValue = "true", matchIfMissing = false)
public class FakePlayerService {

    private final FakePlayerConfig config;
    private final FakePlayerManager manager = FakePlayerManager.getInstance();

    /**
     * 已初始化假人的地图集合（防止重复生成）
     */
    private final Set<Integer> initializedMaps = ConcurrentHashMap.newKeySet();

    /**
     * 服务启动时只做配置检查，不生成假人
     */
    @PostConstruct
    public void init() {
        if (!config.isEnabled()) {
            log.info("假人系统已禁用");
            return;
        }
        log.info("假人系统就绪（懒加载模式），配置地图: {}", config.getMaps().keySet());
    }

    /**
     * 确保指定地图的假人已生成（懒加载核心方法）
     *
     * @param mapId 地图 ID
     */
    private void ensureMapInitialized(int mapId) {
        if (!config.isEnabled() || !config.shouldSpawnInMap(mapId)) {
            return;
        }
        // 已初始化则跳过
        if (!initializedMaps.add(mapId)) {
            return;
        }

        int count = config.getFakeCount(mapId);
        manager.spawnFakes(mapId, count, null);
        log.info("懒加载：在地图 {} 生成 {} 个假人", mapId, count);
    }

    /**
     * 玩家进入地图时调用（由 Character.changeMapInternal 触发）
     *
     * @param client 玩家客户端
     * @param map    玩家进入的地图
     */
    public void onPlayerEnterMap(Client client, MapleMap map) {
        if (!config.isEnabled()) {
            return;
        }

        int mapId = map.getId();

        // 懒加载：首次进入该地图时才生成假人
        ensureMapInitialized(mapId);

        // 展示假人给玩家
        if (config.getInteraction().isShowOnEnter()) {
            manager.showAllFakesInMap(client, mapId);
            log.debug("向玩家 {} 展示地图 {} 的假人", client.getPlayer().getName(), mapId);
        }
    }

    /**
     * 检查 ID 是否是假人
     */
    public boolean isFakePlayer(int id) {
        return manager.isFakePlayer(id);
    }

    /**
     * 处理点击假人
     */
    public void handleFakeInteraction(Client client, int fakeId) {
        if (!isFakePlayer(fakeId)) {
            return;
        }
        var packet = manager.handleFakeClick(fakeId);
        if (packet != null) {
            client.sendPacket(packet);
        }
    }

    /**
     * 刷新指定地图的假人
     */
    public void refreshMapFakes(int mapId) {
        manager.refreshFakes(mapId);
        log.info("刷新地图 {} 的假人", mapId);
    }

    public FakePlayerManager getManager() {
        return manager;
    }
}
