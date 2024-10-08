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
            return qm.sendNext("战神……战神，我就知道一定会再见到你的。因为你是个信守诺言的人。我相信你什么时候一定会来找我的，所以一直在等着你……");
        case 1:
            return qm.sendNextPrev("#b（#p2131000#幸福地笑了。）", 1 << 1);
        case 2:
            return qm.sendAcceptDecline("那时没能给你的信，终于可以交给你了。过了这么长时间，信已经很旧了……但应该还可以看。");
        case 3:
            if (type == 12 && mode == 0) { // DECLINE
                return qm.dispose();
            }
            if (!qm.isQuestStarted(21754) && !qm.isQuestCompleted(21754)) {
                if (!qm.haveItem(4032328, 1)) {
                    if (!qm.canHold(4032328, 1)) {
                        qm.sendOk("背包中的其他栏至少需要一个空位来接受任务。");
                        return qm.dispose();
                    }
                    qm.gainItem(4032328, 1);
                }
                qm.forceStartQuest();
            }
            return qm.sendNext("我虽然很想和你多说会儿话，但现在我担任转职官的功能工作，所以没有时间。你以后再来找我吧。", 1);
        default:
            return qm.dispose();
    }
}