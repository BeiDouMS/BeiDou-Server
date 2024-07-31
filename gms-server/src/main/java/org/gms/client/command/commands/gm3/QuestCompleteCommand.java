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
import org.gms.server.quest.Quest;
import org.gms.util.I18nUtil;

public class QuestCompleteCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("QuestCompleteCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();

        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("QuestCompleteCommand.message2"));
            return;
        }

        int questId = Integer.parseInt(params[0]);

        if (player.getQuestStatus(questId) == 1) {
            Quest quest = Quest.getInstance(questId);
            if (quest != null && quest.getNpcRequirement(true) != -1) {
                c.getAbstractPlayerInteraction().forceCompleteQuest(questId, quest.getNpcRequirement(true));
            } else {
                c.getAbstractPlayerInteraction().forceCompleteQuest(questId);
            }

            player.dropMessage(5, I18nUtil.getMessage("QuestCompleteCommand.message3", questId));
        } else {
            player.dropMessage(5, I18nUtil.getMessage("QuestCompleteCommand.message4", questId));
        }
    }
}
