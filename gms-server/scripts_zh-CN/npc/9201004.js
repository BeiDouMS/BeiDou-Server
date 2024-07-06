/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2017 RonanLana

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
/* Amos the Wise
	Amoria (680000000)
	Wedding info.
 */

var status;

var rings = [1112806, 1112803, 1112807, 1112809];
var divorceFee = 500000;
var ringObj;

function getWeddingRingItemId(player) {
    for (var i = 0; i < rings.length; i++) {
        if (player.haveItemWithId(rings[i], false)) {
            return rings[i];
        }
    }

    return null;
}

function hasEquippedWeddingRing(player) {
    for (var i = 0; i < rings.length; i++) {
        if (player.haveItemEquipped(rings[i])) {
            return true;
        }
    }

    return false;
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
            var questionStr = ["How can I engage someone?", "How can I marry?", "How can I divorce?"]

            if (!(!cm.getPlayer().isMarried() && getWeddingRingItemId(cm.getPlayer()))) {
                questionStr.push("I want a divorce...");
            } else {
                questionStr.push("I wanna remove my old wedding ring...");
            }

            cm.sendSimple("你好，欢迎来到#b阿莫利亚#k，这是一个美丽的地方，枫叶冒险家可以在这里找到爱情，甚至如果足够激动人心，还可以结婚。你对阿莫利亚有任何问题吗？跟我说说吧。#b\r\n\r\n" + "生成选择菜单("+questionStr+")");
        } else if (status == 1) {
            switch (selection) {
                case 0:
                    cm.sendOk("订婚流程非常简单。从#bring maker, #p9201000#那里接受一个预请求，然后在整个冒险岛世界中收集#b#t4031367#。完成后，你就可以制作一枚订婚戒指。拿着戒指向你喜欢的人表白，希望对方也有同样的感觉。");
                    cm.dispose();
                    break;

                case 1:
                    cm.sendOk("在#b结婚流程#k中，你必须已经订婚。恋人必须选择他们想举行婚礼的场所。阿莫利亚提供两个选择：#r大教堂#k和#r小教堂#k。\r\n然后，其中一位伴侣必须购买一张#b婚礼门票#k，可以通过现金商店购买，并与婚礼助手预订他们的仪式。每位伴侣将收到#r客人门票#k，可以分发给他们的熟人。");
                    cm.dispose();
                    break;

                case 2:
                    cm.sendOk("很遗憾，长久的爱有一天可能会消退。嗯，我希望对于任何一对曾经结婚、正在结婚或者将要结婚的恋人来说，情况并非如此。但是，如果真的发生了，我愿意以#r" + divorceFee + "#k金币的费用来提供安全的离婚服务。");
                    cm.dispose();
                    break;

                case 3:
                    ringObj = cm.getPlayer().getMarriageRing();
                    if (ringObj == null) {
                        var itemid = getWeddingRingItemId(cm.getPlayer());

                        if (itemid != null) {
                            cm.sendOk("好了，我已经把你的旧结婚戒指取下来了。");
                            cm.gainItem(itemid, -1);
                        } else if (hasEquippedWeddingRing(cm.getPlayer())) {
                            cm.sendOk("如果你想要移除你的旧婚戒，请在和我对话之前先将它卸下。");
                        } else {
                            cm.sendOk("你不需要离婚才能要求离婚。");
                        }

                        cm.dispose();
                        return;
                    }

                    cm.sendYesNo("所以，你想和你的伴侣离婚？请确保，这个过程无论如何都无法回滚，这应该是一个最后通牒，你的戒指将被摧毁作为后果。也就是说，你真的想离婚吗？");
                    break;
            }
        } else if (status == 2) {
            if (cm.getMeso() < divorceFee) {
                cm.sendOk("你没有足够的#r" + divorceFee + "金币#k来支付离婚费用。");
                cm.dispose();
                return;
            } else if (ringObj.equipped()) {
                cm.sendOk("请在尝试离婚之前卸下你的戒指。");
                cm.dispose();
                return;
            }

            cm.gainMeso(-divorceFee);
            const RingActionHandler = Java.type('org.gms.net.server.channel.handlers.RingActionHandler');
            RingActionHandler.breakMarriageRing(cm.getPlayer(), ringObj.getItemId());
            cm.gainItem(ringObj.getItemId(), -1);

            cm.sendOk("你已经和你的伴侣离婚。");
            cm.dispose();
        }
    }
}

function generateSelectionMenu(array) {
    var menu = "";
    for (var i = 0; i < array.length; i++) {
        menu += "#L" + i + "#" + array[i] + "#l\r\n";
    }
    return menu;
}