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
import org.gms.config.GameConfig;
import org.gms.util.I18nUtil;

public class SpCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("SpCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("SpCommand.message2"));
            return;
        }

        if (params.length == 1) {
            int newSp = Integer.parseInt(params[0]);
            if (newSp < 0) {
                newSp = 0;
            } else if (newSp > GameConfig.getServerInt("max_ap")) {
                newSp = GameConfig.getServerInt("max_ap");
            }

            player.updateRemainingSp(newSp);
        } else {
            Character victim = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);
            if (victim != null) {
                int newSp = Integer.parseInt(params[1]);
                if (newSp < 0) {
                    newSp = 0;
                } else if (newSp > GameConfig.getServerInt("max_ap")) {
                    newSp = GameConfig.getServerInt("max_ap");
                }

                victim.updateRemainingSp(newSp);

                player.dropMessage(5, I18nUtil.getMessage("SpCommand.message3"));
            } else {
                player.message(I18nUtil.getMessage("SpCommand.message4", params[0]));
            }
        }
    }
}
