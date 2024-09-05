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
   @Author: MedicOP - Add clock commands
*/
package org.gms.client.command.commands.gm3;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;
import org.gms.util.StringUtil;

public class TimerCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("TimerCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 2) {
            player.yellowMessage(I18nUtil.getMessage("TimerCommand.message2"));
            return;
        }

        Character victim = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);
        if (victim == null && StringUtil.isNumeric(params[0])) {
            victim = c.getWorldServer().getPlayerStorage().getCharacterById(Integer.parseInt(params[0]));
        }
        if (victim != null) {
            if (params[1].equalsIgnoreCase("remove") || "移除".equals(params[1])) {
                victim.sendPacket(PacketCreator.removeClock());
            } else {
                try {
                    victim.sendPacket(PacketCreator.getClock(Integer.parseInt(params[1])));
                } catch (NumberFormatException e) {
                    player.yellowMessage(I18nUtil.getMessage("TimerCommand.message2"));
                }
            }
        } else {
            player.message(I18nUtil.getMessage("BombCommand.message3", params[0]));
        }
    }
}
