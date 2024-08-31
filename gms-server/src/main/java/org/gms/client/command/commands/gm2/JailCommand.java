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
import org.gms.constants.id.MapId;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Portal;
import org.gms.util.I18nUtil;
import org.gms.util.StringUtil;

import static java.util.concurrent.TimeUnit.MINUTES;

public class JailCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("JailCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("JailCommand.message2"));
            return;
        }

        int minutesJailed = 5;
        if (params.length >= 2) {
            minutesJailed = Integer.parseInt(params[1]);
            if (minutesJailed <= 0) {
                player.yellowMessage(I18nUtil.getMessage("JailCommand.message2"));
                return;
            }
        }

        Character victim = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);
        if (victim == null && StringUtil.isNumeric(params[0])) {
            victim = c.getWorldServer().getPlayerStorage().getCharacterById(Integer.parseInt(params[0]));
        }
        if (victim != null) {
            if (victim.isGM()) {
                player.yellowMessage(I18nUtil.getMessage("JailCommand.message5"));
                return;
            }
            victim.addJailExpirationTime(MINUTES.toMillis(minutesJailed));

            if (victim.getMapId() != MapId.JAIL) {    // those gone to jail won't be changing map anyway
                MapleMap target = c.getChannelServer().getMapFactory().getMap(MapId.JAIL);
                Portal targetPortal = target.getPortal(0);
                victim.saveLocation("JAIL");
                victim.changeMap(target, targetPortal);
                player.message(I18nUtil.getMessage("JailCommand.message3", victim.getName(), minutesJailed));
            } else {
                player.message(I18nUtil.getMessage("JailCommand.message4", victim.getName(), minutesJailed));
            }

        } else {
            player.message(I18nUtil.getMessage("BombCommand.message3", params[0]));
        }
    }
}
