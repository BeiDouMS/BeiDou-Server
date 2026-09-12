package org.gms.client.autoban;

import lombok.extern.slf4j.Slf4j;
import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.config.GameConfig;
import org.gms.dao.entity.AutobanLogDO;
import org.gms.manager.ServerManager;
import org.gms.service.AutobanLogService;
import org.gms.util.I18nUtil;
import org.springframework.context.ApplicationContext;

import java.sql.Timestamp;

/**
 * 反作弊事件落库埋点：游戏线程只组装一行记录并投递给 {@link AutobanLogService} 异步写库，
 * 任何异常都在此吞掉并记 warn，不影响原有检测/处置流程。
 */
@Slf4j
public final class AutobanLogger {
    public static final String ACTION_ALERT = "ALERT";
    public static final String ACTION_POINT = "POINT";
    public static final String ACTION_AUTOBAN = "AUTOBAN";
    public static final String ACTION_DISCONNECT = "DISCONNECT";
    /**
     * AutobanManager.setTimestamp 命中时的类型名（不属于 AutobanFactory 枚举）
     */
    public static final String TYPE_TIMESTAMP_SPAM = "TIMESTAMP_SPAM";

    private static final int REASON_MAX_LENGTH = 512;
    /**
     * Client.getRemoteAddress() 取不到地址时返回的字面量
     */
    private static final String UNKNOWN_ADDRESS = "null";

    private AutobanLogger() {
    }

    public static void record(Character chr, String type, String action, Integer points, Integer threshold, String reason) {
        try {
            ApplicationContext context = ServerManager.getApplicationContext();
            if (context == null) {
                return;
            }
            AutobanLogService service = context.getBean(AutobanLogService.class);

            AutobanLogDO.AutobanLogDOBuilder builder = AutobanLogDO.builder()
                    .world(0)
                    .channel(0)
                    .type(type)
                    .action(action)
                    .points(points)
                    .threshold(threshold)
                    .reason(truncate(reason))
                    .autoBanEnabled(GameConfig.getServerBoolean("use_auto_ban"))
                    // 在游戏线程取事件时刻，而不是依赖 DB 默认值记写库时刻（异步队列积压时两者会漂）
                    .createTime(new Timestamp(System.currentTimeMillis()));

            if (chr != null) {
                builder.world(chr.getWorld())
                        .characterId(chr.getId())
                        .characterName(chr.getName())
                        .mapId(chr.getMapId());
                Client client = chr.getClient();
                if (client != null) {
                    builder.channel(client.getChannel())
                            .accountId(client.getAccID())
                            .accountName(client.getAccountName())
                            .ip(normalizeIp(client.getRemoteAddress()));
                }
            }
            service.record(builder.build());
        } catch (Exception e) {
            log.warn(I18nUtil.getLogMessage("AutobanLogger.record.warn1", type, action), e);
        }
    }

    private static String normalizeIp(String ip) {
        return ip == null || UNKNOWN_ADDRESS.equals(ip) ? null : ip;
    }

    private static String truncate(String reason) {
        if (reason == null || reason.length() <= REASON_MAX_LENGTH) {
            return reason;
        }
        return reason.substring(0, REASON_MAX_LENGTH);
    }
}
