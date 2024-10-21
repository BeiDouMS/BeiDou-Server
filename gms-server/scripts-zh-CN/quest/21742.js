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
        return qm.dispose();
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            return qm.sendNext("虽说也不是什么着急的活儿，不过你这么问总让人觉得有些不爽。我是不是应该让你下次再来找我呢？反正没什么事情，就请让我清净一点行吗？", 1 << 3);
        case 1:
            return qm.sendNextPrev("我听说你见过黑色之翼的武士……", 1 << 1);
        case 2:
            return qm.sendNextPrev("啊，你是说一身漆黑，眉宇间皱纹很深的那个男人吗？是见过。不但见过而且他有东西放在我这里，让我转交给#p2091007#老头子。", 1 << 3)
        case 3:
            return qm.sendNextPrev("东西？", 1 << 1);
        case 4:
            return qm.sendNextPrev("嗯，好大一个#b#t4220151##k塞给我，让我一定要转交。他一脸杀气的，好像我不转交的话，他还会来找我似的。哎呦，真是吓死人了。", 1 << 3);
        case 5:
            return qm.sendNextPrev("然后呢，#t4220151#转交出去了吗？", 1 << 1);
        case 6:
            return qm.sendAcceptDecline("没有，那个……其实出了点问题……你愿意听我说吗？");
        case 7:
            if (type == 12 && mode == 0) { // DECLINE
                return qm.dispose();
            }
            // ACCEPT
            if (!qm.isQuestStarted(21742) && !qm.isQuestCompleted(21742)) {
                if (!qm.haveItem(4220151, 1)) {
                    if (!qm.canHold(4220151, 1)) {
                        qm.sendOk("背包中的其他栏至少需要一个空位来接受任务。");
                        return qm.dispose();
                    }
                    qm.gainItem(4220151, 1);
                }
                qm.forceStartQuest();
            }
            return qm.sendNext("是这样的，我正在做一种新药水，当时正好在煮草药，结果没想到#t4220151#一下子掉了进去。我虽然以最快速度把它捞了出来，不过#t4220151#浸水后，上面的字都消失了。");
        case 8:
            return qm.sendNextPrev("于是我就发愁了，这怎么转交给#p2091007#老头子啊。于是我决定先把#t4220151#修复好，再转交给他。正好你能帮我一个忙。#t4220151#上面的字是#m250000000#最有名的画师#b#p2091008##k写的，你去的话，他一定会帮你修复#t4220151#的。");
        default:
            return qm.dispose();
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
        return qm.dispose();
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            return qm.sendNext("怎么样？#t4220151#修复好了吗？要不看看这上面都写了些什么？");
        case 1:
            qm.gainItem(4032342, -8);
            qm.gainItem(4220151, -1);
            qm.gainExp(10000);

            qm.forceCompleteQuest();
            return qm.sendPrev("哎哎哎哎？这是什么？");
        default:
            return qm.dispose();
    }
}