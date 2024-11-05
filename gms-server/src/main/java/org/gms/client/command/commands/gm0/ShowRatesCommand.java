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

public class ShowRatesCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("ShowRatesCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        String showMsg = "#e" + I18nUtil.getMessage("ShowRatesCommand.message2") + "#n\r\n";
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message3") + "#k" + c.getWorldServer().getExpRate() + "x#k" + "\r\n";
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message4") + "#k" + player.getRawExpRate() + "x#k" + "\r\n";
        if (player.getCouponExpRate() != 1) {
            showMsg += I18nUtil.getMessage("ShowRatesCommand.message5") + "#k" + player.getCouponExpRate() + "x#k" + "\r\n";
        }
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message6") + "#e#b" + player.getExpRate() + "x#k#n" + (player.hasNoviceExpRate() ? I18nUtil.getMessage("ShowRatesCommand.message7") : "") + "\r\n";

        showMsg += "\r\n#e" + I18nUtil.getMessage("ShowRatesCommand.message8") + "#n\r\n";
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message9") + "#k" + c.getWorldServer().getMesoRate() + "x#k" + "\r\n";
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message10") + "#k" + player.getRawMesoRate() + "x#k" + "\r\n";
        if (player.getCouponMesoRate() != 1) {
            showMsg += I18nUtil.getMessage("ShowRatesCommand.message11") + "#k" + player.getCouponMesoRate() + "x#k" + "\r\n";
        }
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message12") + "#e#b" + player.getMesoRate() + "x#k#n" + "\r\n";

        showMsg += "\r\n#e" + I18nUtil.getMessage("ShowRatesCommand.message13") + "#n\r\n";
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message14") + "#k" + c.getWorldServer().getDropRate() + "x#k" + "\r\n";
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message15") + "#k" + player.getRawDropRate() + "x#k" + "\r\n";
        if (player.getCouponDropRate() != 1) {
            showMsg += I18nUtil.getMessage("ShowRatesCommand.message16") + "#k" + player.getCouponDropRate() + "x#k" + "\r\n";
        }
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message17") + "#e#b" + player.getDropRate() + "x#k#n" + "\r\n";

        showMsg += "\r\n#e" + I18nUtil.getMessage("ShowRatesCommand.message18") + "#n\r\n";
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message19") + "#k" + c.getWorldServer().getBossDropRate() + "x#k" + "\r\n";
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message20") + "#k" + player.getRawDropRate() + "x#k" + "\r\n";
        if (player.getCouponDropRate() != 1) {
            showMsg += I18nUtil.getMessage("ShowRatesCommand.message21") + "#k" + player.getCouponDropRate() + "x#k" + "\r\n";
        }
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message22") + "#e#b" + player.getBossDropRate() + "x#k#n" + "\r\n";

        if (GameConfig.getServerBoolean("use_quest_rate")) {
            showMsg += "\r\n#e" + I18nUtil.getMessage("ShowRatesCommand.message23") + "#n\r\n";
            showMsg += I18nUtil.getMessage("ShowRatesCommand.message24") + "#e#b" + c.getWorldServer().getQuestRate() + "x#k#n" + "\r\n";
        }

        showMsg += "\r\n";
        showMsg += I18nUtil.getMessage("ShowRatesCommand.message25") + "#e#b" + c.getWorldServer().getTravelRate() + "x#k#n" + "\r\n";

        player.showHint(showMsg, 300);
    }
}
