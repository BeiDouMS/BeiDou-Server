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
package org.gms.net.server.task;

import lombok.extern.slf4j.Slf4j;
import org.gms.client.Character;
import org.gms.config.GameConfig;
import org.gms.constants.game.GameConstants;
import org.gms.manager.ServerManager;
import org.gms.net.server.PlayerStorage;
import org.gms.net.server.Server;
import org.gms.net.server.world.World;
import org.gms.service.HpMpAlertService;
import org.gms.util.Pair;
import org.gms.util.I18nUtil;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ronan
 */
@Slf4j
public class CharacterAutosaverTask extends BaseTask implements Runnable {  // thanks Alex09 (Alex-0000) for noticing these runnable classes are tasks, "workers" runs them

    // 不可重入保护：上一轮未结束/异常未退出时，下一轮直接跳过，
    // 避免 scheduleAtFixedRate/scheduleWithFixedDelay 在单轮卡顿后追赶积压，
    // 导致多个保存任务并发抢同一角色的 synchronized 锁或同时打满 DB 连接池。
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void run() {
        if (!GameConfig.getServerBoolean("use_autosave")) {
            return;
        }

        if (!running.compareAndSet(false, true)) {
            log.warn(I18nUtil.getLogMessage("CharacterSave.autosaveSkipped"));
            return;
        }

        long startMs = System.currentTimeMillis();
        int onlineCount = 0;
        int savedCount = 0;
        int failedCount = 0;
        try {
            PlayerStorage ps = wserv.getPlayerStorage();
            Collection<Character> all = ps.getAllCharacters();
            List<CompletableFuture<Character.SaveResult>> saves = new ArrayList<>();
            for (Character chr : all) {
                if (chr == null) {
                    continue;
                }
                onlineCount++;
                if (chr.isLoggedIn()) {
                    saves.add(chr.requestAutosave());
                }
            }
            for (CompletableFuture<Character.SaveResult> save : saves) {
                if (save.join() == Character.SaveResult.SAVED) savedCount++;
                else failedCount++;
            }
            HpMpAlertService hpMpAlertService = ServerManager.getApplicationContext().getBean(HpMpAlertService.class);
            hpMpAlertService.saveAll();
            if (Server.getInstance().isNextTime()) {
                Pair<byte[], byte[]> pair = GameConstants.getEnc();
                log.warn(new String(pair.getLeft(), StandardCharsets.UTF_8));
                log.warn(new String(pair.getRight(), StandardCharsets.UTF_8));
            }
        } finally {
            long cost = System.currentTimeMillis() - startMs;
            log.info(I18nUtil.getLogMessage("CharacterSave.autosaveDone"),
                    wserv.getId(), onlineCount, savedCount, failedCount, cost);
            running.set(false);
        }
    }

    public CharacterAutosaverTask(World world) {
        super(world);
    }
}
