/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    Copyleft (L) 2016 - 2019 RonanLana (HeavenMS)

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
/*
-- JavaScript -----------------
Lord Jonathan - Nautilus' Port
-- Created By --
    Cody (Cyndicate)
-- Totally Recreated by --
    Moogra
-- And Quest Script by --
    Ronan
-- Function --
No specific function, useless text.
-- GMS LIKE --
*/

var status;

var seagullProgress;
var seagullIdx = -1;
var seagullQuestion = ["One day, I went to the ocean and caught 62 Octopi for dinner. But then some kid came by and gave me 10 Octopi as a gift! How many Octopi do I have then, in total?"];
var seagullAnswer = ["72"];

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

        if (status == 0) {    // missing script for skill test found thanks to Jade™
            if (!cm.isQuestStarted(6400)) {
                cm.sendOk("你在跟我说话吗？如果你只是无聊，去烦别人吧。");
                cm.dispose();
            } else {
                seagullProgress = cm.getQuestProgressInt(6400, 1);

                if (seagullProgress == 0) {
                    seagullIdx = Math.floor(Math.random() * seagullQuestion.length);

                    // string visibility thanks to ProXAIMeRx & Glvelturall
                    cm.sendNext("好的！我现在就给你第一个问题！你最好准备好，因为这个问题很难。甚至这里的海鸥都觉得这个问题相当棘手。这是一个相当困难的问题。");
                } else if (seagullProgress == 1) {
                    cm.sendNext("现在~让我们继续下一个问题。这个问题真的很难。我要让巴特来帮我。你认识巴特，对吧？");
                } else {
                    cm.sendNext("哦！哇，这真是令人印象深刻！我认为我的考验相当困难，而你竟然通过了……你确实是海盗家族中不可或缺的一员，也是海鸥的朋友。我们现在因着持久的友谊而结为知己！最重要的是，朋友就是在你陷入困境时伸出援手的。如果你遇到紧急情况，就呼唤我们海鸥吧。");
                }
            }
        } else if (status == 1) {
            if (seagullProgress == 0) {
                cm.sendGetText(seagullQuestion[seagullIdx]);
            } else if (seagullProgress == 1) {
                cm.sendNextPrev("我要把你送到尼奥之舟的一个空房间里。你会在那里看到9个巴特。哈哈哈~他们是双胞胎吗？不，不，当然不是。我在这个意志测试中使用了一点魔法。");
            } else {
                cm.sendNextPrev("使用技能空袭通知我们，我们会前来帮助你，因为这就是朋友之间的互助。 #s5221003# #b#q5221003##k");
            }
        } else if (status == 2) {
            if (seagullIdx > -1) {
                var answer = cm.getText();
                if (answer == seagullAnswer[seagullIdx]) {
                    cm.sendNext("什么！我简直不敢相信你有多聪明！不可思议！在海鸥世界里，这种智慧会让你获得博士学位，甚至更多。你真是太了不起了……我简直不敢相信……我简直不敢相信！");
                    cm.setQuestProgress(6400, 1, 1);
                    cm.dispose();
                } else {
                    cm.sendOk("嗯，那不太符合我的记忆。再试一次吧！");
                    cm.dispose();
                }
            } else if (seagullProgress != 2) {
                cm.sendNextPrev("无论如何，9个巴特斯中只有一个是真正的巴特。你知道海盗以他们与其他海盗的友谊和同伴关系而闻名。如果你是一个真正的海盗，你应该能够轻松地找到自己的伙伴。好了，那么我会把你送到巴特所在的房间。");
            } else {
                //cm.gainExp(1000000);
                //cm.teachSkill(5221003, 0, 10, -1);
                //cm.forceCompleteQuest(6400);

                cm.sendNextPrev("你已经应对了我所有的挑战，并且成功通过了！干得好！");
                cm.dispose();
            }
        } else if (status == 3) {
            var em = cm.getEventManager("4jaerial");
            if (!em.startInstance(cm.getPlayer())) {
                cm.sendOk("另一个玩家已经在这个频道挑战测试了。请尝试其他频道，或者等待当前玩家完成。");
            }

            cm.dispose();
        }
    }
}