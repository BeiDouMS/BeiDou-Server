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
import org.gms.config.YamlConfig;
import org.gms.util.I18nUtil;

public class RatesCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("RatesCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();

        // travel rates not applicable since it's intrinsically a server/environment rate rather than a character rate
        String showMsg_ = "#e" + I18nUtil.getMessage("RatesCommand.message2") + "#n\r\n\r\n";
        showMsg_ += I18nUtil.getMessage("ShowRatesCommand.message6") + "#e#b" + player.getExpRate() + "x#k#n" + (player.hasNoviceExpRate() ? " - novice rate" : "") + "\r\n";
        if (player.getMobExpRate() > 1) {
            showMsg_ += I18nUtil.getMessage("RatesCommand.message4") + "#e#b" + Math.round(player.getMobExpRate() * 100f) / 100f + "x#k#n" + "\r\n";
        }
        showMsg_ += I18nUtil.getMessage("ShowRatesCommand.message12") + "#e#b" + player.getMesoRate() + "x#k#n" + "\r\n";
        showMsg_ += I18nUtil.getMessage("ShowRatesCommand.message17") + "#e#b" + player.getDropRate() + "x#k#n" + "\r\n";
        showMsg_ += I18nUtil.getMessage("ShowRatesCommand.message22") + "#e#b" + player.getBossDropRate() + "x#k#n" + "\r\n";
        if (YamlConfig.config.server.USE_QUEST_RATE) {
            showMsg_ += I18nUtil.getMessage("RatesCommand.message3") + "#e#b" + c.getWorldServer().getQuestRate() + "x#k#n" + "\r\n";
        }

        player.showHint(showMsg_, 300);
    }
}
