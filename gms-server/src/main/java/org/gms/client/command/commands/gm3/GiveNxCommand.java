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
import org.gms.util.I18nUtil;
import org.gms.util.StringUtil;

public class GiveNxCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("GiveNxCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("GiveNxCommand.message2"));
            return;
        }

        String recv, typeStr = "nx";
        int value, type = -1;
        if (params.length > 1) {
            type = switch (params[0]) {
                case "mp", "抵用券" -> 2;
                case "np", "信用点" -> 4;
                case "nx", "点券" -> 1;
                default -> type;
            };
            typeStr = params[0];

            if (params.length > 2) {
                recv = params[1];
                value = Integer.parseInt(params[2]);
            } else {
                recv = c.getPlayer().getName();
                value = Integer.parseInt(params[1]);
            }
            if (type == -1) {
                type = 1;
                typeStr = "nx";
                recv = params[0];
                value = Integer.parseInt(params[1]);
            }
        } else {
            type = 1;
            recv = c.getPlayer().getName();
            value = Integer.parseInt(params[0]);
        }

        Character victim = c.getWorldServer().getPlayerStorage().getCharacterByName(recv);
        if (victim == null && StringUtil.isNumeric(recv)) {
            victim = c.getWorldServer().getPlayerStorage().getCharacterById(Integer.parseInt(recv));
        }
        if (victim != null) {
            victim.getCashShop().gainCash(type, value);
            player.message(I18nUtil.getMessage("GiveNxCommand.message3", typeStr.toUpperCase()));
        } else {
            player.message(I18nUtil.getMessage("BombCommand.message3", recv));
        }
    }
}
