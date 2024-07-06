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

status = -1;
var sel;
var pickup = -1;

function start() {
    cm.sendSimple("我是阿布杜拉，我是一个负责稀有商品交易的商人中介。你有什么东西要卖给我？#b\r\n#L0#我想要出售商品。\r\n#L1#我想了解当前市场价格。\r\n#L2#商人中介？那是什么？");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        } else if (mode == 0 && sel == 0 && status == 2) {
            cm.sendNext("你现在不想卖吗？你可以之后再卖，但记住特殊物品只有一周的价值。");
            cm.dispose();
            return;
        } else if (mode == 0 && sel == 2) {
            status -= 2;
        }
    }
    if (status == 0) {
        if (sel == undefined) {
            sel = selection;
        }
        if (selection == 0) {
            var text = "Let's see what you brought...#b";
            for (var i = 0; i < 5; i++) {
                text += "\r\n#L" + i + "##t" + (3994090 + i) + "#";
            }
            cm.sendSimple("抱歉，我无法完成你的要求。");
        } else if (selection == 1) {
            var text = "";
            for (var i = 0; i < 5; i++) {
                text += "The current market price for #t" + (i + 3994090) + "# is #rNOT DONE#k mesos\r\n";
            }
            cm.sendNext("抱歉，我无法完成你的要求。");
            cm.dispose();
        } else {
            cm.sendNext("我在枫叶第七天市场购买产品，然后在其他城镇出售。我交易纪念品、香料、动物标本鲨鱼，等等……但不卖懒惰黛西的鸡蛋。");
        }
    } else if (status == 1) {
        if (sel == 0) {
            if (cm.haveItem(3994090 + selection)) {
                pickup = 3994090 + selection;
                cm.sendYesNo("当前价格是180金币。你想现在卖吗？"); //Make a price changer by hour.
            } else {
                cm.sendNext("你什么都没有。别浪费我的时间……我很忙。");
                cm.dispose();
            }
        } else {
            cm.sendNextPrev("枫之谷七日市场 周日是我的休息日。如果你需要见我，你得在周一到周五来...");
        }
    } else if (status == 2) {
        if (sel == 0) {
            cm.sendGetNumber("How many would you like to sell?", 0, 0, 200);
        } else {
            cm.sendPrev("Oh, and the prices are subject to change. I can't get the short end of the stick, I have to stay in business! Check back with me frequently, my prices change by the hour!");
        }
    } else if (status == 3) {
        if (sel == 0) {
            if (selection != 1) {
                cm.sendNext("有些不对劲。再检查一遍。");
            } else {
                cm.sendNext("交易已经完成。下次再见。");
                cm.gainMeso(180);
                cm.gainItem(pickup, -1);
            }
        }
        cm.dispose();
    }
}