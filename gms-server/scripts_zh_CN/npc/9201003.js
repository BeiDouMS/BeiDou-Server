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
/**
 *9201003.js - Mom and Dad
 *@author Jvlaple
 *@author Ronan
 */
var numberOfLoves = 0;
var status = -1;
var state = 0;

function hasProofOfLoves(player) {
    var count = 0;

    for (var i = 4031367; i <= 4031372; i++) {
        if (player.haveItem(i)) {
            count++;
        }
    }

    return count >= 4;
}

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (!cm.isQuestStarted(100400)) {
                cm.sendOk("你好，我们是爸爸和妈妈...");
                cm.dispose();
            } else {
                if (cm.getQuestProgressInt(100400, 1) == 0) {
                    cm.sendNext("妈妈，爸爸，我有一个请求要向你们两个提出……我想更多地了解你们一直走过的道路，那条爱和关心我所珍爱的人的道路。");
                } else {
                    if (!hasProofOfLoves(cm.getPlayer())) {
                        cm.sendOk("亲爱的，我们需要确保你真的准备好爱上你选择的伴侣，请带来 #b4 #t4031367#'s#k。");
                        cm.dispose();
                    } else {
                        cm.sendNext("#b#h0##k，你今天让我们感到骄傲。你现在可以选择任何人作为你的未婚夫，接受我们的祝福。你现在可以咨询#p9201000#，婚礼珠宝商。祝你前程坦途，充满爱和关怀~~");
                        state = 1;
                    }
                }
            }
        } else if (status == 1) {
            if (state == 0) {
                cm.sendNextPrev("亲爱的！你真体贴，向我们求助。我们一定会帮助你的！");
            } else {
                cm.sendOk("妈妈...爸爸...非常感谢你们的温柔支持！！！");

                cm.completeQuest(100400);
                cm.gainExp(20000 * cm.getPlayer().getExpRate());
                for (var i = 4031367; i <= 4031372; i++) {
                    cm.removeAll(i);
                }

                cm.dispose();
            }
        } else if (status == 2) {
            cm.sendNextPrev("当然，你一定已经在冒险岛世界中见过爱之仙子#rNanas#k了。从她们那里收集#b4个#t4031367#，然后带到这里来。这次旅程将解答你对爱情的一些疑问……");
        } else if (status == 3) {
            cm.setQuestProgress(100400, 1, 1);
            cm.dispose();
        }
    }
}