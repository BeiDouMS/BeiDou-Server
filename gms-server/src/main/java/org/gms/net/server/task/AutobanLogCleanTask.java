package org.gms.net.server.task;

import lombok.extern.slf4j.Slf4j;
import org.gms.manager.ServerManager;
import org.gms.service.AutobanLogService;
import org.gms.util.I18nUtil;

/**
 * 每日清理过期的反作弊事件流水（保留天数见 game_config.autoban_log_keep_days）。
 */
@Slf4j
public class AutobanLogCleanTask implements Runnable {
    @Override
    public void run() {
        try {
            ServerManager.getApplicationContext().getBean(AutobanLogService.class).cleanExpired();
        } catch (Exception e) {
            log.error(I18nUtil.getLogMessage("AutobanLogCleanTask.error1"), e);
        }
    }
}
