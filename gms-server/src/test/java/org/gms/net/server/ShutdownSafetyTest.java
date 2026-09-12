package org.gms.net.server;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.net.server.channel.Channel;
import org.gms.net.server.services.BaseScheduler;
import org.gms.net.server.services.task.world.CharacterSaveService;
import org.gms.net.server.world.World;
import org.gms.testsupport.MockSpringContext;
import org.gms.util.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 关服路径的数据安全回归测试（不需要数据库、不启动 Netty）。
 * <p>
 * 复现 2026-09-12 审计发现的四类丢存档路径：
 * <ol>
 *   <li>频道 players 已置空后，别的频道断开商城玩家时 World/Channel.removePlayer NPE；</li>
 *   <li>disconnectAwayPlayers 边遍历 playersAway 边被 removePlayerAway 删元素 → ConcurrentModificationException；</li>
 *   <li>Channel.shutdown 任一步骤异常 → finishedShutdown 永远为 false → Server 死等；</li>
 *   <li>PlayerStorage.disconnectAll 一个角色断线抛异常 → 后面的角色全部不存档；</li>
 *   <li>转场存档只注册到 CharacterSaveService，dispose 时被静默清掉。</li>
 * </ol>
 */
class ShutdownSafetyTest {

    @BeforeAll
    static void installContext() {
        MockSpringContext.install();
    }

    private static Character mockCharacter(int id, String name, Client client) {
        Character chr = mock(Character.class);
        when(chr.getId()).thenReturn(id);
        when(chr.getName()).thenReturn(name);
        when(chr.getClient()).thenReturn(client);
        when(chr.isLoggedIn()).thenReturn(true);
        return chr;
    }

    private static void setField(Object target, Class<?> declaring, String name, Object value) throws Exception {
        Field f = declaring.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void channelRemovePlayerIsNullSafeAfterShutdown() {
        // 局部 mock 不跑构造函数，players 字段就是 null —— 正是关服中途「已置空」的状态
        Channel ch = mock(Channel.class, CALLS_REAL_METHODS);
        Character chr = mockCharacter(1, "a", mock(Client.class));

        assertFalse(assertDoesNotThrow(() -> ch.removePlayer(chr)));
    }

    @Test
    void worldRemovePlayerIsNullSafeWhenClientOrStorageMissing() {
        World world = mock(World.class, CALLS_REAL_METHODS);   // players 字段为 null
        Character chr = mockCharacter(1, "a", null);          // 连 client 都没有

        assertDoesNotThrow(() -> world.removePlayer(chr));
    }

    @Test
    void disconnectAwayPlayersSurvivesConcurrentRemoval() throws Exception {
        Channel ch = mock(Channel.class, CALLS_REAL_METHODS);
        Set<Integer> away = new HashSet<>(List.of(1, 2, 3));
        setField(ch, Channel.class, "playersAway", away);

        PlayerStorage worldStorage = mock(PlayerStorage.class);
        World world = mock(World.class);
        when(world.getPlayerStorage()).thenReturn(worldStorage);
        doReturn(world).when(ch).getWorldServer();

        AtomicInteger disconnected = new AtomicInteger();
        for (int id : List.of(1, 2, 3)) {
            Client client = mock(Client.class);
            // 真实链路：forceDisconnect → Character.setDisconnectedFromChannelWorld → Channel.removePlayerAway
            doAnswer(inv -> {
                away.remove(id);
                disconnected.incrementAndGet();
                return null;
            }).when(client).forceDisconnect();
            Character chr = mockCharacter(id, "chr" + id, client);   // 先建好 mock，再 stub，避免嵌套 when()
            when(worldStorage.getCharacterById(id)).thenReturn(chr);
        }

        Method m = Channel.class.getDeclaredMethod("disconnectAwayPlayers");
        m.setAccessible(true);
        assertDoesNotThrow(() -> m.invoke(ch));   // 旧代码在第 2 个人处抛 ConcurrentModificationException
        assertEquals(3, disconnected.get(), "三个商城/MTS 玩家都必须被断线存档");
        assertTrue(away.isEmpty());
    }

    @Test
    void channelShutdownAlwaysMarksFinishedEvenWhenStepsFail() throws Exception {
        // 局部 mock：hiredMerchants / lock / services / eventSM / mapManager / channelServer 全是 null，
        // 每一步都会炸，模拟「关服过程中出现异常」。finishedShutdown 必须仍然置位，否则 Server 死等。
        Channel ch = mock(Channel.class, CALLS_REAL_METHODS);
        setField(ch, Channel.class, "playersAway", new HashSet<>());
        setField(ch, Channel.class, "players", new PlayerStorage());
        doReturn(mock(World.class)).when(ch).getWorldServer();

        assertDoesNotThrow(ch::shutdown);
        assertTrue(ch.finishedShutdown(), "任何步骤异常都不能让 finishedShutdown 停在 false");
    }

    @Test
    void playerStorageDisconnectAllIsolatesFailures() {
        PlayerStorage storage = new PlayerStorage();

        Client bad = mock(Client.class);
        doThrow(new IllegalStateException("boom")).when(bad).forceDisconnect();
        Client good = mock(Client.class);

        storage.addPlayer(mockCharacter(1, "bad", bad));
        storage.addPlayer(mockCharacter(2, "good", good));

        assertDoesNotThrow(storage::disconnectAll);
        verify(bad).forceDisconnect();
        verify(good).forceDisconnect();           // 旧代码：第一个抛异常，第二个永远不会被断线存档
        assertEquals(0, storage.getSize());
    }

    @Test
    void characterSaveServiceFlushesPendingSavesOnDispose() throws Exception {
        CharacterSaveService service = new CharacterSaveService();
        Object scheduler = getField(service, CharacterSaveService.class, "chrSaveScheduler");

        AtomicInteger ran = new AtomicInteger();
        Map<Object, Pair<Runnable, Long>> entries = new HashMap<>();
        entries.put(1, new Pair<>(ran::incrementAndGet, Long.MAX_VALUE));   // 还没到期 = 定时器还没跑到
        entries.put(2, new Pair<>(() -> {
            ran.incrementAndGet();
            throw new IllegalStateException("save failed");
        }, Long.MAX_VALUE));
        entries.put(3, new Pair<>(ran::incrementAndGet, Long.MAX_VALUE));
        setField(scheduler, BaseScheduler.class, "registeredEntries", entries);

        assertDoesNotThrow(service::dispose);
        assertEquals(3, ran.get(), "dispose 前必须把三条待处理存档都执行（含失败的那条不影响其余）");
    }

    private static Object getField(Object target, Class<?> declaring, String name) throws Exception {
        Field f = declaring.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }
}
