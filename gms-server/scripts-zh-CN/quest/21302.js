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

var status = -1;

function end(mode, type, selection) {
    if (mode == -1) { // END CHAT
        return qm.dispose();
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            return qm.sendNext("啊！这、这是……你终于想起制作红珠玉的方法了？啊啊……这就是为什么你再笨再健忘，我也依然对你不离不弃……哎呀，现在不是说这些的时候！快把宝石给我！")
        case 1:
            return qm.sendYesNo("好了，红珠玉的力里应该可以恢复了，你的能力也需要再唤醒一些。现在你的等级升高了不少，可以被唤醒的能力也更多了！");
        case 2:
            if (type == 1 && mode == 0) {
                return qm.dispose();
            }
            if (!qm.isQuestCompleted(21302)) {
                if (!qm.canHold(1142131)) {
                    qm.sendOk("背包中的装备栏至少需要一个空位来完成任务。");
                    return qm.dispose();
                }

                if (qm.haveItem(4032312, 1)) {
                    qm.gainItem(4032312, -1);
                }

                qm.gainItem(1142131, true);
                qm.changeJobById(2111);

                const YamlConfig = Java.type('org.gms.config.YamlConfig');
                if (YamlConfig.config.server.USE_FULL_ARAN_SKILLSET) {
                    qm.teachSkill(21110002, 0, 20, -1);   //full swing
                }
                qm.completeQuest();
            }
            qm.sendOk("赶紧恢复以前的能力吧，带上我一起去冒险……");
        default:
            return qm.dispose();
    }
}