package org.gms.net.server;

import java.util.concurrent.ScheduledFuture;

import org.gms.util.Randomizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public abstract class Timer {

    private ScheduledThreadPoolExecutor ses;
    protected String file;
    protected String name;

    public void start() {
        if (this.ses != null && !this.ses.isShutdown() && !this.ses.isTerminated()) {
            return;
        }
        this.file = "Logs/Log_" + this.name + "_Except.rtf";
        final String tname = this.name + Randomizer.nextInt();
        final ThreadFactory thread = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r);
                t.setName(tname + "-Worker-" + this.threadNumber.getAndIncrement());
                return t;
            }
        };
        final ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(7, thread);
        stpe.setKeepAliveTime(10L, TimeUnit.MINUTES);
        stpe.allowCoreThreadTimeOut(true);
        stpe.setCorePoolSize(7);
        stpe.setMaximumPoolSize(14);
        stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        this.ses = stpe;
    }

    public static class GuiTimer extends Timer {

        private static GuiTimer instance = new GuiTimer();

        private GuiTimer() {
            name = "GuiTimer";
        }

        public static GuiTimer getInstance() {
            return instance;
        }
    }

    public void stop() {
        try {
            this.ses.shutdownNow();
        } catch (Exception e) {
            //FilePrinter.printError("Timer.txt", (Throwable) e);
        }
    }

    public ScheduledFuture<?> register(final Runnable r, final long repeatTime, final long delay) {
        if (this.ses == null) {
            return null;
        }
        return this.ses.scheduleAtFixedRate((Runnable) new LoggingSaveRunnable(r, this.file), delay, repeatTime, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> register(final Runnable r, final long repeatTime) {
        if (this.ses == null) {
            return null;
        }
        return this.ses.scheduleAtFixedRate((Runnable) new LoggingSaveRunnable(r, this.file), 0L, repeatTime, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(final Runnable r, final long delay) {
        if (this.ses == null) {
            return null;
        }
        return this.ses.schedule((Runnable) new LoggingSaveRunnable(r, this.file), delay, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleAtTimestamp(final Runnable r, final long timestamp) {
        return this.schedule(r, timestamp - System.currentTimeMillis());
    }

    public static class WorldTimer extends Timer {

        private static final WorldTimer instance;

        private WorldTimer() {
            this.name = "Worldtimer";
        }

        public static WorldTimer getInstance() {
            return instance;
        }

        static {
            instance = new WorldTimer();
        }
    }

    public static class MapTimer extends Timer {

        private static final MapTimer instance;

        private MapTimer() {
            this.name = "Maptimer";
        }

        public static MapTimer getInstance() {
            return instance;
        }

        static {
            instance = new MapTimer();
        }
    }

    public static class BuffTimer extends Timer {

        private static final BuffTimer instance;

        private BuffTimer() {
            this.name = "Bufftimer";
        }

        public static BuffTimer getInstance() {
            return instance;
        }

        static {
            instance = new BuffTimer();
        }
    }

    public static class EventTimer extends Timer {

        private static final EventTimer instance;

        private EventTimer() {
            this.name = "Eventtimer";
        }

        public static EventTimer getInstance() {
            return instance;
        }

        static {
            instance = new EventTimer();
        }
    }

    public static class RespawnTimer extends Timer {

        private static RespawnTimer instance;

        private RespawnTimer() {
            this.name = "RespawnTimer";
        }

        public static RespawnTimer getInstance() {
            return instance;
        }

        static {
            instance = new RespawnTimer();
        }
    }

    public static class CloneTimer extends Timer {

        private static final CloneTimer instance;

        private CloneTimer() {
            this.name = "Clonetimer";
        }

        public static CloneTimer getInstance() {
            return instance;
        }

        static {
            instance = new CloneTimer();
        }
    }

    public static class EtcTimer extends Timer {

        private static final EtcTimer instance;

        private EtcTimer() {
            this.name = "Etctimer";
        }

        public static EtcTimer getInstance() {
            return instance;
        }

        static {
            instance = new EtcTimer();
        }
    }

    public static class MobTimer extends Timer {

        private static final MobTimer instance;

        private MobTimer() {
            this.name = "Mobtimer";
        }

        public static MobTimer getInstance() {
            return instance;
        }

        static {
            instance = new MobTimer();
        }
    }

    public static class CheatTimer extends Timer {

        private static final CheatTimer instance;

        private CheatTimer() {
            this.name = "Cheattimer";
        }

        public static CheatTimer getInstance() {
            return instance;
        }

        static {
            instance = new CheatTimer();
        }
    }

    public static class PingTimer extends Timer {

        private static final PingTimer instance;

        private PingTimer() {
            this.name = "Pingtimer";
        }

        public static PingTimer getInstance() {
            return instance;
        }

        static {
            instance = new PingTimer();
        }
    }

    private static class LoggingSaveRunnable implements Runnable {

        Runnable r;
        String file;

        public LoggingSaveRunnable(final Runnable r, final String file) {
            this.r = r;
            this.file = file;
        }

        @Override
        public void run() {
            try {
                this.r.run();
            } catch (Throwable t) {
                //FilePrinter.printError("Timer.txt", t);
            }
        }
    }
}
