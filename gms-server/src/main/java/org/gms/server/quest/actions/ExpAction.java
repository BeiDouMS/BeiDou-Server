/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

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
package org.gms.server.quest.actions;

import org.gms.client.Character;
import org.gms.config.GameConfig;
import org.gms.provider.Data;
import org.gms.provider.DataTool;
import org.gms.server.quest.Quest;
import org.gms.server.quest.QuestActionType;
import org.gms.util.NumberTool;

/**
 * @author Tyler (Twdtwd)
 */
public class ExpAction extends AbstractQuestAction {
    int exp;

    public ExpAction(Quest quest, Data data) {
        super(QuestActionType.EXP, quest);
        processData(data);
    }


    @Override
    public void processData(Data data) {
        exp = DataTool.getInt(data);
    }

    @Override
    public void run(Character chr, Integer extSelection) {
        runAction(chr, exp);
    }

    public static void runAction(Character chr, int gain) {
        if (!GameConfig.getServerBoolean("use_quest_rate")) {
            chr.gainExp(NumberTool.floatToInt(gain * chr.getExpRate()), true, true);
        } else {
            chr.gainExp(NumberTool.floatToInt(gain * chr.getQuestExpRate()), true, true);
        }
    }
} 
