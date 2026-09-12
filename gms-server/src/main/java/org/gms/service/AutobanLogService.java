package org.gms.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.autoban.AutobanFactory;
import org.gms.client.autoban.AutobanLogger;
import org.gms.config.GameConfig;
import org.gms.dao.entity.AutobanLogDO;
import org.gms.dao.mapper.AutobanLogMapper;
import org.gms.model.dto.AutobanLogSearchReqDTO;
import org.gms.model.dto.AutobanLogSummaryReqDTO;
import org.gms.model.dto.AutobanLogSummaryRtnDTO;
import org.gms.util.I18nUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.gms.dao.entity.table.AutobanLogDOTableDef.AUTOBAN_LOG_D_O;

/**
 * 反作弊触发事件流水服务。
 *
 * @author Nap
 * @since 2026-09-12
 */
@Service
@AllArgsConstructor
@Slf4j
public class AutobanLogService {
    /**
     * game_config：流水保留天数，0 或缺省表示不清理
     */
    public static final String CONFIG_KEEP_DAYS = "autoban_log_keep_days";

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;
    private static final int DEFAULT_SUMMARY_LIMIT = 20;
    private static final int MAX_SUMMARY_LIMIT = 200;
    /**
     * 写库队列上限：MySQL 不可用时队列不会无限增长，满了丢弃最旧一条
     */
    private static final int QUEUE_CAPACITY = 10_000;
    /**
     * 写库失败/丢弃告警的最小间隔，避免 MySQL 抖动期间每条都打堆栈
     */
    private static final long WARN_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(1);
    private static final int CLEAN_BATCH_SIZE = 5_000;
    private static final int CLEAN_MAX_BATCHES = 200;

    private static final AtomicLong LAST_WARN_AT = new AtomicLong();
    private static final AtomicLong DROPPED_COUNT = new AtomicLong();
    private static final AtomicLong FAILED_COUNT = new AtomicLong();

    /**
     * 单线程守护写库线程 + 有界队列：游戏线程只投递，不等待，写失败不影响游戏逻辑。
     */
    private static final ThreadPoolExecutor WRITER = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(QUEUE_CAPACITY),
            r -> {
                Thread t = new Thread(r, "autoban-log-writer");
                t.setDaemon(true);
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            },
            (r, executor) -> {
                // 队列满：丢弃最旧一条再投递本条（同 DiscardOldestPolicy），并限频告警
                if (!executor.isShutdown()) {
                    executor.getQueue().poll();
                    executor.execute(r);
                }
                long dropped = DROPPED_COUNT.incrementAndGet();
                if (shouldWarn()) {
                    log.warn(I18nUtil.getLogMessage("AutobanLogService.record.warn3", String.valueOf(QUEUE_CAPACITY), String.valueOf(dropped)));
                }
            });

    private final AutobanLogMapper autobanLogMapper;

    /**
     * 异步落库；异常只记日志，绝不抛回调用线程。
     */
    public void record(AutobanLogDO row) {
        if (row == null) {
            return;
        }
        try {
            WRITER.execute(() -> {
                try {
                    autobanLogMapper.insertSelective(row);
                } catch (Exception e) {
                    long failed = FAILED_COUNT.incrementAndGet();
                    if (shouldWarn()) {
                        log.warn(I18nUtil.getLogMessage("AutobanLogService.record.warn1", row.getCharacterName(), row.getType(), row.getAction(), String.valueOf(failed)), e);
                    }
                }
            });
        } catch (Exception e) {
            log.warn(I18nUtil.getLogMessage("AutobanLogService.record.warn2", row.getCharacterName(), row.getType(), row.getAction()), e);
        }
    }

    public Page<AutobanLogDO> getLogList(AutobanLogSearchReqDTO req) {
        if (req == null) {
            req = new AutobanLogSearchReqDTO();
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(AUTOBAN_LOG_D_O.CHARACTER_NAME.like(req.getCharacterName(), StringUtils.hasText(req.getCharacterName())))
                .and(AUTOBAN_LOG_D_O.ACCOUNT_ID.eq(req.getAccountId(), req.getAccountId() != null))
                .and(AUTOBAN_LOG_D_O.CHARACTER_ID.eq(req.getCharacterId(), req.getCharacterId() != null))
                .and(AUTOBAN_LOG_D_O.TYPE.eq(req.getType(), StringUtils.hasText(req.getType())))
                .and(AUTOBAN_LOG_D_O.ACTION.eq(req.getAction(), StringUtils.hasText(req.getAction())))
                .and(AUTOBAN_LOG_D_O.POINTS.ge(req.getMinPoints(), req.getMinPoints() != null))
                .and(AUTOBAN_LOG_D_O.CREATE_TIME.ge(req.getStartTime(), req.getStartTime() != null))
                .and(AUTOBAN_LOG_D_O.CREATE_TIME.le(inclusiveEnd(req.getEndTime()), req.getEndTime() != null))
                .orderBy(AUTOBAN_LOG_D_O.CREATE_TIME.desc(), AUTOBAN_LOG_D_O.ID.desc());
        int pageNo = req.getPageNo() == null || req.getPageNo() < 1 ? 1 : req.getPageNo();
        int pageSize = req.getPageSize() == null || req.getPageSize() < 1 ? DEFAULT_PAGE_SIZE : Math.min(req.getPageSize(), MAX_PAGE_SIZE);
        return autobanLogMapper.paginate(pageNo, pageSize, queryWrapper);
    }

    public List<AutobanLogSummaryRtnDTO> getSummary(AutobanLogSummaryReqDTO req) {
        if (req == null) {
            req = new AutobanLogSummaryReqDTO();
        }
        Integer limit = req.getLimit();
        if (limit == null || limit < 1) {
            limit = DEFAULT_SUMMARY_LIMIT;
        } else if (limit > MAX_SUMMARY_LIMIT) {
            limit = MAX_SUMMARY_LIMIT;
        }
        req.setLimit(limit);
        req.setEndTime(inclusiveEnd(req.getEndTime()));
        if (!StringUtils.hasText(req.getAction())) {
            req.setAction(null);
        }
        return autobanLogMapper.selectSummary(req);
    }

    /**
     * 类型下拉选项：全部 AutobanFactory 枚举 + TIMESTAMP_SPAM。
     */
    public List<Map<String, String>> getTypeOptions() {
        List<Map<String, String>> result = new ArrayList<>();
        for (AutobanFactory factory : AutobanFactory.values()) {
            result.add(option(factory.name(), factory.getName()));
        }
        result.add(option(AutobanLogger.TYPE_TIMESTAMP_SPAM, I18nUtil.getMessage("autoban.name." + AutobanLogger.TYPE_TIMESTAMP_SPAM)));
        return result;
    }

    /**
     * 按 game_config.autoban_log_keep_days 清理过期流水；0 或缺省表示不清理。分批删除避免长事务。
     *
     * @return 本次删除行数
     */
    public int cleanExpired() {
        int keepDays = GameConfig.getServerInt(CONFIG_KEEP_DAYS);
        if (keepDays <= 0) {
            return 0;
        }
        Timestamp before = new Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(keepDays));
        int total = 0;
        for (int i = 0; i < CLEAN_MAX_BATCHES; i++) {
            int deleted = autobanLogMapper.deleteBefore(before, CLEAN_BATCH_SIZE);
            total += deleted;
            if (deleted < CLEAN_BATCH_SIZE) {
                break;
            }
        }
        if (total > 0) {
            log.info(I18nUtil.getLogMessage("AutobanLogService.clean.info1", String.valueOf(keepDays), String.valueOf(total)));
        }
        return total;
    }

    /**
     * 前端时间精度为秒而列为 DATETIME(3)：把结束时间补到该秒的 .999，避免漏掉同一秒内的记录。
     */
    private static Date inclusiveEnd(Date end) {
        if (end == null) {
            return null;
        }
        return new Date(end.getTime() / 1000 * 1000 + 999);
    }

    private static boolean shouldWarn() {
        long now = System.currentTimeMillis();
        long last = LAST_WARN_AT.get();
        return now - last >= WARN_INTERVAL_MILLIS && LAST_WARN_AT.compareAndSet(last, now);
    }

    private static Map<String, String> option(String value, String label) {
        Map<String, String> map = new HashMap<>(2);
        map.put("value", value);
        map.put("label", label);
        return map;
    }
}
