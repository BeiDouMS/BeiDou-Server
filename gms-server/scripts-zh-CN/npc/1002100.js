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
// Jane the Alchemist
var status = -1;
var amount = -1;
var items = [[2000002, 310], [2022003, 1060], [2022000, 1600], [2001000, 3120]];
var item;

function start() {
    if (cm.isQuestCompleted(2013)) {
        cm.sendNext("是你啊...多亏了你，我才能完成了很多事情。最近我一直在制作各种物品。如果你需要什么，告诉我一声。");
    } else {
        if (cm.isQuestCompleted(2010)) {
            cm.sendNext("你似乎不够强大，无法购买我的药水……");
        } else {
            cm.sendOk("我的梦想是到处旅行，就像你一样。然而，我的父亲不允许我这样做，因为他认为这太危险了。不过，如果我能向他证明我并不是他所认为的软弱女孩，他也许会同意...");
        }
        cm.dispose();
    }
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0 && type == 1) {
            cm.sendNext("我还有你之前给我的很多材料。物品都在那里，所以你可以慢慢挑选。");
        }
        cm.dispose();
        return;
    }
    if (status == 0) {
        var selStr = "你想购买哪些药水?#b";
        for (var i = 0; i < items.length; i++) {
            selStr += "\r\n#L" + i + "##i" + items[i][0] + "# (价格 : " + items[i][1] + " 金币)#l";
        }
        cm.sendSimple(selStr);
    } else if (status == 1) {
        item = items[selection];
        var recHpMp = ["300 HP.", "1000 HP.", "800 MP", "1000 HP and MP."];
        cm.sendGetNumber("你想买 #b#t" + item[0] + "##k? #t" + item[0] + "# 允许您恢复 " + recHpMp[selection] + " 你想买多少个?", 1, 1, 100);
    } else if (status == 2) {
        cm.sendYesNo("你将购买这些 #r" + selection + "#k #b#t" + item[0] + "#(s)#k 吗？#t" + item[0] + "# 一个需要 " + item[1] + " 冒险币，所以总共需要 #r" + (item[1] * selection) + "#k 冒险币。");
        amount = selection;
    } else if (status == 3) {
        if (cm.getMeso() < item[1] * amount) {
            cm.sendNext("你是否缺少冒险币？请检查一下你的消耗物品栏中是否有空位，并且你是否携带了至少 #r" + (item[1] * selectedItem) + "#k 冒险币。");
        } else {
            if (cm.canHold(item[0])) {
                cm.gainMeso(-item[1] * amount);
                cm.gainItem(item[0], amount);
                cm.sendNext("谢谢你的光临。这里的东西总是可以购买，如果你需要什么，请再来。");
            } else {
                cm.sendNext("请检查并查看您的消耗物品栏中是否有空的槽位可用。");
            }
        }
        cm.dispose();
    }
}