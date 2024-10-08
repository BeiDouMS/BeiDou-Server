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
            return qm.sendAcceptDecline("这段时间你升级很快嘛，英雄大人？我终于又发现了和黑色之 翼有关的有趣事情了。这一次咱们早点……#b#m250000000##k这个村子你知道吗？看来你得去一趟那里。");
        case 1:
            if (type == 12 && mode == 0) { // DECLINE
                return qm.dispose();
            }
            // ACCCEPT
            if (!qm.isQuestStarted(21741) && !qm.isQuestCompleted(21741)) {
                qm.forceStartQuest();
            }
            return qm.sendOk("#m250000000#的一个叫#b#p2090004##k的人似乎在和黑色之翼接触。虽然不知道他们在密谋什么，不过这个情报是很可靠的。你去调查一下黑色之翼为什么要和#p2090004#接触，他们之间有什么交易。")
        default:
            return qm.dispose();
    }
}
