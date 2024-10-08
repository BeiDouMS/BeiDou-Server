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
            return qm.sendNext("呜呜～#p1203001#很难过。#p1203001#很生气。#p1203001#很想哭……呜呜呜呜～", 1 << 3);
        case 1:
            return qm.sendNextPrev("怎、怎么了？", 1 << 1);
        case 2:
            return qm.sendNextPrev("#p1203001#做好了宝石。#b像苹果一样的红宝石#k。结果#r小偷#k却把宝石给偷走了。#p1203001#宝石没了。#p1203001#好难过……", 1 << 3);
        case 3:
            return qm.sendNextPrev("你说小偷偷走了红宝石？", 1 << 1);
        case 4:
            return qm.sendAcceptDecline("对。必须找回#p1203001#宝石。你要是能帮我找回#p1203001#宝石，我会好好答谢你的。要是帮我抓到小偷，我也会答谢你的。");
        case 5:
            if (type == 12 && mode == 0) {
                return qm.dispose();
            }
            if (!qm.isQuestStarted(21303) && !qm.isQuestCompleted(21303)) {
                qm.forceStartQuest();
            }
            qm.sendOk("小偷往那个方向去了。那个方向是……吃饭的手是右手，不吃饭的手是左手……#b左边#k！往左边去就能抓到小偷。")
        default:
            return qm.dispose();
    }
}
