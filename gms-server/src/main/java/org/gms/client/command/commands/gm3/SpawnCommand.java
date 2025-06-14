/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
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

/*
   @Author: Arthur L - Refactored command content into modules
*/
package org.gms.client.command.commands.gm3;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.server.life.LifeFactory;
import org.gms.server.life.Monster;
import org.gms.util.I18nUtil;

public class SpawnCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("SpawnCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("SpawnCommand.message2"));
            return;
        }

        Monster monster = LifeFactory.getMonster(Integer.parseInt(params[0]));
        if (monster == null) {
            player.dropMessage(6,"怪物ID：[" + params[0] + "] 不存在，无法召唤。");
            return;
        }
        if (params.length == 2) {
            for (int i = 0; i < Integer.parseInt(params[1]); i++) {
                player.getMap().spawnMonsterOnGroundBelow(new Monster(monster.getId(), monster.getStats()), player.getPosition());
            }
        } else {
            player.getMap().spawnMonsterOnGroundBelow(monster, player.getPosition());
        }
    }
}
