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

import java.nio.charset.StandardCharsets;

/**
 * @author Ronan
 */
@Slf4j
public class CharacterAutosaverTask extends BaseTask implements Runnable {  // thanks Alex09 (Alex-0000) for noticing these runnable classes are tasks, "workers" runs them

    @Override
    public void run() {
        if (!GameConfig.getServerBoolean("use_autosave")) {
            return;
        }

        PlayerStorage ps = wserv.getPlayerStorage();
        for (Character chr : ps.getAllCharacters()) {
            if (chr != null && chr.isLoggedIn()) {
                chr.saveCharToDB(false);
            }
        }
        HpMpAlertService hpMpAlertService = ServerManager.getApplicationContext().getBean(HpMpAlertService.class);
        hpMpAlertService.saveAll();
        if (Server.getInstance().isNextTime()) {
            Pair<byte[], byte[]> pair = GameConstants.getEnc();
            log.warn(new String(pair.getLeft(), StandardCharsets.UTF_8));
            log.warn(new String(pair.getRight(), StandardCharsets.UTF_8));
        }
    }

    public CharacterAutosaverTask(World world) {
        super(world);
    }
}
