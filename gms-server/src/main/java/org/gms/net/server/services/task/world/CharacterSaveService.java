package org.gms.net.server.services.task.world;

import org.gms.client.Character.SaveResult;
import org.gms.config.GameConfig;
import org.gms.util.I18nUtil;
import org.gms.exception.BizException;
import org.gms.exception.BizExceptionEnum;
import org.gms.net.server.services.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

/**
 * 全进程共用的存档入口。账号级串行同时保护角色、商城和共享仓库；不同账号有限并行。
 * monitor 只保护队列/会话状态，绝不在锁内取角色锁、执行 SQL 或完成 Future。
 * World 销毁不会清空尚未成功的存档，断线重试也不依赖已销毁的 World。
 */
public class CharacterSaveService extends BaseService {
    public static final int MAX_SAVE_ATTEMPTS = 3;
    private static final int REALTIME_BURST = 3;
    private static final Logger log = LoggerFactory.getLogger(CharacterSaveService.class);
    private static final ScheduledExecutorService RETRIES = Executors.newSingleThreadScheduledExecutor(
            Thread.ofPlatform().daemon().name("character-save-retry").factory());
    private static final CharacterSaveService INSTANCE = new CharacterSaveService(
            // 实际并发只由队列派发额度控制，执行器不再维护第二套固定线程上限。
            Executors.newCachedThreadPool(Thread.ofPlatform().daemon().name("character-save-", 0).factory()),
            (action, delay) -> RETRIES.schedule(action, delay, TimeUnit.MILLISECONDS),
            () -> GameConfig.get("server", "character_save_realtime_concurrency", 2),
            () -> GameConfig.get("server", "character_save_shared_concurrency", 2));

    private final Object monitor = new Object();
    private final Map<Integer, Lane> accounts = new HashMap<>();
    private final Executor executor;
    private final RetryScheduler retryScheduler;
    private final IntSupplier realtimeConcurrency;
    private final IntSupplier sharedConcurrency;
    // 两个集合只存账号；快照及其先后关系始终由唯一的 Lane.pending 管理。
    private final LinkedHashSet<Integer> realtimeReady = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> autosaveReady = new LinkedHashSet<>();
    private int reservedRunning;
    private int sharedRunning;
    private int sharedRealtimeBurst;
    private boolean dispatching;
    private long completedDispatches;
    private long savedDispatches;
    private long failedDispatches;
    private long discardedDispatches;
    private long mergedRequests;
    private long totalWaitNanos;
    private long totalExecuteNanos;
    private long lastMetricsAt = System.nanoTime();

    public CharacterSaveService() {
        this(INSTANCE.executor, INSTANCE.retryScheduler, INSTANCE.realtimeConcurrency, INSTANCE.sharedConcurrency);
    }

    public CharacterSaveService(Executor executor) {
        this(executor, (action, delay) -> {});
    }

    public CharacterSaveService(Executor executor, RetryScheduler retryScheduler) {
        this(executor, retryScheduler, INSTANCE.realtimeConcurrency, INSTANCE.sharedConcurrency);
    }

    public CharacterSaveService(Executor executor, RetryScheduler retryScheduler, IntSupplier realtimeConcurrency) {
        this(executor, retryScheduler, realtimeConcurrency, INSTANCE.sharedConcurrency);
    }

    public CharacterSaveService(Executor executor, RetryScheduler retryScheduler,
                                IntSupplier realtimeConcurrency, IntSupplier sharedConcurrency) {
        this.executor = Objects.requireNonNull(executor);
        this.retryScheduler = Objects.requireNonNull(retryScheduler);
        this.realtimeConcurrency = Objects.requireNonNull(realtimeConcurrency);
        this.sharedConcurrency = Objects.requireNonNull(sharedConcurrency);
    }

    public static CharacterSaveService getInstance() {
        return INSTANCE;
    }

    /** 热升额时立即派发已有等待账号；降额由执行者在当前存档结束后收敛。 */
    public void refreshConcurrency() {
        // 派发器也允许同步 Executor；回调重入只唤醒外层循环，避免递归耗尽栈。
        synchronized (monitor) {
            if (dispatching) return;
            dispatching = true;
        }
        while (true) {
            Dispatch dispatch;
            synchronized (monitor) {
                dispatch = claim();
                if (dispatch == null) {
                    dispatching = false;
                    return;
                }
            }
            try {
                executor.execute(() -> execute(dispatch));
            } catch (RuntimeException failure) {
                // 只扣本次实际领取请求的预算，不能连坐其他等待账号。
                dispatch.request.attempts++;
                dispatch.request.lastFailure = failure.getClass().getSimpleName();
                log.error(I18nUtil.getLogMessage("CharacterSave.queue1"),
                        dispatch.request.session.accountId, dispatch.request.version, failure);
                finish(dispatch, SaveResult.FAILED, 0L);
            }
        }
    }

    /** 必须在角色读档/Client 换绑之前调用。新连接不能抢占尚未最终提交的旧连接。 */
    public Session openSession(int accountId, int characterId, long loginTime, long sessionId) {
        synchronized (monitor) {
            Lane lane = accounts.computeIfAbsent(accountId, ignored -> new Lane());
            Session old = lane.owner;
            if (old != null && !old.closed && old.loginTime == loginTime && old.characterId == characterId) {
                return old;
            }
            if (old != null && (!old.closed || lane.running || !lane.pending.isEmpty()
                    || loginTime <= old.loginTime)) {
                throw new BizException(BizExceptionEnum.CHARACTER_SAVE_UNAVAILABLE);
            }
            Session session = new Session(accountId, characterId, loginTime, sessionId);
            lane.owner = session;
            return session;
        }
    }

    public boolean hasUnfinishedSession(int accountId) {
        synchronized (monitor) {
            Lane lane = accounts.get(accountId);
            return lane != null && lane.owner != null
                    && (!lane.owner.closed || lane.running || !lane.pending.isEmpty());
        }
    }

    public boolean hasOtherUnfinishedSession(int accountId, long loginTime) {
        synchronized (monitor) {
            Lane lane = accounts.get(accountId);
            return lane != null && lane.owner != null && lane.owner.loginTime != loginTime
                    && (!lane.owner.closed || lane.running || !lane.pending.isEmpty());
        }
    }

    /** 只返回已封闭且尚未结束的最终存档；普通在线账号不能被误报为存档等待。 */
    public PendingFinalSave pendingFinalSave(int accountId) {
        synchronized (monitor) {
            Lane lane = accounts.get(accountId);
            Session session = lane == null ? null : lane.owner;
            Request request = session == null ? null : session.finalRequest;
            if (session == null || !session.closing || session.closed || request == null) return null;
            String state = request.retryWaiting ? "RETRY_WAIT"
                    : lane.running ? (lane.pending.contains(request) ? "WAIT_PREDECESSOR" : "EXECUTING")
                    : lane.readyQueue.name();
            return new PendingFinalSave(accountId, session.characterId, session.sessionId, request.version,
                    request.attempts, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - session.closingAt),
                    request.nextRetryAtMillis, state, request.lastFailure);
        }
    }

    public record PendingFinalSave(int accountId, int characterId, long sessionId, long version,
                                   int attempts, long waitingMillis, long nextRetryAtMillis,
                                   String state, String lastFailure) {}

    public boolean isCurrentSession(Session session) {
        synchronized (monitor) {
            Lane lane = accounts.get(session.accountId);
            return lane != null && lane.owner == session && !session.closed;
        }
    }

    /** 仅撤销读档失败、从未提交过任何保存请求的预约，不能释放真实玩家的待存档。 */
    public boolean abandonLoad(Session session) {
        synchronized (monitor) {
            if (session == null) return false;
            Lane lane = accounts.get(session.accountId);
            if (lane != null && lane.owner == session && isUnusedLoad(lane)) {
                session.closed = true;
                return true;
            }
            return false;
        }
    }

    /** 控制台只处理已超过登录宽限期的空预约；调用方同时持有该账号的 AccountSession 锁。 */
    public Session getUnusedLoad(int accountId, long createdBefore) {
        synchronized (monitor) {
            Lane lane = accounts.get(accountId);
            return lane != null && isUnusedLoad(lane) && !lane.owner.closed
                    && lane.owner.createdAt <= createdBefore ? lane.owner : null;
        }
    }

    private boolean isUnusedLoad(Lane lane) {
        Session owner = lane.owner;
        return owner != null && owner.nextVersion == 0 && owner.committedVersion == 0
                && !owner.closing && owner.finalRequest == null && !lane.running
                && lane.executingSession == null && lane.pending.isEmpty();
    }

    public boolean isClosing(Session session) {
        synchronized (monitor) {
            return session.closing || session.closed;
        }
    }

    /** 角色主体只使用入队前的私有快照及固定会话身份；宠物/家族沿用现有独立保存入口。 */
    public CompletableFuture<SaveResult> submit(Session session, boolean realtime, boolean terminal,
                                               SaveOperation operation, Runnable release) {
        return submit(session, realtime, terminal, 0, operation, release);
    }

    /** 快照生成失败也占用同一预算，入队后不能重新获得三次机会。 */
    public CompletableFuture<SaveResult> submit(Session session, boolean realtime, boolean terminal,
                                               int previousFailures, SaveOperation operation, Runnable release) {
        Request request;
        List<Request> discarded = new ArrayList<>();
        synchronized (monitor) {
            Lane lane = accounts.get(session.accountId);
            if (lane == null || lane.owner != session || session.closing || session.closed) {
                throw new BizException(BizExceptionEnum.CHARACTER_SAVE_UNAVAILABLE);
            }
            request = new Request(session, ++session.nextVersion, realtime, terminal, operation, release);
            request.attempts = Math.min(MAX_SAVE_ATTEMPTS, Math.max(0, previousFailures));
            // 只替换同一 Session 的等待项，绝不删除正在执行的任务。
            for (var iterator = lane.pending.iterator(); iterator.hasNext();) {
                Request old = iterator.next();
                if (old.session == session) {
                    iterator.remove();
                    request.realtime |= old.realtime;
                    request.firstEnqueuedAt = Math.min(request.firstEnqueuedAt, old.firstEnqueuedAt);
                    request.readyWaitNanos += old.readyWaitNanos;
                    request.executionNanos += old.executionNanos;
                    request.reportedWaitNanos += old.reportedWaitNanos;
                    mergedRequests++;
                    request.waiters.addAll(old.waiters);
                    old.waiters.clear();
                    discarded.add(old);
                }
            }
            if (terminal) {
                session.closing = true;
                session.closingAt = System.nanoTime();
                session.finalRequest = request;
            }
            lane.pending.addLast(request);
            refreshReadyPlacement(session.accountId, lane);
        }
        // 合并后的旧请求等待新版本真正提交；资源释放不持队列锁。
        discarded.forEach(this::release);
        refreshConcurrency();
        return request.completion;
    }

    /** 重试沿用原请求及累计次数；成功或耗尽丢弃后，不能重新创建旧 session 的存档。 */
    public CompletableFuture<SaveResult> retryFinal(Session session) {
        CompletableFuture<SaveResult> result;
        synchronized (monitor) {
            if (session.closed) return CompletableFuture.completedFuture(session.finalResult);
            Request request = session.finalRequest;
            if (request == null) return CompletableFuture.completedFuture(SaveResult.FAILED);
            Lane lane = accounts.get(session.accountId);
            if (request.completion.isDone()
                    || (lane.pending.contains(request) && request.waiters.isEmpty())) {
                request.completion = new CompletableFuture<>();
                request.waiters.add(request.completion);
            }
            if (lane.pending.contains(request)) {
                request.retryWaiting = false;
                request.nextRetryAtMillis = 0L;
                request.retryGeneration++;
                refreshReadyPlacement(session.accountId, lane);
            }
            result = request.completion;
        }
        refreshConcurrency();
        return result;
    }

    /** 调用方持 monitor；仅看链头，后续 session 的实时请求不能越过旧会话。 */
    private void refreshReadyPlacement(int accountId, Lane lane) {
        Request head = lane.pending.peekFirst();
        ReadyQueue target = lane.running || head == null || head.retryWaiting ? ReadyQueue.NONE
                : head.realtime ? ReadyQueue.REALTIME : ReadyQueue.AUTOSAVE;
        if (lane.readyQueue == target) return; // 合并同类快照不丢失原 FIFO 位置。
        realtimeReady.remove(accountId);
        autosaveReady.remove(accountId);
        if (lane.readyQueue != ReadyQueue.NONE && head != null) {
            head.readyWaitNanos += System.nanoTime() - lane.readyAt;
        }
        lane.readyQueue = target;
        lane.readyAt = System.nanoTime();
        if (target == ReadyQueue.REALTIME) realtimeReady.add(accountId);
        if (target == ReadyQueue.AUTOSAVE) autosaveReady.add(accountId);
    }

    /** 仅供已验证迁移凭证、复用同一在线角色的连接交接；普通读档仍走 openSession。 */
    public Session openMigrationSession(Session previous, int accountId, int characterId,
                                        long loginTime, long sessionId) {
        synchronized (monitor) {
            Lane lane = accounts.get(accountId);
            if (previous == null || lane == null || lane.owner != previous
                    || previous.accountId != accountId || previous.characterId != characterId
                    || !previous.closing || loginTime <= previous.loginTime) {
                throw new BizException(BizExceptionEnum.CHARACTER_SAVE_UNAVAILABLE);
            }
            Session session = new Session(accountId, characterId, loginTime, sessionId);
            lane.owner = session;
            return session;
        }
    }

    /** 已入队的旧 session 仍可顺序执行；此资格不允许旧连接追加保存。 */
    public boolean isExecutingSession(Session session) {
        synchronized (monitor) {
            Lane lane = accounts.get(session.accountId);
            return lane != null && lane.executingSession == session && !session.closed;
        }
    }

    /** 账号、链头和执行名额在同一临界区领取，领取后就不能再合并掉该快照。 */
    private Dispatch claim() {
        int realtimeLimit = Math.min(8, Math.max(0, realtimeConcurrency.getAsInt()));
        int sharedLimit = Math.min(8, Math.max(1, sharedConcurrency.getAsInt()));
        if (reservedRunning + sharedRunning >= realtimeLimit + sharedLimit) return null;
        boolean dedicated = !realtimeReady.isEmpty() && reservedRunning < realtimeLimit;
        LinkedHashSet<Integer> queue;
        if (dedicated) {
            queue = realtimeReady;
        } else {
            if (sharedRunning >= sharedLimit) return null;
            if (!autosaveReady.isEmpty() && (realtimeReady.isEmpty() || sharedRealtimeBurst >= REALTIME_BURST)) {
                queue = autosaveReady;
            } else if (!realtimeReady.isEmpty()) {
                queue = realtimeReady;
            } else {
                return null;
            }
        }
        int accountId = queue.removeFirst();
        Lane lane = accounts.get(accountId);
        Request request = lane.pending.removeFirst();
        request.readyWaitNanos += System.nanoTime() - lane.readyAt;
        lane.readyQueue = ReadyQueue.NONE;
        lane.running = true;
        lane.executingSession = request.session;
        if (dedicated) reservedRunning++;
        else {
            sharedRunning++;
            sharedRealtimeBurst = request.realtime ? Math.min(REALTIME_BURST, sharedRealtimeBurst + 1) : 0;
        }
        return new Dispatch(request, dedicated);
    }

    private void execute(Dispatch dispatch) {
        Request request = dispatch.request;
        long startedAt = System.nanoTime();
        SaveResult result = SaveResult.FAILED;
        int attempts = Math.min(request.realtime ? 3 : 1, MAX_SAVE_ATTEMPTS - request.attempts);
        for (int attempt = 1; attempt <= attempts; attempt++) {
            request.attempts++;
            try {
                result = request.operation.save();
                if (result == SaveResult.SAVED || result == SaveResult.DISCARDED) break;
                request.lastFailure = "SAVE_OPERATION_RETURNED_" + result;
                log.warn(I18nUtil.getLogMessage("CharacterSave.queue2"),
                        request.session.accountId, request.session.characterId, request.session.sessionId,
                        request.version, request.attempts, result);
                result = SaveResult.FAILED;
            } catch (Throwable failure) {
                String sqlState = failure instanceof SQLException sql ? sql.getSQLState() : null;
                int vendorCode = failure instanceof SQLException sql ? sql.getErrorCode() : 0;
                request.lastFailure = failure.getClass().getSimpleName() + ":" + sqlState + ":" + vendorCode;
                log.error(I18nUtil.getLogMessage("CharacterSave.queue3"),
                        request.session.accountId, request.session.characterId, request.session.sessionId, request.version,
                        request.attempts, MAX_SAVE_ATTEMPTS, sqlState, vendorCode, failure);
                result = SaveResult.FAILED;
                if (failure instanceof Error) break;
            }
        }
        finish(dispatch, result, System.nanoTime() - startedAt);
        refreshConcurrency();
    }

    private void finish(Dispatch dispatch, SaveResult result, long executionNanos) {
        Request request = dispatch.request;
        boolean retain;
        long retryGeneration;
        int completedAttempts;
        long readyWaitMillis;
        request.executionNanos += executionNanos;
        long waitNanos = System.nanoTime() - request.firstEnqueuedAt - request.executionNanos;
        List<CompletableFuture<SaveResult>> waiters;
        QueueStatus metrics = null;
        synchronized (monitor) {
            Lane lane = accounts.get(request.session.accountId);
            waiters = new ArrayList<>(request.waiters);
            request.waiters.clear();
            if (result != SaveResult.SAVED && request.attempts >= MAX_SAVE_ATTEMPTS) result = SaveResult.DISCARDED;
            retain = result == SaveResult.FAILED && (request.terminal
                    || lane.pending.stream().noneMatch(pending -> pending.session == request.session));
            if (result == SaveResult.SAVED) request.session.committedVersion = request.version;
            if (request.terminal && !retain) {
                request.session.closed = true;
                request.session.finalResult = result;
                request.session.finalRequest = null;
            }
            lane.running = false;
            lane.executingSession = null;
            if (dispatch.reserved) reservedRunning--;
            else sharedRunning--;
            if (retain) {
                request.retryWaiting = true;
                request.retryGeneration++;
                lane.pending.addFirst(request);
            }
            retryGeneration = request.retryGeneration;
            completedAttempts = request.attempts;
            readyWaitMillis = TimeUnit.NANOSECONDS.toMillis(request.readyWaitNanos);
            refreshReadyPlacement(request.session.accountId, lane);
            completedDispatches++;
            if (result == SaveResult.SAVED) savedDispatches++;
            else if (result == SaveResult.DISCARDED) discardedDispatches++;
            else failedDispatches++;
            totalWaitNanos += waitNanos - request.reportedWaitNanos;
            request.reportedWaitNanos = waitNanos;
            totalExecuteNanos += executionNanos;
            if (System.nanoTime() - lastMetricsAt >= TimeUnit.MINUTES.toNanos(1)) {
                lastMetricsAt = System.nanoTime();
                metrics = queueStatus();
            }
        }
        long queueMillis = TimeUnit.NANOSECONDS.toMillis(waitNanos);
        long executionMillis = TimeUnit.NANOSECONDS.toMillis(executionNanos);
        if (request.terminal || queueMillis >= 1_000L || executionMillis >= 2_000L) {
            log.info(I18nUtil.getLogMessage("CharacterSave.queue4"),
                    request.session.accountId, request.session.characterId, request.session.sessionId, request.version,
                    request.terminal, request.realtime, queueMillis, readyWaitMillis,
                    executionMillis, completedAttempts, result);
        }
        if (metrics != null) log.info(I18nUtil.getLogMessage("CharacterSave.queue5"), metrics);
        // 先完成 Future，再回收快照；不在 monitor 中取角色锁或调用业务回调。
        SaveResult completionResult = result;
        waiters.forEach(waiter -> waiter.complete(completionResult));
        if (!retain) release(request);
        if (result != SaveResult.SAVED) {
            log.error(I18nUtil.getLogMessage("CharacterSave.queue6"),
                    request.session.characterId, request.session.sessionId, request.version, completedAttempts, result, retain);
            if (retain) retryOrdinarySave(request, retryGeneration);
        }
    }

    public QueueStatus queueStatus() {
        synchronized (monitor) {
            long retryWaiting = accounts.values().stream()
                    .flatMap(lane -> lane.pending.stream()).filter(request -> request.retryWaiting).count();
            return new QueueStatus(realtimeReady.size(), autosaveReady.size(), reservedRunning, sharedRunning,
                    retryWaiting, completedDispatches, savedDispatches, failedDispatches, discardedDispatches, mergedRequests,
                    TimeUnit.NANOSECONDS.toMillis(totalWaitNanos), TimeUnit.NANOSECONDS.toMillis(totalExecuteNanos));
        }
    }

    public record QueueStatus(int realtimeReady, int autosaveReady, int reservedRunning, int sharedRunning,
                              long retryWaiting, long completedDispatches, long savedDispatches, long failedDispatches,
                              long discardedDispatches, long mergedRequests,
                              long totalWaitMillis, long totalExecuteMillis) {}

    private void release(Request request) {
        try { request.release.run(); }
        catch (Throwable failure) { log.error(I18nUtil.getLogMessage("CharacterSave.queue7"), request.session.characterId, failure); }
    }

    public boolean hasUnfinishedSessions() {
        synchronized (monitor) {
            return accounts.values().stream().anyMatch(lane -> lane.owner != null
                    && (!lane.owner.closed || lane.running || !lane.pending.isEmpty()));
        }
    }

    private void retryOrdinarySave(Request request, long generation) {
        long delay;
        synchronized (monitor) {
            if (!request.retryWaiting || request.retryGeneration != generation) return;
            delay = request.retryDelayMillis;
            request.nextRetryAtMillis = System.currentTimeMillis() + delay;
            request.retryDelayMillis = Math.min(60_000L, delay * 2L);
        }
        try {
            retryScheduler.schedule(() -> wakeRetry(request, generation), delay);
        } catch (RuntimeException failure) {
            log.error(I18nUtil.getLogMessage("CharacterSave.queue8"), request.session.characterId,
                    request.session.sessionId, failure);
            wakeRetry(request, generation);
        }
    }

    private void wakeRetry(Request request, long generation) {
        synchronized (monitor) {
            Lane lane = accounts.get(request.session.accountId);
            // 晚到回调不能唤醒替代快照、新一轮重试或新会话。
            if (lane == null || !lane.pending.contains(request)
                    || !request.retryWaiting || request.retryGeneration != generation) return;
            request.retryWaiting = false;
            request.nextRetryAtMillis = 0L;
            refreshReadyPlacement(request.session.accountId, lane);
        }
        refreshConcurrency();
    }

    @Override
    public void dispose() {
        // 生命周期属于进程。World.shutdown 不能删掉旧 session 的失败存档。
    }

    @FunctionalInterface
    public interface SaveOperation {
        SaveResult save() throws Exception;
    }

    @FunctionalInterface
    public interface RetryScheduler {
        void schedule(Runnable action, long delayMillis);
    }

    public static final class Session {
        private final long createdAt = System.currentTimeMillis();
        private final int accountId;
        private final int characterId;
        private final long loginTime;
        private final long sessionId;
        private long nextVersion;
        private long committedVersion;
        private boolean closing;
        private long closingAt;
        private boolean closed;
        private SaveResult finalResult = SaveResult.DISCARDED;
        private Request finalRequest;

        private Session(int accountId, int characterId, long loginTime, long sessionId) {
            this.accountId = accountId;
            this.characterId = characterId;
            this.loginTime = loginTime;
            this.sessionId = sessionId;
        }

        public int accountId() { return accountId; }
        public long loginTime() { return loginTime; }
        public long sessionId() { return sessionId; }
    }

    private enum ReadyQueue { NONE, REALTIME, AUTOSAVE }

    private record Dispatch(Request request, boolean reserved) {}

    private static final class Lane {
        private Session owner;
        private Session executingSession;
        private final Deque<Request> pending = new ArrayDeque<>();
        private boolean running;
        private ReadyQueue readyQueue = ReadyQueue.NONE;
        private long readyAt;
    }

    private static final class Request {
        private long firstEnqueuedAt = System.nanoTime();
        private long readyWaitNanos;
        private long executionNanos;
        private long reportedWaitNanos;
        private boolean retryWaiting;
        private long retryGeneration;
        private final Session session;
        private final long version;
        private boolean realtime;
        private final boolean terminal;
        private final SaveOperation operation;
        private final Runnable release;
        private long retryDelayMillis = 1_000L;
        private volatile int attempts;
        private volatile String lastFailure = "NONE";
        private long nextRetryAtMillis;
        private CompletableFuture<SaveResult> completion = new CompletableFuture<>();
        private final List<CompletableFuture<SaveResult>> waiters = new ArrayList<>();

        private Request(Session session, long version, boolean realtime, boolean terminal,
                        SaveOperation operation, Runnable release) {
            this.session = session;
            this.version = version;
            this.realtime = realtime;
            this.terminal = terminal;
            this.operation = operation;
            this.release = release;
            waiters.add(completion);
        }
    }
}
