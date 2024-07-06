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
            cm.sendNext("制作" + 物品 + "并不容易。请准备好材料。");
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
            cm.sendSimple("你想要制作什么？#b\r\n#L0#月岩#l\r\n#L1#星岩#l\r\n#L2#黑色羽毛#l");
        } else if (status == 2) {
            selected = selection;
            if (selection == 0) {
                item = "Moon Rock";
                cm.sendYesNo("所以你想制作一个月岩？要做到这一点，你需要提炼每一种材料：#b青铜板#k，#b钢板#k，#b秘银板#k，#b精钢板#k，#b银板#k，#b奥利哈刚板#k和#b金板#k。再加上10,000金币，我就可以为你制作了。");
            } else if (selection == 1) {
                item = "Star Rock";
                cm.sendYesNo("所以你想制作一颗星之岩？为了做到这一点，你需要提炼出每一种宝石：#b石榴石#k，#b紫水晶#k，#b海蓝宝石#k，#b翡翠#k，#b蛋白石#k，#b蓝宝石#k，#b黄玉#k，#b钻石#k和#b黑水晶#k。再加上15,000金币，我就可以为你制作了。");
            } else if (selection == 2) {
                item = "Black Feather";
                cm.sendYesNo("所以你想制作一根黑羽毛？为了做到这一点，你需要 #b1 火焰羽毛#k，#b1 月岩#k 和 #b1 黑水晶#k。再加上30,000金币，我就会为你制作。哦对了，这块羽毛是非常特别的物品，所以如果你不小心掉了，它会消失，而且你也不能把它送给别人。");
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
                    cm.sendNext("你确定你有足够的金币吗？请检查一下，看看你是否有精炼的#b青铜板#k、#b钢板#k、#b秘银板#k、#b精钢板#k、#b银板#k、#b奥利哈钢板#k和#b黄金板#k，每种一个。");
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
                    cm.sendNext("你确定你有足够的金币吗？请检查一下，看看你是否有精炼的 #b石榴石#k、#b紫水晶#k、#b海蓝宝石#k、#b翡翠#k、#b蛋白石#k、#b蓝宝石#k、#b黄玉#k、#b钻石#k 和 #b黑水晶#k，每种一个。");
                }
            } else if (selected == 2) {
                if (cm.haveItem(4001006) && cm.haveItem(4011007) && cm.haveItem(4021008) && cm.getMeso() >= 30000) {
                    cm.gainMeso(-30000);
                    for (var k = 4001006; k < 4021009; k += 10001) {
                        cm.gainItem(k, -1);
                    }
                    cm.gainItem(4031042, 1);
                    cm.sendNext("好的，拿着这个" + 物品 + "。它做得很好，可能是因为我用了好材料。如果你日后需要我的帮助，随时可以回来。");
                } else {
                    cm.sendNext("你确定你有足够的金币吗？请检查一下，看看你是否有精炼的 #b石榴石#k、#b紫水晶#k、#b海蓝宝石#k、#b翡翠#k、#b蛋白石#k、#b蓝宝石#k、#b黄玉#k、#b钻石#k 和 #b黑水晶#k，每种一个。");
                }
            }
            cm.dispose();
        }
    }
}