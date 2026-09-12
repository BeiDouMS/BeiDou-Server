package org.gms.client.autoban;

import com.mybatisflex.core.query.QueryWrapper;
import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.dao.entity.AutobanLogDO;
import org.gms.dao.mapper.AutobanLogMapper;
import org.gms.model.dto.AutobanLogSearchReqDTO;
import org.gms.service.AutobanLogService;
import org.gms.testsupport.MockSpringContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 反作弊事件落库的纯逻辑测试（不需要数据库）。
 * <p>
 * 假上下文里 GameConfig 是空表，等价于 {@code use_auto_ban=false / use_auto_ban_log=false} 的留痕模式。
 */
class AutobanLogTest {

    private AutobanLogService service;

    @BeforeAll
    static void installContext() {
        MockSpringContext.install();
        AutobanFactory.initConfig(new HashMap<>());   // 无 DB 覆盖，全部走枚举默认阈值
    }

    @BeforeEach
    void freshServiceMock() {
        service = mock(AutobanLogService.class);
        MockSpringContext.registerBean(AutobanLogService.class, service);
    }

    private static Character mockCharacter() {
        Client client = mock(Client.class);
        when(client.getChannel()).thenReturn(2);
        when(client.getAccID()).thenReturn(77);
        when(client.getAccountName()).thenReturn("acc");
        when(client.getRemoteAddress()).thenReturn("null");   // Client 取不到地址时的字面量

        Character chr = mock(Character.class);
        when(chr.getId()).thenReturn(1001);
        when(chr.getName()).thenReturn("tester");
        when(chr.getWorld()).thenReturn(0);
        when(chr.getMapId()).thenReturn(100000000);
        when(chr.getClient()).thenReturn(client);
        return chr;
    }

    @Test
    void recordFillsCharacterFieldsTruncatesReasonAndNormalizesIp() {
        Character chr = mockCharacter();
        String longReason = "x".repeat(600);

        AutobanLogger.record(chr, "ITEM_VAC", AutobanLogger.ACTION_ALERT, 3, 10, longReason);

        ArgumentCaptor<AutobanLogDO> captor = ArgumentCaptor.forClass(AutobanLogDO.class);
        verify(service).record(captor.capture());
        AutobanLogDO row = captor.getValue();
        assertEquals(1001, row.getCharacterId());
        assertEquals("tester", row.getCharacterName());
        assertEquals(77, row.getAccountId());
        assertEquals("acc", row.getAccountName());
        assertEquals(2, row.getChannel());
        assertEquals(100000000, row.getMapId());
        assertNull(row.getIp(), "\"null\" 字面量地址应落 NULL");
        assertEquals("ITEM_VAC", row.getType());
        assertEquals(AutobanLogger.ACTION_ALERT, row.getAction());
        assertEquals(3, row.getPoints());
        assertEquals(10, row.getThreshold());
        assertEquals(512, row.getReason().length(), "原因超过 512 应截断");
        assertEquals(Boolean.FALSE, row.getAutoBanEnabled(), "空配置 = use_auto_ban 关 = 仅留痕");
        assertNotNull(row.getCreateTime(), "事件时刻应在游戏线程取，不依赖 DB 默认值");
    }

    @Test
    void recordWithoutCharacterDoesNotThrow() {
        AutobanLogger.record(null, "GENERAL", AutobanLogger.ACTION_ALERT, null, null, "no chr");
        verify(service).record(any(AutobanLogDO.class));
    }

    @Test
    void addPointKeepsCountingInLogOnlyModeButNeverBans() {
        Character chr = mockCharacter();
        AutobanManager manager = new AutobanManager(chr);
        // FAST_HP_HEALING：默认 15 分、永不衰减（expire=-1），不会触碰 Server.getInstance()
        int threshold = AutobanFactory.FAST_HP_HEALING.getEffectivePoints();
        assertEquals(15, threshold);

        for (int i = 0; i < threshold; i++) {
            AutobanFactory.FAST_HP_HEALING.addPoint(manager, "heal too fast");
        }

        ArgumentCaptor<AutobanLogDO> captor = ArgumentCaptor.forClass(AutobanLogDO.class);
        verify(service, times(threshold)).record(captor.capture());
        List<AutobanLogDO> rows = captor.getAllValues();
        assertEquals(AutobanLogger.ACTION_POINT, rows.get(0).getAction());
        assertEquals(1, rows.get(0).getPoints());
        assertEquals(threshold, rows.get(0).getThreshold());
        assertEquals(AutobanLogger.ACTION_AUTOBAN, rows.get(threshold - 1).getAction(), "达阈值那条应标为 AUTOBAN 判定");
        assertEquals(threshold, rows.get(threshold - 1).getPoints());
        verify(chr, never()).autoBan(any());   // 留痕模式：判定归判定，绝不真封
    }

    @Test
    void addPointExemptsGmButStillLeavesTrace() {
        Character gm = mockCharacter();
        when(gm.isGM()).thenReturn(true);
        AutobanManager manager = new AutobanManager(gm);

        AutobanFactory.FAST_HP_HEALING.addPoint(manager, "gm testing");

        ArgumentCaptor<AutobanLogDO> captor = ArgumentCaptor.forClass(AutobanLogDO.class);
        verify(service).record(captor.capture());
        assertNull(captor.getValue().getPoints(), "GM 不计分，points 留空");
        assertEquals(AutobanLogger.ACTION_POINT, captor.getValue().getAction());
        verify(gm, never()).autoBan(any());
    }

    @Test
    void getLogListClampsPageSize() {
        AutobanLogMapper mapper = mock(AutobanLogMapper.class);
        AutobanLogService real = new AutobanLogService(mapper);
        AutobanLogSearchReqDTO req = new AutobanLogSearchReqDTO();
        req.setPageNo(0);
        req.setPageSize(100000);

        real.getLogList(req);

        verify(mapper).paginate(eq(1), eq(200), any(QueryWrapper.class));
        verify(mapper, never()).paginate(anyInt(), eq(100000), any(QueryWrapper.class));
    }
}
