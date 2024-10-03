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
/*	
	Author : 		Generic
	NPC Name: 		Neinheart
	Map(s): 		Ereve: Empress' Road
	Description: 		Quest - Neinheart the Tactician
	Quest ID: 		20001
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
            if (status == 0)
                qm.sendNext("你好, #h #. 我正式欢迎你加入皇家骑士团。我叫内哈特·冯·鲁比斯汀，年轻皇后的首席战术师。从现在起我会经常和你见面，所以我建议你记住我的名字。哈哈。。。");
            else if (status == 1)
                qm.sendNextPrev("我知道你没有足够的时间弄清楚作为皇家骑士你真正需要做什么。我会一个接一个地详细地告诉你。我会解释你在哪里，年轻的皇后是谁，我们的职责是什么。。。");
            else if (status == 2)
                qm.sendNextPrev("你正站在一个叫做埃雷夫的岛上，这是唯一一个由年轻的皇后统治的陆地，也碰巧漂浮在空中。是的，我们说话的时候漂浮在空中。我们住在这里是不必要的，但为了年轻的皇后，它通常像一艘船，在枫树的世界里漂流...");
            else if (status == 3)
                qm.sendNextPrev("年轻的皇后的确是枫树世界的统治者，是这个世界唯一的统治者。什么？你从没听说过这样的事？啊，这是可以理解的。年轻的皇后也许会统治这个世界，但她不是一个笼罩在每个人面前的独裁者。她用埃雷夫作为一种方式，让她以观察员的身份监督世界，而不必太亲力亲为。不管怎样，通常都是这样...");
            else if (status == 4)
                qm.sendNextPrev("但时不时会出现她必须控制的情况。邪恶的黑魔法师在全世界都有复活的迹象。正是毁灭之王威胁要毁灭我们所知的世界，它正试图重新出现在我们的生活中.");
            else if (status == 5)
                qm.sendNextPrev("问题是，没人知道。黑魔法师失踪已经很久了，人们已经习惯了世界的和平，不一定知道如果这样的危机来临该怎么办。如果这种情况继续下去，我们的世界将很快面临严重的危险.");
            else if (status == 6)
                qm.sendNextPrev("就在那时，年轻的皇后决定在这场潜在的危机暴露出来之前挺身而出，控制住它。她决定建立一个骑士团，以防止黑魔法师被完全复活。我相信你知道自从你自愿成为骑士之后会发生什么.");
            else if (status == 7)
                qm.sendNextPrev("我们的职责很简单。我们需要让自己变得更强大；比我们现在的状态强大得多，这样当黑魔法师回来时，我们将与他战斗，并在他把整个世界置于严重危险之前彻底消灭他。这是我们的目标，我们的使命，因此也是你的");
            else if (status == 8)
                qm.sendAcceptDecline("这是对这种情况的基本概述. 明白?");
            else if (status == 9) {
                if (qm.isQuestCompleted(20001)) {
                    qm.gainExp(40);
                    qm.gainItem(1052177, 1); // fancy noblesse robe
                }
                qm.forceStartQuest();
                qm.forceCompleteQuest();
                qm.sendNext("我很高兴你明白我告诉你的，但是。。。你知道吗？根据你现在的等级，你将无法面对黑法师。见鬼，你不能面对他的徒弟的奴隶怪物的宠物的假人！你确定你准备好保护枫树世界了吗?");
            } else if (status == 10)
                qm.sendNextPrev("你可能是皇家骑士的一员，但这并不意味着你是骑士。忘了做个骑士吧。你还不是一个训练中的骑士。很多时候你会坐在这里为皇家的骑士们做文书工作，但是...");
            else if (status == 11)
                qm.sendNextPrev("但话又说回来，没有人天生强壮。皇后也更喜欢创造一个环境，在那里可以培养和创造一系列强大的骑士，而不是寻找一个超自然的天才骑士。现在，你必须在训练中成为一名骑士，让自己变得更加强大，这样你以后会变得有用。我们将讨论当皇家骑士的职责，一旦你达到这样的能力水平.");
            else if (status == 12)
                qm.sendPrev("从左边的入口一直走，然后朝前走 #b 训练林1 # . 在那里，你会找到骑士队的训练教练，基库。下次见到你，我希望你至少等级达到10级.");
        }
    }
}