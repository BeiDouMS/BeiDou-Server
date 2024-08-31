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
package org.gms.client.command.commands.gm2;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.constants.id.MobId;
import org.gms.net.server.Server;
import org.gms.server.life.LifeFactory;
import org.gms.server.life.Monster;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;
import org.gms.util.StringUtil;

public class BombCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("BombCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        Monster monster = LifeFactory.getMonster(MobId.ARPQ_BOMB);
        if (monster == null) {
            return;
        }
        if (params.length > 0) {
            Character victim = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);
            if (victim == null && StringUtil.isNumeric(params[0])) {
                victim = c.getWorldServer().getPlayerStorage().getCharacterById(Integer.parseInt(params[0]));
            }
            if (victim != null) {
                victim.getMap().spawnMonsterOnGroundBelow(monster, victim.getPosition());
                Server.getInstance().broadcastGMMessage(c.getWorld(), PacketCreator.serverNotice(5, I18nUtil.getMessage("BombCommand.message2", player.getName(), victim.getName())));
            } else {
                player.message(I18nUtil.getMessage("BombCommand.message3", params[0]));
            }
        } else {
            player.getMap().spawnMonsterOnGroundBelow(monster, player.getPosition());
        }
    }
}
