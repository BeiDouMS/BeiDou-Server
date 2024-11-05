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
public class MobClearSkillService extends BaseService {

    private final MobClearSkillScheduler[] mobClearSkillSchedulers = new MobClearSkillScheduler[GameConfig.getServerInt("channel_locks")];

    public MobClearSkillService() {
        for (int i = 0; i < GameConfig.getServerInt("channel_locks"); i++) {
            mobClearSkillSchedulers[i] = new MobClearSkillScheduler();
        }
    }

    @Override
    public void dispose() {
        for (int i = 0; i < GameConfig.getServerInt("channel_locks"); i++) {
            if (mobClearSkillSchedulers[i] != null) {
                mobClearSkillSchedulers[i].dispose();
                mobClearSkillSchedulers[i] = null;
            }
        }
    }

    public void registerMobClearSkillAction(int mapid, Runnable runAction, long delay) {
        mobClearSkillSchedulers[getChannelSchedulerIndex(mapid)].registerClearSkillAction(runAction, delay);
    }

    private class MobClearSkillScheduler extends BaseScheduler {

        public void registerClearSkillAction(Runnable runAction, long delay) {
            registerEntry(runAction, runAction, delay);
        }

    }

}
