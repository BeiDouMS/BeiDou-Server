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
package org.gms.client.command.commands.gm0;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.config.GameConfig;
import org.gms.util.I18nUtil;

public class StatDexCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("StatDexCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        int remainingAp = player.getRemainingAp();

        int amount;
        if (params.length > 0) {
            try {
                amount = Math.min(Integer.parseInt(params[0]), remainingAp);
            } catch (NumberFormatException e) {
                player.dropMessage(I18nUtil.getMessage("StatStrCommand.message2"));
                return;
            }
        } else {
            amount = Math.min(remainingAp, GameConfig.getServerInt("max_ap") - player.getDex());
        }
        if (!player.assignDex(Math.max(amount, 0))) {
            player.dropMessage(I18nUtil.getMessage("StatStrCommand.message3",  GameConfig.getServerInt("max_ap")));
        }
    }
}
