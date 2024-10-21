/*
    This file is part of the HeavenMS MapleStory Server
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
            return qm.sendNext("……战神？……这是我的幻觉吗？战神你……真的还活着吗？啊……谢天谢地……", 1 << 3);
        case 1:
            return qm.sendNextPrev("……嗯，不好意思，我不记得你了。", 1 << 1);
        case 2:
            return qm.sendNextPrev("……嗯？你说什么，战神？你……不是战神吗？保护我们的英雄，战神……那不就是你吗？", 1 << 3);
        case 3:
            return qm.sendNextPrev("#b（详细说明醒来之后的情况。）", 1 << 1);
        case 4:
            return qm.sendYesNo("……原来如此。原来你失去了记忆，并且在几百年后的世界醒了过来。这么说来，这里对你而言应该是过去的世界……");
        case 5:
            if (type == 1 && mode == 0) { // NO
                return qm.dispose();
            }
            // YES
            return qm.sendNext("看来我 得重新向你介绍了我的名字是#p2131000#。#b战神的朋友#p2131000##k。几个月前，你在和黑魔法师的战斗中牺牲，我们则逃了出来……");
        case 6:
            return qm.sendNextPrev("在你阻挡住黑魔法师的时候，我们乘坐飞艇逃到了金银岛。但是受到了龙的攻击，没能到达南部的平原，而是迫降在了这里。");
        case 7:
            return qm.sendNextPrev("但是我们不能就此放弃……我们于是在这里定居下来。一边休养生息，一边准备建造新的村庄。");
        case 8:
            return qm.sendNextPrev("为了开垦荒无人烟的金银岛，一起来的青年们全都到村外去了。所以村里只剩下受伤的人，以及妇女和孩子。");
        case 9:
            qm.forceCompleteQuest();
            return qm.sendPrev("战神，你为什么要到这里来呢？");
        default:
            return qm.dispose();
    }
}