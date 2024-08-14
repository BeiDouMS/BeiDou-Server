/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
 * @author: Ronan
 * @npc: Vending Machine
 * @map: 193000000 - Premium Road - Kerning City Internet Cafe
 * @func: Cafe PQ Rewarder
 */

var status;

var itemSet_lv6 = [1442046, 1432018, 1102146, 1102145, 2022094, 2022544, 2022123, 2022310, 2040727, 2041058, 2040817, 4000030, 4003005, 4003000, 4011007, 4021009, 4011008, 3010098];
var itemQty_lv6 = [1, 1, 1, 1, 35, 15, 20, 20, 1, 1, 1, 30, 30, 30, 1, 1, 3, 1];

var itemSet_lv5 = [1382015, 1382016, 1442044, 1382035, 2022310, 2022068, 2022069, 2022190, 2022047, 2040727, 2040924, 2040501, 4000030, 4003005, 4003000, 4011003, 4011006, 4021004, 3010099];
var itemQty_lv5 = [1, 1, 1, 1, 20, 40, 40, 30, 30, 1, 1, 1, 20, 20, 25, 3, 2, 3, 1];

var itemSet_lv4 = [1332029, 1472027, 1462032, 1492019, 2022045, 2022048, 2022094, 2022123, 2022058, 2041304, 2041019, 2040826, 2040758, 4000030, 4003005, 4003000, 4010007, 4011003, 4021003, 3010016, 3010017];
var itemQty_lv4 = [1, 1, 1, 1, 45, 40, 25, 20, 60, 1, 1, 1, 1, 10, 10, 20, 5, 1, 1, 1, 1];

var itemSet_lv3 = [1302058, 1372008, 1422030, 1422031, 1022082, 2022279, 2022120, 2001001, 2001002, 2022071, 2022189, 2040914, 2041001, 2041041, 2041308, 4031203, 4000030, 4003005, 4003000, 4010004, 4010006, 4020000, 4020006, 3010002, 3010003];
var itemQty_lv3 = [1, 1, 1, 1, 1, 65, 40, 40, 40, 25, 25, 1, 1, 1, 1, 10, 7, 10, 8, 5, 5, 5, 5, 1, 1];

var itemSet_lv2 = [1022073, 1012098, 1012101, 1012102, 1012103, 2022055, 2022056, 2022103, 2020029, 2020032, 2020031, 2022191, 2022016, 2043300, 2043110, 2043800, 2041001, 2040903, 4031203, 4000021, 4003005, 4003000, 4003001, 4010000, 4010001, 4010003, 4010004, 4020004, 3010004, 3010005];
var itemQty_lv2 = [1, 1, 1, 1, 1, 40, 40, 40, 40, 60, 60, 60, 60, 1, 1, 1, 1, 1, 4, 6, 7, 5, 2, 4, 4, 3, 3, 4, 1, 1];

var itemSet_lv1 = [1302021, 1302024, 1302033, 1082150, 1002419, 2022053, 2022054, 2020032, 2022057, 2022096, 2022097, 2022192, 2020030, 2010005, 2022041, 2030000, 2040100, 2040004, 2040207, 2048004, 4031203, 4000021, 4003005, 4003000, 4003001, 4010000, 4010001, 4010002, 4010005, 4020004];
var itemQty_lv1 = [1, 1, 1, 1, 1, 20, 20, 20, 20, 20, 25, 25, 25, 50, 50, 12, 1, 1, 1, 1, 3, 4, 2, 2, 1, 2, 2, 2, 2, 2];

var levels = ["第1层", "第2层", "第3层", "第4层", "第5层", "第6层"];

var tickets = [0, 0, 0, 0, 0, 0];
var coinId = 4001158;
var coins = 0;

var hasCoin = false;
var currentTier;
var curItemQty;
var curItemSel;
var advance = true;

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
        if (mode == 1 && advance) {
            status++;
        } else {
            status--;
        }

        advance = true;

        if (status == 0) {
            hasCoin = cm.haveItem(coinId);
            cm.sendNext("这是互联网咖啡厅的自动贩卖机。放置你在任务中获得的橡皮擦或#t" + coinId + "#来兑换奖品。你可以放置#b任意数量的橡皮擦#k，但请注意放置#r不同的橡皮擦#k和#r更多数量的任何一种#k会提高奖励的可能性！");
        } else if (status == 1) {
            var sendStr;
            currentTier = getRewardTier();

            if (currentTier >= 0) {
                sendStr = "使用您当前放置的物品，您可以检索#r" + levels[currentTier] + "#k 奖品。放置橡皮擦:";
            } else {
                sendStr = "您还没有放置橡皮擦。放置橡皮擦:";
            }

            var listStr = "";
            for (var i = 0; i < tickets.length; i++) {
                listStr += "#b#L" + i + "##t" + (4001009 + i) + "##k";
                if (tickets[i] > 0) {
                    listStr += " - " + tickets[i] + " 橡皮擦";
                }
                listStr += "#l\r\n";
            }
            if (hasCoin) {
                listStr += "#b#L" + tickets.length + "##t" + coinId + "##k";
                if (coins > 0) {
                    listStr += " - " + coins + " 羽毛";
                }
                listStr += "#l\r\n";
            }

            cm.sendSimple(sendStr + "\r\n\r\n" + listStr + "#r#L" + getRewardIndex(hasCoin) + "#找回奖品！#l#k\r\n");

        } else if (status == 2) {
            if (selection == getRewardIndex(hasCoin)) {
                if (currentTier < 0) {
                    cm.sendPrev("您没有提交橡皮擦。提交至少一个橡皮擦即可领取奖品。");
                    advance = false;
                } else {
                    givePrize();
                    cm.dispose();
                }
            } else {
                var tickSel;
                if (selection < tickets.length) {
                    tickSel = 4001009 + selection;
                } else {
                    tickSel = coinId;
                }

                curItemQty = cm.getItemQuantity(tickSel);
                curItemSel = selection;

                if (curItemQty > 0) {
                    cm.sendGetText("要将提交多少个 #b#t" + tickSel + "##k 提交到机器上? (剩余#r" + curItemQty + "#k 可用)#k");
                } else {
                    cm.sendPrev(" #r你没有可用的 #k #b#t" + tickSel + "##k 提交到机器上。 点击 '#r上项#k' 返回主界面。");
                    advance = false;
                }
            }
        } else if (status == 3) {
            var text = cm.getText();

            try {
                var placedQty = parseInt(text);
                if (isNaN(placedQty) || placedQty < 0) {
                    throw true;
                }

                if (placedQty > curItemQty) {
                    cm.sendPrev("你没有足够的橡皮可提交 (剩余#r" + curItemQty + "#k 可用). 点击 '#r上项#k' 返回主界面。");
                    advance = false;
                } else {
                    if (curItemSel < tickets.length) {
                        tickets[curItemSel] = placedQty;
                    } else {
                        coins = placedQty;
                    }

                    cm.sendPrev("操作成功。 点击 '#r上项#k' 返回主界面 #r找回奖品！#k");
                    advance = false;
                }
            } catch (err) {
                cm.sendPrev("你必须提交正确橡皮擦数量。 点击 '#r上项#k' 返回主界面。");
                advance = false;
            }

            status = 2;
        } else {
            cm.dispose();
        }
    }
}

function getRewardIndex(hasCoin) {
    return (!hasCoin) ? tickets.length : tickets.length + 1;
}

function getRewardTier() {
    var points = getPoints();

    if (points <= 6) {
        if (points <= 0) {
            return -1;
        } else {
            return 0;
        }
    }
    if (points >= 46) {
        return 5;
    }

    return Math.floor((points - 6) / 8);
}

function getPoints() {
    var points = 0;

    for (var i = 0; i < tickets.length; i++) {
        if (tickets[i] <= 0) {
            continue;
        }

        points += (6 + ((tickets[i] - 1) * getTicketMultiplier(i)));    //6 from uniques + rest from each ticket difficulty
    }
    points += Math.ceil(0.46 * coins);  // 100 coins for a LV6 tier item.

    return points;
}

function getTicketMultiplier(ticket) {
    if (ticket == 1 || ticket == 3) {
        return 3;
    } else {
        return 1;
    }
}

function givePrize() {
    var lvTarget, lvQty;

    if (currentTier == 0) {
        lvTarget = itemSet_lv1;
        lvQty = itemQty_lv1;
    } else if (currentTier == 1) {
        lvTarget = itemSet_lv2;
        lvQty = itemQty_lv2;
    } else if (currentTier == 2) {
        lvTarget = itemSet_lv3;
        lvQty = itemQty_lv3;
    } else if (currentTier == 3) {
        lvTarget = itemSet_lv4;
        lvQty = itemQty_lv4;
    } else if (currentTier == 4) {
        lvTarget = itemSet_lv5;
        lvQty = itemQty_lv5;
    } else {
        lvTarget = itemSet_lv6;
        lvQty = itemQty_lv6;
    }

    if (!hasRewardSlot(lvTarget, lvQty)) {
        cm.sendOk("检查一下你的背包是否有空间，然后再领取奖品。");
    } else {
        var rnd = Math.floor(Math.random() * lvTarget.length);

        for (var i = 0; i < tickets.length; i++) {
            if(tickets[i]&&tickets[i]>0){
                cm.gainItem(4001009 + i, -1 * tickets[i]);
            }
        }
        if(hasCoin){
            cm.gainItem(coinId, -1 * coins);
        }


        cm.gainItem(lvTarget[rnd], lvQty[rnd]);
    }
}

function hasRewardSlot(lvTarget, lvQty) {
    for (var i = 0; i < lvTarget.length; i++) {
        if (!cm.canHold(lvTarget[i], lvQty[i])) {
            return false;
        }
    }

    return true;
}
