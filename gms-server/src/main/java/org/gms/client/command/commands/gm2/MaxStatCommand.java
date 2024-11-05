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
import org.gms.client.Stat;
import org.gms.client.command.Command;
import org.gms.config.GameConfig;
import org.gms.util.I18nUtil;

public class MaxStatCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("MaxStatCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        player.loseExp(player.getExp(), false, false);
        player.setLevel(255);
        player.resetPlayerRates();
        if (GameConfig.getServerBoolean("use_add_rates_by_level")) {
            player.setPlayerRates();
        }
        player.setWorldRates();
        player.updateStrDexIntLuk(Short.MAX_VALUE);
        player.setFame(13337);
        player.updateMaxHpMaxMp(30000, 30000);
        player.updateSingleStat(Stat.LEVEL, 255);
        player.updateSingleStat(Stat.FAME, 13337);
        player.yellowMessage(I18nUtil.getMessage("MaxStatCommand.message2"));
    }
}
