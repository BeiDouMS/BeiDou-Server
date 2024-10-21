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
            return qm.sendYesNo("你有什么事吗？除了想成为骑士的人之外，这里禁止闲杂人等靠近。嗯……那是什么？你想把它交给女皇吗？说不定会是危险物质。来，请把它给我。我要先看一下。");
        case 1:
            if (type == 1 && mode == 0) { // NO
                return qm.dispose();
            }
            // YES
            if (qm.isQuestStarted(21757) && !qm.isQuestCompleted(21757)) {
                qm.forceCompleteQuest();
            }
            return qm.sendNext("……嗯……上面的內容非常有趣。竟然知道神兽的眼泪……啊，没什么。我们会慎重研究的。", 1 << 3);
        case 2:
            return qm.sendNextPrev("黑色之翼也许正在打这里的主意。", 1 << 1);
        case 3:
            return qm.sendPrev("即使是那样，也是#m130000000#的事情。和你这样的外人没有任何关系。你又怎么保证自己不是黑色之翼呢？……谢谢你的情报，再见。", 1 << 3);
        default:
            return qm.dispose();
    }
}