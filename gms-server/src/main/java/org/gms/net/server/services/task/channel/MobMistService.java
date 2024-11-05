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
package org.gms.net.server.services.task.channel;

import org.gms.config.GameConfig;
import org.gms.net.server.services.BaseScheduler;
import org.gms.net.server.services.BaseService;

/**
 * @author Ronan
 */
public class MobMistService extends BaseService {

    private final MobMistScheduler[] mobMistSchedulers = new MobMistScheduler[GameConfig.getServerInt("channel_locks")];

    public MobMistService() {
        for (int i = 0; i < GameConfig.getServerInt("channel_locks"); i++) {
            mobMistSchedulers[i] = new MobMistScheduler();
        }
    }

    @Override
    public void dispose() {
        for (int i = 0; i < GameConfig.getServerInt("channel_locks"); i++) {
            if (mobMistSchedulers[i] != null) {
                mobMistSchedulers[i].dispose();
                mobMistSchedulers[i] = null;
            }
        }
    }

    public void registerMobMistCancelAction(int mapid, Runnable runAction, long delay) {
        mobMistSchedulers[getChannelSchedulerIndex(mapid)].registerMistCancelAction(runAction, delay);
    }

    private class MobMistScheduler extends BaseScheduler {

        public void registerMistCancelAction(Runnable runAction, long delay) {
            registerEntry(runAction, runAction, delay);
        }

    }

}
