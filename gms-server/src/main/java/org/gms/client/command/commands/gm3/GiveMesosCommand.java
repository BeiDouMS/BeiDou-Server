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

public class GiveMesosCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("GiveMesosCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("GiveMesosCommand.message2"));
            return;
        }

        String recv, value;
        long mesos_ = 0;

        if (params.length == 2) {
            recv = params[0];
            value = params[1];
        } else {
            recv = c.getPlayer().getName();
            value = params[0];
        }

        try {
            mesos_ = Long.parseLong(value);
            if (mesos_ > Integer.MAX_VALUE) {
                mesos_ = Integer.MAX_VALUE;
            } else if (mesos_ < Integer.MIN_VALUE) {
                mesos_ = Integer.MIN_VALUE;
            }
        } catch (NumberFormatException nfe) {
            if (value.contentEquals("max")) {  // "max" descriptor suggestion thanks to Vcoc
                mesos_ = Integer.MAX_VALUE;
            } else if (value.contentEquals("min")) {
                mesos_ = Integer.MIN_VALUE;
            }
        }

        Character victim = c.getWorldServer().getPlayerStorage().getCharacterByName(recv);
        if (victim == null && StringUtil.isNumeric(recv)) {
            victim = c.getWorldServer().getPlayerStorage().getCharacterById(Integer.parseInt(recv));
        }
        if (victim != null) {
            victim.gainMeso((int) mesos_, true);
            player.message(I18nUtil.getMessage("GiveMesosCommand.message3"));
        } else {
            player.message(I18nUtil.getMessage("BombCommand.message3", recv));
        }
    }
}
