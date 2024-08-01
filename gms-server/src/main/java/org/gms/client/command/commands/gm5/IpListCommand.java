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
package org.gms.client.command.commands.gm5;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.constants.game.GameConstants;
import org.gms.net.server.Server;
import org.gms.net.server.world.World;
import org.gms.util.I18nUtil;

import java.util.Collection;

/**
 * @author Mist
 * @author Blood (Tochi)
 * @author Ronan
 */
public class IpListCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("IpListCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        StringBuilder str = new StringBuilder(I18nUtil.getMessage("IpListCommand.message2"));

        for (World w : Server.getInstance().getWorlds()) {
            Collection<Character> chars = w.getPlayerStorage().getAllCharacters();

            if (!chars.isEmpty()) {
                str.append("\r\n").append(GameConstants.WORLD_NAMES[w.getId()]).append("\r\n");

                for (Character chr : chars) {
                    str.append("  ").append(chr.getName()).append(" - ").append(chr.getClient().getRemoteAddress()).append("\r\n");
                }
            }
        }

        c.getAbstractPlayerInteraction().npcTalk(22000, str.toString());
    }

}