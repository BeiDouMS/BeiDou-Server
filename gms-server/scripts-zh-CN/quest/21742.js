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
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            qm.sendNext("虽说也不是什么着急的活儿，不过你这么问总让人觉得有些不爽。我是不是应该让你下次再来找我呢？反正没什么事情，就请让我清净一点行吗？", 9);
        } else if (status == 1) {
            qm.sendNextPrev("我听说你见过黑色之翼的武士......", 3);
        } else if (status == 2) {
            qm.sendNextPrev("啊，你是说一身漆黑，眉宇间皱纹很深的那个男人吗？是见过。不但见过而且他有东西放在我这里，让我转交给武公老头子。", 9);
        } else if (status == 3) {
            qm.sendNextPrev("东西？", 3);
        } else if (status == 4) {
            qm.sendNextPrev("嗯，好大一个#b画轴#k塞给我，让我一定要转交。他一脸杀气的，好像我不转交的话，他还会来找我似的。哎呦，真是吓死人了。", 9);
        } else if (status == 5) {
            qm.sendNextPrev("然后呢，画轴转交出去了吗？", 3);
        } else if (status == 6) {
            qm.sendAcceptDecline("没有，那个......其实出了点问题......你愿意听我说吗？");
        } else if (status == 7) {
            qm.sendNext("是这样的，我正在做一种新药水，当时正好在煮草药，结果没想到画轴一下子掉了进去。我虽然以最快速度把它捞了出来，不过画轴浸水后，上面的字都消失了。", 9);
        } else if (status == 8) {
            qm.sendNextPrev("于是我就发愁了，这怎么转交给武公老头子啊。于是我决定先把画轴修复好，再转交给他。正好你能帮我一个忙。画轴上面的字是武陵最有名的画师#b津津#k写的，你去的话，他一定会帮你修复画轴的。", 9);
        } else {
            if (!qm.haveItem(4220151, 1)) {
                if (!qm.canHold(4220151, 1)) {
                    qm.sendOk("Please free a room on your ETC inventory.", 9);
                    qm.dispose();
                    return;
                }

                qm.gainItem(4220151, 1);
            }

            qm.forceStartQuest();
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            qm.sendNext("怎么样？画轴修复好了吗？要不看看这上面都写了些什么？");
        } else if (status == 1) {
            qm.gainItem(4032342, -8);
            qm.gainItem(4220151, -1);
            qm.gainExp(10000);

            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}