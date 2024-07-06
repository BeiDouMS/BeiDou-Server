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

/* 
 * @Author Ronan
 * Snow Spirit
	Maplemas PQ coordinator
 */

var prizeTree = [[[2000002, 1002850], [20, 1]], [[2000006, 1012011], [20, 1]]];

var state;
var status;
var gift;
var pqType;

function start() {
    pqType = ((cm.getMapId() / 10) % 10) + 1;
    state = (cm.getMapId() % 10 > 0) ? 1 : 0;
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

        if (state > 0) {
            insidePqAction(mode, type, selection);
        } else {
            recruitPqAction(mode, type, selection);
        }
    }
}

function recruitPqAction(mode, type, selection) {
    if (status == 0) {
        em = cm.getEventManager("HolidayPQ_" + pqType);
        if (em == null) {
            cm.sendOk("假期组队任务" + pqType + "遇到了一个错误。");
            cm.dispose();
        } else if (cm.isUsingOldPqNpcStyle()) {
            action(1, 0, 0);
            return;
        }

        cm.sendSimple("#e#b<组队任务：假日>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n你和你的队伍成员一起完成任务怎么样？在这里，你会遇到障碍和问题，如果没有出色的团队合作，你是无法完成的。如果你想尝试，请告诉你的队伍的#b队长#k来找我谈谈。#b\r\n#L0#我想参加组队任务。\r\n#L1#我想" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "关闭" : "开启") + "组队搜索。\r\n#L2#我想了解更多详情。");
    } else if (status == 1) {
        if (selection == 0) {
            if (cm.getParty() == null) {
                cm.sendOk("只有当你加入一个队伍时，你才能参加派对任务。");
                cm.dispose();
            } else if (!cm.isLeader()) {
                cm.sendOk("你的队长必须与我交谈才能开始这个组队任务。");
                cm.dispose();
            } else {
                var eli = em.getEligibleParty(cm.getParty());
                if (eli.size() > 0) {
                    if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), pqType)) {
                        cm.sendOk("另一个队伍已经进入了该频道的#r组队任务#k。请尝试其他频道，或者等待当前队伍完成。");
                    }
                } else {
                    cm.sendOk("你目前无法开始这个组队任务，因为你的队伍可能不符合人数要求，有些队员可能不符合参与条件，或者他们不在这张地图上。如果你找不到队员，可以尝试使用组队搜索功能。");
                }

                cm.dispose();
            }
        } else if (selection == 1) {
            var psState = cm.getPlayer().toggleRecvPartySearchInvite();
            cm.sendOk("你的组队搜索状态现在是：#b" + (psState ? "启用" : "禁用") + "#k。想要改变状态时随时找我谈谈。");
            cm.dispose();
        } else {
            cm.sendOk("#e#b<组队任务：假日>#k#n\r\n\r\n与您的团队一起参与，建造雪人，保护快乐村免受斯克鲁奇的恶行。在里面，与您的团队一起努力保护它，收集雪之力量，这将有助于建造雪人。");
            cm.dispose();
        }
    }
}

function insidePqAction(mode, type, selection) {
    var eim = cm.getEventInstance();
    var difficulty = eim.getIntProperty("level");
    var stg = eim.getIntProperty("statusStg1");

    var mapobj = eim.getInstanceMap(889100001 + 10 * (difficulty - 1));

    if (status == 0) {
        if (stg == -1) {
            cm.sendNext("“#b#h0##k... 你终于来了。这是快乐村居民建造巨大雪人的地方。但斯克鲁奇的手下现在正在攻击它。快点！我们的任务是让你和你的队伍在限定时间内保护雪人免受斯克鲁奇手下的攻击。如果你消灭他们，他们会掉落一个叫做雪之力量的物品。收集它们并丢在雪人身上，你会看到它真的在变大。一旦它恢复到原来的大小，你的任务就完成了。只是要小心一件事。一些手下可能会掉落一个假的雪之力量。假的雪之力量实际上会让雪人比平常更快地融化。祝你好运。”");
        } else if (stg == 0) {
            if (cm.getMap().getMonsterById(9400321 + 5 * difficulty) == null) {
                cm.sendNext("请打败斯克鲁奇的手下，让雪人变大，这样斯克鲁奇就无法再躲避露面了。");
                cm.dispose();
            } else {
                cm.sendNext("太棒了！正如我所预料的，你成功击败了斯克鲁奇的手下。非常感谢你！（沉默片刻……）不幸的是，斯克鲁奇似乎并不打算就此罢手。他的手下已经告诉他发生了什么，这意味着……他很快就会出现。请继续战斗，再次祝你好运。");
            }
        } else {
            if (!eim.isEventCleared()) {
                cm.sendNext("请击败斯克鲁奇，这样我们的枫之圣诞节就能免受伤害了！");
                cm.dispose();
            } else {
                cm.sendNext("哇！！你打败了Scrooge！非常感谢你！你成功地让这个枫之谷平安度过了Maplemas！谢谢！！");
            }
        }
    } else if (status == 1) {
        const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
        const Point = Java.type('java.awt.Point');

        if (stg == -1) {
            if (!cm.isEventLeader()) {
                cm.sendOk("请让你们的队长和我联系，以获取有关任务的进一步详情。");
                cm.dispose();
                return;
            }

            mapobj.allowSummonState(true);
            var snowman = LifeFactory.getMonster(9400317 + (5 * difficulty));
            mapobj.spawnMonsterOnGroundBelow(snowman, new Point(-180, 15));
            eim.setIntProperty("snowmanLevel", 1);
            eim.dropMessage(5, "The snowman appeared on the field! Protect it using all means necessary!");

            eim.setIntProperty("statusStg1", 0);
            cm.dispose();

        } else if (stg == 0) {
            if (!cm.isEventLeader()) {
                cm.sendOk("请让你们的队长和我进一步讨论任务的细节。");
                cm.dispose();
                return;
            }

            mapobj.broadcastStringMessage(5, "As the snowman grows to it's prime, the Scrooge appears!");
            eim.getEm().getIv().invokeFunction("snowmanHeal", eim);

            var boss = LifeFactory.getMonster(9400318 + difficulty);
            mapobj.spawnMonsterOnGroundBelow(boss, new Point(-180, 15));
            eim.setProperty("spawnedBoss", "true");

            eim.setIntProperty("statusStg1", 1);
            cm.dispose();
        } else {
            gift = cm.haveItem(4032092, 1);
            if (gift) {
                var optStr = generateSelectionMenu(generatePrizeString());
                cm.sendSimple("哦，你带了一个#b#t4032092##k吗？太好了，稍等一下... 这是你的冒险岛圣诞礼物。请选择你想要收到的礼物：\r\n\r\n" + optStr);
            } else if (eim.gridCheck(cm.getPlayer()) == -1) {
                cm.sendNext("这是你的冒险岛圣诞礼物。享受吧~");
            } else {
                cm.sendOk("快乐的枫之圣诞节！");
                cm.dispose();
            }
        }

    } else if (status == 2) {
        if (gift) {
            var selItems = prizeTree[selection];
            if (cm.canHoldAll(selItems[0], selItems[1])) {
                cm.gainItem(4032092, -1);
                cm.gainItem(selItems[0][0], selItems[1][0]);

                if (selection == 1) {
                    var rnd = (Math.random() * 9) | 0;
                    cm.gainItem(selItems[0][1] + rnd, selItems[1][1]);
                } else {
                    cm.gainItem(selItems[0][1], selItems[1][1]);
                }
            } else {
                cm.sendOk("请确保在继续之前，您的装备和使用物品栏有足够的空间。");
            }
        } else {
            if (eim.giveEventReward(cm.getPlayer(), difficulty)) {
                eim.gridInsert(cm.getPlayer(), 1);
            } else {
                cm.sendOk("请确保在继续之前，您的装备、使用和杂项物品栏中有足够的空间。");
            }
        }

        cm.dispose();
    }
}

function generatePrizeString() {
    var strTree = [];

    for (var i = 0; i < prizeTree.length; i++) {
        var items = prizeTree[i][0];
        var qtys = prizeTree[i][1];

        var strSel = "";
        for (var j = 0; j < items.length; j++) {
            strSel += ("#i" + items[j] + "# #t" + items[j] + "#" + (qtys[j] > 1 ? (" : " + qtys[j]) : ""));
        }

        strTree.push(strSel);
    }

    return strTree;
}

function generateSelectionMenu(array) {
    var menu = "";
    for (var i = 0; i < array.length; i++) {
        menu += "#L" + i + "#" + array[i] + "#l\r\n";
    }
    return menu;
}