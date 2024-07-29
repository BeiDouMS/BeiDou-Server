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
import org.gms.client.autoban.AutobanFactory;
import org.gms.client.command.Command;
import org.gms.net.server.Server;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;

public class IgnoreCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("IgnoreCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("IgnoreCommand.message2"));
            return;
        }
        Character victim = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);
        if (victim == null) {
            player.message(I18nUtil.getMessage("BombCommand.message3", params[0]));
            return;
        }

        boolean ignored = AutobanFactory.toggleIgnored(victim.getId());
        player.yellowMessage(ignored ? I18nUtil.getMessage("IgnoreCommand.message3", victim.getName()) : I18nUtil.getMessage("IgnoreCommand.message4", victim.getName()));
        String message_ = (ignored ? I18nUtil.getMessage("IgnoreCommand.message5", player.getName(), victim.getName()) : I18nUtil.getMessage("IgnoreCommand.message6", player.getName(), victim.getName()));
        Server.getInstance().broadcastGMMessage(c.getWorld(), PacketCreator.serverNotice(5, message_));

    }
}
