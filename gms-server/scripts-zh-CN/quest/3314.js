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

function isPillUsed(ch) {
    const BuffStat = Java.type('org.gms.client.BuffStat');
    return ch.getBuffSource(BuffStat.HPREC) == 2022198;
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
            if (isPillUsed(qm.getPlayer())) {
                if (qm.canHoldAll([2050004, 2022224], [10, 20])) {
                    qm.sendNext("呼呼呼呼.... 看你面色苍白看来真的很有效果啊．这次的实验成功了！呃哈哈哈哈！果然可以用在能打倒洛伊德的坚强的人身上！......很惊讶的表情嘛？不用太担心．不是很危险的药…不，虽然是危险的药但是有解毒药…呼呼呼呼...如此一来，任意改变人体的状态会变得更为容易…这样...搞不好可以帮那家伙达成愿望...");

                    qm.gainExp(12500);
                    qm.gainItem(2050004, 10);

                    var i = Math.floor(Math.random() * 5);
                    qm.gainItem(2022224 + i, 10);

                    qm.forceCompleteQuest();
                } else {
                    qm.sendNext("你的背包满了，清理背包后再来试试。");
                }
            } else {
                qm.sendNext("你看起来很正常，不是吗？我的实验对你没有任何可能的影响。去吃我给你的药水，给我看看效果，好吗？");
            }
        } else if (status == 1) {
            qm.dispose();
        }
    }
}
