/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gms.net.server.services;

import org.gms.config.GameConfig;
import org.gms.net.server.Server;
import org.gms.server.TimerManager;
import org.gms.util.I18nUtil;
import org.gms.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ronan
 */
public abstract class BaseScheduler {
    private static final Logger log = LoggerFactory.getLogger(BaseScheduler.class);
    private int idleProcs = 0;
    private final List<SchedulerListener> listeners = new LinkedList<>();
    private final List<Lock> externalLocks = new LinkedList<>();
    private final Map<Object, Pair<Runnable, Long>> registeredEntries = new HashMap<>();

    private ScheduledFuture<?> schedulerTask = null;
    private final Lock schedulerLock = new ReentrantLock(true);

    protected BaseScheduler() {
    }

    // NOTE: practice EXTREME caution when adding external locks to the scheduler system, if you don't know what you're doing DON'T USE THIS.
    protected BaseScheduler(List<Lock> extLocks) {
        externalLocks.addAll(extLocks);
    }

    protected void addListener(SchedulerListener listener) {
        listeners.add(listener);
    }

    private void lockScheduler() {
        externalLocks.forEach(Lock::lock);
        schedulerLock.lock();
    }

    private void unlockScheduler() {
        externalLocks.forEach(Lock::unlock);
        schedulerLock.unlock();
    }

    private void runBaseSchedule() {
        List<Object> toRemove;
        Map<Object, Pair<Runnable, Long>> registeredEntriesCopy;

        lockScheduler();
        try {
            if (registeredEntries.isEmpty()) {
                idleProcs++;

                if (idleProcs >= GameConfig.getServerInt("mob_status_monitor_idle")) {
                    if (schedulerTask != null) {
                        schedulerTask.cancel(false);
                        schedulerTask = null;
                    }
                }

                return;
            }

            idleProcs = 0;
            registeredEntriesCopy = new HashMap<>(registeredEntries);
        } finally {
            unlockScheduler();
        }

        long timeNow = Server.getInstance().getCurrentTime();
        toRemove = new LinkedList<>();
        for (Entry<Object, Pair<Runnable, Long>> rmd : registeredEntriesCopy.entrySet()) {
            Pair<Runnable, Long> r = rmd.getValue();

            if (r.getRight() < timeNow) {
                r.getLeft().run();  // runs the scheduled action
                toRemove.add(rmd.getKey());
            }
        }

        if (!toRemove.isEmpty()) {
            lockScheduler();
            try {
                for (Object o : toRemove) {
                    registeredEntries.remove(o);
                }
            } finally {
                unlockScheduler();
            }
        }

        dispatchRemovedEntries(toRemove, true);
    }

    protected void registerEntry(Object key, Runnable removalAction, long duration) {
        lockScheduler();
        try {
            idleProcs = 0;
            if (schedulerTask == null) {
                schedulerTask = TimerManager.getInstance().register(this::runBaseSchedule, GameConfig.getServerLong("mob_status_monitor_proc"), GameConfig.getServerLong("mob_status_monitor_proc"));
            }

            registeredEntries.put(key, new Pair<>(removalAction, Server.getInstance().getCurrentTime() + duration));
        } finally {
            unlockScheduler();
        }
    }

    protected void interruptEntry(Object key) {
        interruptEntry(key, true);
    }

    protected void interruptEntry(Object key, boolean executeBeforeStop) {
        Runnable toRun = null;

        lockScheduler();
        try {
            Pair<Runnable, Long> rm = registeredEntries.remove(key);
            if (rm != null) {
                toRun = rm.getLeft();
            }
        } finally {
            unlockScheduler();
        }

        if (toRun != null && executeBeforeStop) {
            toRun.run();
        }

        dispatchRemovedEntries(Collections.singletonList(key), false);
    }

    private void dispatchRemovedEntries(List<Object> toRemove, boolean fromUpdate) {
        for (SchedulerListener listener : listeners.toArray(new SchedulerListener[listeners.size()])) {
            listener.removedScheduledEntries(toRemove, fromUpdate);
        }
    }

    public void dispose() {
        lockScheduler();
        try {
            if (schedulerTask != null) {
                schedulerTask.cancel(false);
                schedulerTask = null;
            }

            listeners.clear();
            registeredEntries.clear();
        } finally {
            unlockScheduler();
            externalLocks.clear();
        }
    }

    /**
     * 立即执行所有尚未到期/尚未被调度跑到的条目并清空。给关服路径用：
     * 换频道/进商城这类「转场」存档只是注册到本调度器、由 200ms 一跳的定时器执行，
     * 关服时若直接 dispose 会把没跑到的存档整条丢掉。返回执行的条目数。
     */
    protected int flushPendingEntries() {
        List<Pair<Runnable, Long>> pending;
        lockScheduler();
        try {
            if (schedulerTask != null) {
                schedulerTask.cancel(false);
                schedulerTask = null;
            }
            pending = new ArrayList<>(registeredEntries.values());
            registeredEntries.clear();
        } finally {
            unlockScheduler();
        }

        for (Pair<Runnable, Long> entry : pending) {
            try {
                entry.getLeft().run();
            } catch (Exception e) {
                log.error(I18nUtil.getLogMessage("BaseScheduler.flushPendingEntries.error1"), e);
            }
        }
        return pending.size();
    }
}
