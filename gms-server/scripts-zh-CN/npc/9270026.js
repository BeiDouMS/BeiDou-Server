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
/* 	Sixx
	Singa REG/VIP Eye Color Changer
*/

var status = 0;
var beauty = 0;
var colors = Array();

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function pushIfItemsExists(array, itemidList) {
    for (var i = 0; i < itemidList.length; i++) {
        var itemid = itemidList[i];

        if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
            array.push(itemid);
        }
    }
}

function start() {
    cm.sendSimple("嗨，你好！我是Sixx，在CBD的大眼睛镜片店负责这里的事务！使用#b#t5152039##k或#b#t5152040##k，你可以让我们来处理剩下的事情，拥有你一直渴望的美丽外观！记住，每个人注意到的第一件事就是眼睛，我们可以帮助你找到最适合你的美瞳！那么，你想要使用什么呢？\r\n#L1#美瞳：#i5152039##t5152039##l\r\n#L2#美瞳：#i5152040##t5152040##l\r\n#L3#一次性美瞳：#i5152107#（任何颜色）#l");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            if (selection == 1) {
                beauty = 1;
                var current = cm.getPlayer().getFace() % 100 + 20000 + cm.getPlayer().getGender() * 1000;
                cm.sendYesNo("如果你使用普通优惠券，你将获得一副随机的化妆隐形眼镜。你打算使用#b#t5152039##k，真的改变你的眼睛吗？");
            } else if (selection == 2) {
                beauty = 2;
                var current = cm.getPlayer().getFace() % 100 + 20000 + cm.getPlayer().getGender() * 1000;
                pushIfItemsExists(colors, [current + 200, current + 300, current + 400, current + 700]);
                cm.sendStyle("用我们的专业机器，您可以提前看到治疗后的自己，您想戴什么样的镜片呢？选择自己喜欢的款式吧。", colors);
            } else if (selection == 3) {
                beauty = 3;
                if (cm.getPlayer().getGender() == 0) {
                    var current = cm.getPlayer().getFace()
                        % 100 + 20000;
                }
                if (cm.getPlayer().getGender() == 1) {
                    var current = cm.getPlayer().getFace()
                        % 100 + 21000;
                }

                colors = Array();
                for (var i = 0; i < 8; i++) {
                    if (cm.haveItem(5152100 + i)) {
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
        } else if (status == 2) {
            if (beauty == 1) {
                if (cm.haveItem(5152039)) {
                    cm.gainItem(5152039, -1);
                    cm.setFace(Math.floor(Math.random() * 8) * 100 + current);
                    cm.sendOk("享受你的新款和升级版的美瞳隐形眼镜吧！");
                } else {
                    cm.sendOk("对不起，但我不认为你现在带着我们的化妆镜片优惠券。没有优惠券，恐怕我不能为你做这件事。");
                }
            } else if (beauty == 2) {
                if (cm.haveItem(5152040)) {
                    cm.gainItem(5152040, -1);
                    cm.setFace(colors[selection]);
                    cm.sendOk("享受你的新款和升级版的美瞳隐形眼镜吧！");
                } else {
                    cm.sendOk("对不起，但我觉得你现在没有我们的化妆镜片优惠券。没有优惠券，恐怕我不能为你做。");
                }
            } else if (beauty == 3) {
                var color = (colors[selection] / 100) % 10 | 0;

                if (cm.haveItem(5152100 + color)) {
                    cm.gainItem(5152100 + color, -1);
                    cm.setFace(colors[selection]);
                    cm.sendOk("享受你的新款和升级版的隐形眼镜吧！");
                } else {
                    cm.sendOk("对不起，但我觉得你现在没有我们的化妆镜片优惠券。没有优惠券，恐怕我不能为你做这件事。");
                }
            }
            cm.dispose();
        }
    }
}