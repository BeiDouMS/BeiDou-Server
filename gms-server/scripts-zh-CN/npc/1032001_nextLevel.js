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
/* Grendel the Really Old
	Magician Job Advancement
	Victoria Road : Magic Library (101000003)

	Custom Quest 100006, 100008, 100100, 100101
*/

status = -1;
actionx = {"1stJob": false, "2ndjob": false, "3thJobI": false, "3thJobC": false};
job = 210;

const SPAWN_PNPC_FEE = 7000000;
const JOB_TYPE = 2;
const GameConstants = Java.type('org.gms.constants.game.GameConstants');


function start() {
    levelStart();
}

/**
 * @description 脚本开始执行
 */
function levelStart() {
    if (parseInt(cm.getJobId() / 100) === JOB_TYPE && cm.canSpawnPlayerNpc(GameConstants.getHallOfFameMapid(cm.getJob()))) {
        levelStartHallOfFame();
    } else {
        levelStartChangeJob();
    }
}

/**
 * @description 处理名人堂相关的起始方法
 */
function levelStartHallOfFame() {
    let sendStr = "你已经走了很长的路才获得你今天所拥有的力量、智慧和勇气，不是吗？你觉得在#r冒险岛荣耀大厅，持有你当前角色的图像#k怎么样？";
    if (SPAWN_PNPC_FEE > 0) {
        sendStr += "只需要支付 #b " + cm.numberWithCommas(SPAWN_PNPC_FEE) + " 金币#k，我就可以帮你实现~";
    }
    // 选择否就调用dispose，选择是就走levelCheckHallOfFame
    cm.sendYesNoLevel("Dispose", "CheckHallOfFame", sendStr);
}

/**
 * @description 校验并执行名人堂操作
 */
function levelCheckHallOfFame() {
    if (cm.getMeso() < SPAWN_PNPC_FEE) {
        cm.sendOkLevel("Dispose", "抱歉，您没有足够的金币购买在名人堂上的位置。");
        return;
    }

    const PlayerNPC = Java.type('org.gms.server.life.PlayerNPC');
    let msg;
    if (PlayerNPC.spawnPlayerNPC(GameConstants.getHallOfFameMapid(cm.getJob()), cm.getPlayer())) {
        cm.gainMeso(-SPAWN_PNPC_FEE);
        msg = "给你了！希望你会喜欢它。";
    } else {
        msg = "抱歉，名人堂目前已满...";
    }
    cm.sendOkLevel("Dispose", msg);
}

/**
 * @description 处理转职相关的起始方法
 */
function levelStartChangeJob() {
    if (cm.getJobId() === 0) {
        // 1转
        cm.sendNextLevel("StartFistJob", "想成为一个#r魔法师#k吗？有一些标准需要满足，因为我们不能接受每个人... #b你的等级至少应该是8#k，首要目标是获得" + cm.getFirstJobStatRequirement(JOB_TYPE) + "。让我们看看。");   // thanks Vcoc for noticing a need to state and check requirements on first job adv starting message
    } else if (cm.getLevel() >= 30 && cm.getJobId() === 200) {
        // 2转
        if (cm.haveItem(4031012)) {
            cm.sendNextLevel("StartSecondJob1", "我看到你做得很好。我会允许你迈出漫长道路上的下一步。");
        } else if (cm.haveItem(4031009)) {
            cm.sendOkLevel("Dispose", "去找#b#p1072001##k。");
        } else {
            cm.sendNextLevel("StartSecondJob2", "你取得的进步令人惊讶。");
        }
    } else if (actionx["3thJobI"] || (cm.getPlayer().gotPartyQuestItem("JB3") && cm.getLevel() >= 70 && cm.getJobId() % 10 == 0 && parseInt(cm.getJobId() / 100) == 2 && !cm.getPlayer().gotPartyQuestItem("JBP"))) {
        cm.sendNext("你来了。几天前，奥西里亚的#b#p2020009##k跟我谈到了你。我看到你对成为魔法师的第三次职业转职感兴趣。为了实现这个目标，我需要测试你的实力，看看你是否配得上这个晋升。在金银岛的一个邪恶深林中有一个开口，会带你通往一个秘密通道。一旦进入，你将面对我的一个克隆。你的任务是打败他，并带着#b#t4031059##k回来。");
    } else if (cm.getPlayer().gotPartyQuestItem("JBP") && !cm.haveItem(4031059)) {
        cm.sendNext("请给我带来#b#t4031059##k，它是从我的克隆体那里得到的。你可以在邪恶森林深处的空间洞穴里找到他。");
        cm.dispose();
    } else if (cm.haveItem(4031059) && cm.getPlayer().gotPartyQuestItem("JBP")) {
        cm.sendNext("干得好。你打败了我的分身，并安全地带回了#b#t4031059##k。你现在已经从物理角度证明了自己配得上进行第三次职业转职。现在你应该把这条项链交给在奥西里亚的#b#p2020011##k，以进行测试的第二部分。祝你好运。你会需要的。");
    } else {
        cm.sendOkLevel("Dispose", "明智的选择");
    }
}

function levelStartFistJob() {
    if (cm.getLevel() >= 8 && cm.canGetFirstJob(JOB_TYPE)) {
        cm.sendYesNo("Dispose", "FinishFirstJob1", "哦...！你看起来就像是我们团队的一员... 你只需要一点邪恶的心思，然后... 是的... 那么，你觉得怎么样？想成为魔法师吗？");
    } else {
        m.sendOkLevel("Dispose", "再多训练一会儿，直到你达到基本要求，我就可以教你成为#r魔法师#k的方法。");
    }
}

function levelFinishFirstJob1() {
    if (cm.canHold(1372043)) {
        if (cm.getJobId() === 0) {
            cm.changeJobById(200);
            cm.gainItem(1372043, 1);
            cm.resetStats();
        }
        cm.sendNextLevel("FinishFirstJob2", "好的，从现在开始，你就是我们的一员了！你将在...过着流浪者的生活，但要耐心等待，很快你就会过上富裕的生活。好了，虽然不多，但我会传授给你一些我的能力... 哈啊啊啊！！");
    } else {
        cm.sendNextLevel("Dispose", "清理一下你的背包，然后回来找我说话。");
    }
}

function levelFinishFirstJob2() {
    cm.sendLastNextLevel("FinishFirstJob1", "FinishFirstJob3", "你现在变得更强大了。而且你的所有物品栏都增加了槽位。确切地说，是一整行。去亲自看看吧。我刚给了你一点点#bSP#k。当你在屏幕左下角打开#b技能#k菜单时，可以使用SP学习技能。不过要注意一点：你不能一次性全部提升。在学习了一些技能之后，你还可以获得一些特定的技能。");
}

function levelFinishFirstJob3() {
    cm.sendLastNextLevel("FinishFirstJob2", "FinishFirstJob4", "但是要记住，技能并不是一切。作为一个魔法师，你的属性应该支持你的技能。魔法师主要使用智力作为他们的主属性，幸运作为次要属性。如果提升属性很困难，就使用#b自动分配#k。");
}

function levelFinishFirstJob4() {
    cm.sendLastNextLevel("FinishFirstJob3", "FinishFirstJob5", "现在，我要再警告你一句。如果你从现在开始在战斗中失败，你将失去一部分总经验值。要特别注意这一点，因为你的生命值比大多数人都要少。");
}

function levelFinishFirstJob5() {
    cm.sendLastNextLevel("FinishFirstJob4", "Dispose", "这是我能教你的全部了。祝你在旅途中好运，年轻的魔法师。");
}

function levelStartSecondJob1() {
    cm.sendSelectLevel("", "好的，当你做出决定后，点击底部的【我会选择我的职业】。#b\r\n#L0#请解释一下成为火毒法师是什么意思。\r\n#L1#请解释一下成为冰雷法师是什么意思。\r\n#L2#请解释一下成为牧师是什么意思。\r\n#L3#我会选择我的职业！");
}

function levelStartSecondJob2() {

}

function levelDispose() {
    cm.dispose();
}
function getJobName() {
    return job == 210 ? "#b法师（火/毒）#k" : (job == 220 ? "#b法师（冰/雷）#k" : "#b牧师#k");
}