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
            return qm.sendNext("刚才听到图书馆里面有什么声音传出来……是你吗，战神？封印石找到了吗？", 1 << 3);
        case 1:
            return qm.sendNextPrev("#b（讲述图书馆里发生的事情。）", 1 << 1);
        case 2:
            return qm.sendNextPrev("……那些家伙竟然在这里出现……对不起，战神。我应该好好保管的……", 1 << 3);
        case 3:
            return qm.sendNextPrev("#p2131000#，不是你的错。", 1 << 1);
        case 4:
            return qm.sendNextPrev("你还是老样子。不过……你提到封印石的事情，让我想起了一个线索。", 1 << 3);
        case 5:
            return qm.sendNextPrev("线索？", 1 << 1);
        case 6:
            return qm.sendAcceptDecline("是的，我发现了一封你过去写的信，里面有和封印石有关的线索。你想看看吗？");
        case 7:
            if (type == 12 && mode == 0) {
                return qm.dispose();
            }
            // ACCEPT
            if (!qm.isQuestStarted(21753) && !qm.isQuestCompleted(21753)) {
                qm.forceStartQuest();
            }
            return qm.sendNext("……嗯？信……", 1 | 1 << 3);
        case 8:
            return qm.sendNextPrev("#i4032327#\r\n#b（无法拿到信。信通过了手，掉到了地上。）", 1 | 1 << 1);
        case 9:
            return qm.sendNextPrev("……虽然我不太清楚时间法则……但我不能把这封信交给你的原因，应该是#b因为我们属于两个不同的时空#k……真让人伤感。不久之前我们还是同伴……", 1 | 1 << 3);
        case 10:
            return qm.sendNextPrev("……你也知道，我们妖精可以活很长时间。虽然你成为了几百年以后的人，但我应该也能活到那个时候。战神，#b我会好好保管这封信，请你在你的那个时代过来找我#k。", 1 | 1 << 3);
        case 11:
            return qm.sendNextPrev("虽然过了几百年时间，但我想你应该不会忘记这个约定。让我们以后再见吧。我会等着你的。", 1 | 1 << 3);
        case 12:
            return qm.sendPrev("#b（回到#p1201000#所存在的时间，去找找#p2131000#吧。请求#p1002104#肯定可以找到#p2131000#的。）", 1 | 1 << 1);
        default:
            return qm.dispose();
    }
}
