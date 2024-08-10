/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/* Dr. Rhomes
	Orbis Random/VIP Eye Color Change.
*/
var status = 0;
var beauty = 0;
var colors = Array();

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {  // disposing issue with stylishs found thanks to Vcoc
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendSimple("你好，我是Rhomes博士，是奥比斯整形外科店的美瞳部门主任。\r\n我的目标是通过美瞳的奇迹为每个人的眼睛增添个性，而且通过#b#t5152011##k或者#b#t5152014##k，我也可以为你做同样的事情！现在，你想要使用哪个？\r\n#L1#美瞳：#i5152011##t5152011##l\r\n#L2#美瞳：#i5152014##t5152014##l\r\n#L3#一次性美瞳：#i5152104#（任何颜色）#l");
        } else if (status == 1) {
            if (selection == 1) {
                beauty = 1;
                selectedRegularCoupon()
            } else if (selection == 2) {
                beauty = 2;
                selectedVipCoupon()
            } else if (selection == 3) {
                beauty = 3;
                selectedOneTimeCoupon()
            }
        } else if (status == 2) {
            cm.dispose();
            if (beauty == 1) {
                acceptedRegularCoupon()
            } else if (beauty == 2) {
                selectedVipStyle(selection)
            } else if (beauty == 3) {
                selectedOneTimeStyle(selection)
            }
        }
    }
}

function selectedRegularCoupon() {
    if (cm.getPlayer().getGender() == 0) {
        var current = cm.getPlayer().getFace() % 100 + 20000;
    }
    if (cm.getPlayer().getGender() == 1) {
        var current = cm.getPlayer().getFace() % 100 + 21000;
    }
    colors = Array();
    pushIfItemsExists(colors, [current + 100, current + 300, current + 400, current + 700]);
    cm.sendYesNo("如果你使用普通优惠券，你将获得一副随机的化妆隐形眼镜。你打算使用#b#t5152011##k，真的改变你的眼睛吗？");
}

function selectedVipCoupon() {
    if (cm.getPlayer().isMale()) {
        var current = cm.getPlayer().getFace() % 100 + 20000;
    } else {
        var current = cm.getPlayer().getFace() % 100 + 21000;
    }

    colors = Array();
    pushIfItemsExists(colors, [current + 100, current + 300, current + 400, current + 700]);
    cm.sendStyle("用我们新的计算机程序，你可以提前看到治疗后的自己。你想戴什么样的美瞳？请选择您喜欢的风格。", colors);
}

function pushIfItemsExists(array, itemidList) {
    for (var i = 0; i < itemidList.length; i++) {
        var itemid = itemidList[i];

        if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
            array.push(itemid);
        }
    }
}

function selectedOneTimeCoupon() {
    if (cm.getPlayer().isMale()) {
        var current = cm.getPlayer().getFace() % 100 + 20000;
    } else {
        var current = cm.getPlayer().getFace() % 100 + 21000;
    }

    colors = Array();
    for (var i = 0; i < 8; i++) {
        const oneTimeCouponId = 5152100 + i
        if (cm.haveItem(oneTimeCouponId)) {
            pushIfItemExists(colors, current + 100 * i);
        }
    }

    if (colors.length == 0) {
        cm.sendOk("你没有任何一次性化妆镜片可供使用。");
        cm.dispose();
        return;
    }

    cm.sendStyle("你想戴什么样的眼镜？请选择您喜欢的风格。", colors);
}

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function acceptedRegularCoupon() {
    const regularCouponItemId = 5152011
    if (cm.haveItem(regularCouponItemId)) {
        cm.gainItem(regularCouponItemId, -1);
        cm.setFace(colors[Math.floor(Math.random() * colors.length)]);
        cm.sendOk("享受你的新款和升级版的美瞳隐形眼镜吧！");
    } else {
        sendLackingCoupon()
    }
}

function selectedVipStyle(selection) {
    const vipCouponItemId = 5152014
    if (cm.haveItem(vipCouponItemId)) {
        cm.gainItem(vipCouponItemId, -1);
        const selectedFace = colors[selection]
        cm.setFace(selectedFace);
        cm.sendOk("享受你的新款和升级版的隐形眼镜吧！");
    } else {
        sendLackingCoupon()
    }
}

function selectedOneTimeStyle(selection) {
    const selectedFace = colors[selection]
    const color = Math.floor(selectedFace / 100) % 10;

    const oneTimeCouponItemId = 5152100 + color
    if (cm.haveItem(oneTimeCouponItemId)) {
        cm.gainItem(oneTimeCouponItemId, -1);
        cm.setFace(selectedFace);
        cm.sendOk("享受你的新款和升级版的隐形眼镜吧！");
    } else {
        sendLackingCoupon()
    }
}

function sendLackingCoupon() {
    cm.sendOk("对不起，但我觉得你现在没有我们的美瞳优惠券。没有优惠券，恐怕我不能为你办理。");
}