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
package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.command.Command;
import config.YamlConfig;

public class RatesCommand extends Command {
    {
        setDescription("Show your rates.");
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();

        // travel rates not applicable since it's intrinsically a server/environment rate rather than a character rate
        String showMsg_ = "#e玩家倍率#n" + "\r\n\r\n";
        showMsg_ += "经验: #e#b" + player.getExpRate() + "x#k#n" + (player.hasNoviceExpRate() ? " - 新手倍率" : "") + "\r\n";
        showMsg_ += "怪物经验: #e#b" + player.getMobExpRate() + "x#k#n" + (player.hasNoviceExpRate() ? " - 新手倍率" : "") + "\r\n";
        showMsg_ += "金币: #e#b" + player.getMesoRate() + "x#k#n" + "\r\n";
        showMsg_ += "爆率: #e#b" + player.getDropRate() + "x#k#n" + "\r\n";
        showMsg_ += "BOSS 爆率: #e#b" + player.getBossDropRate() + "x#k#n" + "\r\n";
        if (YamlConfig.config.server.USE_QUEST_RATE) {
            showMsg_ += "任务: #e#b" + c.getWorldServer().getQuestRate() + "x#k#n" + "\r\n";
        }

        player.showHint(showMsg_, 300);
    }
}
