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
//@Author Moogra, Ronan
//Fixed grammar, javascript syntax

var status = 0;
var price = 100000;

function isTransformed(ch) {
    const BuffStat = Java.type('org.gms.client.BuffStat');
    return ch.getBuffSource(BuffStat.MORPH) == 2210003;
}

function start() {
    if (!(isTransformed(cm.getPlayer()) || cm.haveItem(4001086))) {
        cm.sendOk("这是强大的霍恩尾巴龙的洞穴，他是利弗雷峡谷的至高统治者。只有那些被认为值得见他的人才能通过这里，外来者是不受欢迎的。滚开！");
        cm.dispose();
        return;
    }

    cm.sendSimple("欢迎来到生命之洞穴 - 入口！你想要进去和 #r暴君#k 战斗吗？如果你想要和他战斗，你可能需要一些 #b#v2000005##k，这样如果你被 #r暴君#k 击中了，你就可以恢复一些HP。\r\n#L1#我想花100,000金币购买10个！#l\r\n#L2#不用了，让我进去吧！#l");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else if (selection == 1) {
        if (cm.getMeso() >= price) {
            if (!cm.canHold(2000005)) {
                cm.sendOk("抱歉，你的背包里没有空位来存放这个物品！");
            } else {
                cm.gainMeso(-price);
                cm.gainItem(2000005, 10);
                cm.sendOk("谢谢购买这个药水。记得好好使用它！");
            }
        } else {
            cm.sendOk("抱歉，你没有足够的金币来购买它们！");
        }
        cm.dispose();
    } else if (selection == 2) {
        if (cm.getLevel() > 99) {
            cm.warp(240050000, 0);
        } else {
            cm.sendOk("对不起，您需要至少达到100级或以上才能进入。");
        }
        cm.dispose();
    }
}