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
 -- Odin JavaScript --------------------------------------------------------------------------------
 Arwen the Fairy - Victoria Road : Ellinia (101000000)
 -- By ---------------------------------------------------------------------------------------------
 Xterminator
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Xterminator
 ---------------------------------------------------------------------------------------------------
 **/

var status = 0;
var item;
var selected;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            cm.dispose();
            return;
        } else if (status == 2 && mode == 0) {
            cm.sendNext("制作" + item + "并不容易。请准备好材料。");
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.getLevel() >= 40) {
                cm.sendNext("是的...我是精灵们的大师炼金术士。但是精灵们不应该长时间与人类接触...像你这样强大的人应该没问题。如果你给我带来材料，我会为你制作一件特别的物品。");
            } else {
                cm.sendOk("我可以制作稀有、有价值的物品，但很遗憾我不能为像你这样的陌生人制作。");
                cm.dispose();
            }
        } else if (status == 1) {
            cm.sendSimple("你想要制作什么？#b\r\n#L0##i4011007##t4011007##l\r\n#L1##i4021009##t4021009##l\r\n#L2##i4031042##t4031042##l");
        } else if (status == 2) {
            selected = selection;
            if (selection == 0) {
                item = "Moon Rock";
                cm.sendYesNo("所以你想制作一个#i4011007##t4011007#？要做到这一点，你需要提炼每一种材料：\r\n#b#i4011000##t4011000##k，#b#i4011001##t4011001##k，#b#i4011002##t4011002##k，#b#i4011003##t4011003##k，#b#i4011004##t4011004##k，#b#i4011005##t4011005##k 和 #b#i4011006##t4011006##k。\r\n再加上10,000金币，我就可以为你制作了。");
            } else if (selection == 1) {
                item = "Star Rock";
                cm.sendYesNo("所以你想制作一颗#i4021009##t4021009#？为了做到这一点，你需要提炼出每一种宝石：\r\n#b#i4021000##t4021000##k，#b#i4021001##t4021001##k，#b#i4021002##t4021002##k，#b#i4021003##t4021003##k，#b#i4021004##t4021004##k，#b#i4021005##t4021005##k，#b#i4021006##t4021006##k，#b#i4021007##t4021007##k 和 #b#i4021008##t4021008##k。\r\n再加上15,000金币，我就可以为你制作了。");
            } else if (selection == 2) {
                item = "Black Feather";
                cm.sendYesNo("所以你想制作一根#i4031042##t4031042#？为了做到这一点，你需要：\r\n #b1 #b#i4001006##t4001006##k，#b1 #i4011007##t4011007##k 和 #b1 #i4021008##t4021008##k。\r\n再加上30,000金币，我就会为你制作。哦对了，这块羽毛是非常特别的物品，所以如果你不小心掉了，它会消失，而且你也不能把它送给别人。");
            }
        } else if (status == 3) {
            if (selected == 0) {
                if (cm.haveItem(4011000) && cm.haveItem(4011001) && cm.haveItem(4011002) && cm.haveItem(4011003) && cm.haveItem(4011004) && cm.haveItem(4011005) && cm.haveItem(4011006) && cm.getMeso() >= 10000) {
                    cm.gainMeso(-10000);
                    for (var i = 4011000; i < 4011007; i++) {
                        cm.gainItem(i, -1);
                    }
                    cm.gainItem(4011007, 1);
                    cm.sendNext("好的，拿着这个" + item + "。它做得很好，可能是因为我用了好材料。如果你需要我的帮助，随时可以回来找我。");
                } else {
                    cm.sendNext("你确定你有足够的金币吗？请检查一下，看看你是否有精炼的\r\n#b#i4011000##t4011000##k、#b#i4011001##t4011001##k、#b#i4011002##t4011002##k、#b#i4011003##t4011003##k、#b#i4011004##t4011004##k、#b#i4011005##t4011005##k 和 #b#i4011006##t4011006##k，\r\n每种一个。");
                }
            } else if (selected == 1) {
                if (cm.haveItem(4021000) && cm.haveItem(4021001) && cm.haveItem(4021002) && cm.haveItem(4021003) && cm.haveItem(4021004) && cm.haveItem(4021005) && cm.haveItem(4021006) && cm.haveItem(4021007) && cm.haveItem(4021008) && cm.getMeso() >= 15000) {
                    cm.gainMeso(-15000);
                    for (var j = 4021000; j < 4021009; j++) {
                        cm.gainItem(j, -1);
                    }
                    cm.gainItem(4021009, 1);
                    cm.sendNext("好的，拿着这个" + item + "。它做得很好，可能是因为我用了好材料。如果你需要我的帮助，随时可以回来找我。");
                } else {
                    cm.sendNext("你确定你有足够的金币吗？请检查一下，看看你是否有精炼的 #b#i4021000##t4021000##k、#b#i4021001##t4021001##k、#b#i4021002##t4021002##k、#b#i4021003##t4021003##k、#b#i4021004##t4021004##k、#b#i4021005##t4021005##k、#b#i4021006##t4021006##k、#b#i4021007##t4021007##k 和 #b#i4021008##t4021008##k，每种一个。");
                }
            } else if (selected == 2) {
                if (cm.haveItem(4001006) && cm.haveItem(4011007) && cm.haveItem(4021008) && cm.getMeso() >= 30000) {
                    cm.gainMeso(-30000);
                    for (var k = 4001006; k < 4021009; k += 10001) {
                        cm.gainItem(k, -1);
                    }
                    cm.gainItem(4031042, 1);
                    cm.sendNext("好的，拿着这个" + item + "。它做得很好，可能是因为我用了好材料。如果你日后需要我的帮助，随时可以回来。");
                } else {
                    cm.sendNext("你确定你有足够的金币吗？请检查一下，看看你是否有精炼的 #b1 #b#i4001006##t4001006##k，#b1 #i4011007##t4011007##k 和 #b1 #i4021008##t4021008##k，\r\n每种一个。");
                }
            }
            cm.dispose();
        }
    }
}