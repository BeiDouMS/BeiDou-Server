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
package org.gms.net.server.services.task.world;

import org.gms.net.server.services.BaseScheduler;
import org.gms.net.server.services.BaseService;
import org.gms.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ronan
 */
public class CharacterSaveService extends BaseService {
    private static final Logger log = LoggerFactory.getLogger(CharacterSaveService.class);

    CharacterSaveScheduler chrSaveScheduler = new CharacterSaveScheduler();

    @Override
    public void dispose() {
        if (chrSaveScheduler != null) {
            // 关服：先把还没跑到的转场存档全部执行掉再销毁，否则这些角色的最后一次落库会被静默丢弃
            int flushed = chrSaveScheduler.flushPendingSaves();
            if (flushed > 0) {
                log.info(I18nUtil.getLogMessage("CharacterSaveService.dispose.info1"), flushed);
            }
            chrSaveScheduler.dispose();
            chrSaveScheduler = null;
        }
    }

    public void registerSaveCharacter(int characterId, Runnable runAction) {
        chrSaveScheduler.registerSaveCharacter(characterId, runAction);
    }

    public void unregisterSaveCharacter(int characterId) {
        chrSaveScheduler.unregisterSaveCharacter(characterId);
    }

    private class CharacterSaveScheduler extends BaseScheduler {

        public void registerSaveCharacter(Integer characterId, Runnable runAction) {
            registerEntry(characterId, runAction, 0);
        }

        public void unregisterSaveCharacter(Integer characterId) {
            interruptEntry(characterId, false);
        }

        public int flushPendingSaves() {
            return flushPendingEntries();
        }

    }

}
