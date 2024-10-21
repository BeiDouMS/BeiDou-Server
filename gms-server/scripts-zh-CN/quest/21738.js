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

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendNext("你有什么事? 虽然我并不欢迎不速之客......但你的身上却散发一种非比寻常的气息......看来我得听听你的事情了。", 1 << 3);
            break;
        case 1:
            qm.sendNextPrev("#b（讲述关于#o9300347#的事情。）", 1 << 1);
            break;
        case 2:
            qm.sendNextPrev("#o9300347#? 虽然这的确是个严峻的问题......不过到目前为止应该对#m200000000#还造不成影响。等等，你刚才说#o9300347#在哪儿?", 1 << 3);
            break;
        case 3:
            qm.sendNextPrev("在#m200060001#。", 1 << 1);
            break;
        case 4:
            qm.sendNextPrev("#m200060001#? #o9300347#居然在那里, 那么你是说有人想要入侵#m920030001#? 到底为什么呢? 是谁? ", 1 << 3);
            break;
        case 5:
            qm.sendNextPrev("#m920030001#?", 1 << 1);
            break;
        case 6:
            qm.sendNextPrev("......你到底是什么人竟然来问这样的问题? 你先稍等会儿。我要先卜一卦看你是不是值得信任。", 1 << 3);
            break;
        case 7:
            qm.sendNextPrev(".............", 1 << 3);
            break;
        case 8:
            qm.sendNextPrev(".........................", 1 << 3);
            break;
        case 9:
            qm.sendNextPrev("你, 你......不, 你......完全不同于普通人类。那悠久的岁月, 那可怕的宇宙, 然而你有着再次战胜它们的伟大命运......你到底是谁?", 1 << 3)
            break;
        case 10:
            if (!qm.isQuestStarted(21738) && !qm.isQuestCompleted(21738)) {
                qm.forceStartQuest();
            }
            qm.sendPrev("......不管是谁都好。占卦已经让我把一切都告诉你了。关于封印的庭院的一切......", 1 | 1 << 3);
            break;
        default:
            qm.dispose();
            break;
    }
}
