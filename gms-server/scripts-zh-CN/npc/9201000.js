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
/* Moony
	Amoria (680000000)
	Engagement ring NPC.
 */

var status;
var state;

var item;
var mats;
var matQty;
var cost;

var options;

function hasEngagementBox(player) {
    for (var i = 2240000; i <= 2240003; i++) {
        if (player.haveItem(i)) {
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
            options = ["我想做个戒指。", "我想扔掉我的戒指盒。"];
            cm.sendSimple("我是#p9201000#，订婚戒指制造商。我能为您做些什么？\r\n\r\n#b" + generateSelectionMenu(options));
        } else if (status == 1) {
            if (selection == 0) {
                if (!cm.isQuestCompleted(100400)) {
                    if (!cm.isQuestStarted(100400)) {
                        state = 0;
                        cm.sendNext("所以你想要制作订婚戒指，是吗？好的，当你从#b#p9201003##k那里得到#rblessings#k后，我可以提供一个。");
                    } else {
                        cm.sendOk("在尝试制作订婚戒指之前，先从#b#p9201003#k那里得到祝福。他们一定在你家等着你，就在#r射手村狩猎场#k的那边。");
                        cm.dispose();
                    }
                } else {
                    if (hasEngagementBox(cm.getPlayer())) {
                        cm.sendOk("抱歉，您已经有一个戒指盒了。我一次不能为您提供多个盒子。");
                        cm.dispose();
                        return;
                    }
                    if (cm.getPlayer().getGender() != 0) {
                        cm.sendOk("抱歉，戒指盒目前只适用于男性。");
                        cm.dispose();
                        return;
                    }

                    state = 1;
                    options = ["月光石", "星星宝石", "金心", "银天鹅"];
                    var selStr = "那么，你想让我制作什么样的订婚戒指？\r\n\r\n#b" + generateSelectionMenu(options);
                    cm.sendSimple(selStr);
                }
            } else {
                if (hasEngagementBox(cm.getPlayer())) {
                    for (var i = 2240000; i <= 2240003; i++) {
                        cm.removeAll(i);
                    }

                    cm.sendOk("你的戒指盒已被丢弃。");
                } else {
                    cm.sendOk("你没有戒指盒可以丢弃。");
                }

                cm.dispose();
            }
        } else if (status == 2) {
            if (state == 0) {
                cm.sendOk("他们住在哪里，你问？哦，这可追溯很久了……你知道，我是他们的朋友，我是那个制作并亲自送交他们订婚戒指的人。他们住在#r林中之城狩猎场#k的后面，我相信你知道那是哪里。");
                cm.startQuest(100400);
                cm.dispose();
            } else {
                var itemSet = [2240000, 2240001, 2240002, 2240003];
                var matSet = [[4011007, 4021007], [4021009, 4021007], [4011006, 4021007], [4011004, 4021007]];
                var matQtySet = [[1, 1], [1, 1], [1, 1], [1, 1]];
                var costSet = [30000, 20000, 10000, 5000];

                item = itemSet[selection];
                mats = matSet[selection];
                matQty = matQtySet[selection];
                cost = costSet[selection];

                var prompt = "然后我会给你做一个 #b#t" + item + "##k, 那样对吗?";
                prompt += " 在这种情况下，我需要你提供特定的物品才能完成。不过，请确保你的库存中有足够的空间！#b";

                if (mats instanceof Array) {
                    for (var i = 0; i < mats.length; i++) {
                        prompt += "\r\n#i" + mats[i] + "# " + matQty[i] + " #t" + mats[i] + "#";
                    }
                } else {
                    prompt += "\r\n#i" + mats + "# " + matQty + " #t" + mats + "#";
                }

                if (cost > 0) {
                    prompt += "\r\n#i4031138# " + cost + " meso";
                }

                cm.sendYesNo(prompt);
            }
        } else if (status == 3) {
            var complete = true;
            var recvItem = item, recvQty = 1, qty = 1;

            if (!cm.canHold(recvItem, recvQty)) {
                cm.sendOk("首先检查你的物品栏是否有空位。");
                cm.dispose();
                return;
            } else if (cm.getMeso() < cost * qty) {
                cm.sendOk("对不起，我的服务是需要收费的。在尝试锻造戒指之前，请在这里给我带来正确数量的黄金。");
                cm.dispose();
                return;
            } else {
                if (mats instanceof Array) {
                    for (var i = 0; complete && i < mats.length; i++) {
                        if (!cm.haveItem(mats[i], matQty[i] * qty)) {
                            complete = false;
                        }
                    }
                } else if (!cm.haveItem(mats, matQty * qty)) {
                    complete = false;
                }
            }

            if (!complete) {
                cm.sendOk("“嗯，看来你缺少订婚戒指的一些材料。请先提供这些材料，好吗？”");
            } else {
                if (mats instanceof Array) {
                    for (var i = 0; i < mats.length; i++) {
                        cm.gainItem(mats[i], -matQty[i] * qty);
                    }
                } else {
                    cm.gainItem(mats, -matQty * qty);
                }

                if (cost > 0) {
                    cm.gainMeso(-cost * qty);
                }

                cm.gainItem(recvItem, recvQty);
                cm.sendOk("一切都搞定了，订婚戒指做得非常完美。祝你们订婚快乐。");
            }
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
