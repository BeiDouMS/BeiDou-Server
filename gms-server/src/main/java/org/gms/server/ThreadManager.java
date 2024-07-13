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
package org.gms.server;

import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Ronan
 */
public class ThreadManager {
    @Getter
    private static final ThreadManager instance = new ThreadManager();

    private ExecutorService executorService;

    private ThreadManager() {}

    public void newTask(Runnable r) {
        executorService.execute(r);
    }

    public void start() {
        // 注意，虚拟线程不建议池化，所以也不需要拒绝策略
        executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void stop() {
        executorService.shutdown();
        try {
            boolean ignore = executorService.awaitTermination(5, MINUTES);
        } catch (InterruptedException ignore) {

        }
    }

}
