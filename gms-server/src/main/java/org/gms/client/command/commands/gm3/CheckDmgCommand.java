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

import org.gms.client.BuffStat;
import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.util.I18nUtil;

public class CheckDmgCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("CheckDmgCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        Character victim = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);
        if (victim != null) {
            int maxBase = victim.calculateMaxBaseDamage(victim.getTotalWatk());
            Integer watkBuff = victim.getBuffedValue(BuffStat.WATK);
            Integer matkBuff = victim.getBuffedValue(BuffStat.MATK);
            int blessing = victim.getSkillLevel(10000000 * player.getJobType() + 12);
            if (watkBuff == null) {
                watkBuff = 0;
            }
            if (matkBuff == null) {
                matkBuff = 0;
            }

            player.dropMessage(5, I18nUtil.getMessage("CheckDmgCommand.message2", victim.getTotalStr(), victim.getTotalDex(), victim.getTotalInt(), victim.getTotalLuk()));
            player.dropMessage(5, I18nUtil.getMessage("CheckDmgCommand.message3", victim.getTotalWatk(), victim.getTotalMagic()));
            player.dropMessage(5, I18nUtil.getMessage("CheckDmgCommand.message4", watkBuff, matkBuff, blessing));
            player.dropMessage(5, I18nUtil.getMessage("CheckDmgCommand.message5", victim.getName(), maxBase));
        } else {
            player.message(I18nUtil.getMessage("BombCommand.message3", params[0]));
        }
    }
}
