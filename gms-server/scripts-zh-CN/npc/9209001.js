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
var sel, sel2;

function start() {
    cm.sendOk("你好，冒险岛第七天市场目前不可用。");
    cm.dispose();
    return;

    cm.sendSimple("你好，枫之谷七日市场今天开张了。#b\r\n#L0#移动到枫之谷七日市场地图\r\n#L1#听解释关于枫之谷七日市场");
}

function action(mode, type, selection) {
    status++;
    if (status == 6 && mode == 1) {
        sel2 = undefined;
        status = 0;
    }
    if (mode != 1) {
        if (mode == 0 && type == 0) {
            status -= 2;
        } else {
            cm.dispose();
            return;
        }
    }
    if (status == 0) {
        if (sel == undefined) {
            sel = selection;
        }
        if (selection == 0) {
            cm.sendNext("好的，我们会把你送到冒险岛第七天市场地图。");
        } else {
            cm.sendSimple("你想了解关于冒险岛第七天市场的什么信息？#b\r\n#L0#冒险岛第七天市场在哪里举行？\r\n#L1#在冒险岛第七天市场可以做什么？\r\n#L2#我没有任何问题。");
        }
    } else if (status == 1) {
        if (sel == 0) {
            cm.getPlayer().saveLocation("EVENT");
            cm.warp(680100000 + parseInt(Math.random() * 3));
            cm.dispose();
        } else if (selection == 0) {
            cm.sendNext("枫叶七日市场只在星期天开放。你可以在任何城镇找到我，比如Henysys、新叶城、利弗雷、废都、废弃都市，我几乎无处不在！");
            status -= 2;
        } else if (selection == 1) {
            cm.sendSimple("您可以在冒险岛第七天市场找到其他地方难以找到的稀有物品。#b\r\n#L0#购买特殊物品\r\n#L1#帮助家禽农场主");
        } else {
            cm.sendNext("我猜你没有任何问题。请记得我们，如果你对任何事情感到好奇，请随时问。");
            cm.dispose();
        }
    } else if (status == 2) {
        if (sel2 == undefined) {
            sel2 = selection;
        }
        if (sel2 == 0) {
            cm.sendNext("你可以在冒险岛第七天市场找到许多物品。价格可能会有所变动，所以最好在它们便宜的时候购买！");
        } else {
            cm.sendNext("除了商人之外，你还可以在枫之谷第七天市场找到家禽场主的懒惰女儿。帮助咪咪孵化她的蛋，直到它长成一只鸡！");
        }
    } else if (status == 3) {
        if (sel2 == 0) {
            cm.sendNextPrev("在这里购买的物品可以卖给商人中间人阿卜杜拉。他不会接受超过一周的物品，所以确保你在周六之前卖掉！");
        } else {
            cm.sendNextPrev("因为她不能随便相信任何人照顾这个蛋，所以她会要求押金。支付押金并好好照顾这个蛋。");
        }
    } else if (status == 4) {
        if (sel2 == 0) {
            cm.sendNextPrev("阿卜杜拉也会调整他的转售价格，所以在能够获得最大利润时出售是明智的。价格往往每小时都会波动，所以记得经常检查。");
        } else {
            cm.sendNextPrev("如果你成功地把蛋孵化成一只小鸡并把它带回给咪咪，咪咪会给你奖励。她可能懒惰，但并不是忘恩负义的。");
        }
    } else if (status == 5) {
        if (sel2 == 0) {
            cm.sendNextPrev("在冒险岛的第七天市场以低价购买物品，当其价值上涨时将其卖给商人中介，测试你的商业头脑！");
        } else {
            cm.sendNextPrev("你可以点击蛋来查看它的成长情况。你必须勤奋照料蛋，因为你获得的经验和蛋的成长是同时进行的。");
        }
    }
}