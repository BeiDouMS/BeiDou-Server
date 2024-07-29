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
import org.gms.net.server.Server;
import org.gms.net.server.channel.Channel;
import org.gms.util.I18nUtil;

public class OnlineTwoCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("OnlineTwoCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        int total = 0;
        for (Channel ch : Server.getInstance().getChannelsFromWorld(player.getWorld())) {
            int size = ch.getPlayerStorage().getAllCharacters().size();
            total += size;
            StringBuilder s = new StringBuilder(I18nUtil.getMessage("OnlineTwoCommand.message2", ch.getId(), size)).append("\r\n");
            if (ch.getPlayerStorage().getAllCharacters().size() < 50) {
                for (Character chr : ch.getPlayerStorage().getAllCharacters()) {
                    s.append(Character.makeMapleReadable(chr.getName())).append(", ");
                }
                player.dropMessage(6, s.substring(0, s.length() - 2));
            }
        }

        //player.dropMessage(6, "There are a total of " + total + " players online.");
        player.showHint(I18nUtil.getMessage("OnlineTwoCommand.message3", total), 300);
    }
}
