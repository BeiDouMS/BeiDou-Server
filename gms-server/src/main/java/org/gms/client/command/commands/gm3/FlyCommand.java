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
import org.gms.util.I18nUtil;

public class FlyCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("FlyCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) { // fly option will become available for any character of that account
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("FlyCommand.message2"));
            return;
        }

        Integer accid = c.getAccID();
        Server srv = Server.getInstance();
        String sendStr = "";
        if (params[0].equalsIgnoreCase("on") || params[0].equalsIgnoreCase("开启")) {
            sendStr += I18nUtil.getMessage("FlyCommand.message3");
            if (!srv.canFly(accid)) {
                sendStr += I18nUtil.getMessage("FlyCommand.message4");
            }

            srv.changeFly(c.getAccID(), true);
        } else {
            sendStr += I18nUtil.getMessage("FlyCommand.message5");
            if (srv.canFly(accid)) {
                sendStr += I18nUtil.getMessage("FlyCommand.message4");
            }

            srv.changeFly(c.getAccID(), false);
        }

        player.dropMessage(6, sendStr);
    }
}
