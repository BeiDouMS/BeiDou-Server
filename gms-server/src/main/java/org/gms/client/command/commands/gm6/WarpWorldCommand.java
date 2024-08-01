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
package org.gms.client.command.commands.gm6;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.net.server.Server;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class WarpWorldCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("WarpWorldCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("WarpWorldCommand.message2"));
            return;
        }

        Server server = Server.getInstance();
        byte worldb = Byte.parseByte(params[0]);
        if (worldb <= (server.getWorldsSize() - 1)) {
            try {
                String[] socket = server.getInetSocket(c, worldb, c.getChannel());
                c.getWorldServer().removePlayer(player);
                player.getMap().removePlayer(player);//LOL FORGOT THIS ><
                player.setSessionTransitionState();
                player.setWorld(worldb);
                player.saveCharToDB();//To set the new world :O (true because else 2 player instances are created, one in both worlds)
                c.sendPacket(PacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
            } catch (UnknownHostException | NumberFormatException ex) {
                ex.printStackTrace();
                player.message(I18nUtil.getMessage("WarpWorldCommand.message3"));
            }

        } else {
            player.message(I18nUtil.getMessage("WarpWorldCommand.message4", server.getWorldsSize() - 1));
        }
    }
}
