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
/* Author: Xterminator
	NPC Name: 		Cloy
	Map(s): 		Victoria Road : Henesys Park (100000200)
	Description: 		Pet Master
 */
var status = -2;
var sel;

function start() {
    status = -2
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

        if (status == -1) {
            cm.sendNext("嗯...你是不是偶然间在抚养我的孩子？我完善了一个使用生命之水给玩偶注入生命的咒语。人们称之为#b宠物#k。如果你身边有一个，随时可以问我问题。");
        } else if (status == 0) {
            cm.sendSimple("你想了解更多什么？#b\r\n#L0#告诉我更多关于宠物的信息。#l\r\n#L1#我如何养宠物？#l\r\n#L2#宠物也会死吗？#l\r\n#L3#布朗和黑色小猫的指令是什么？#l\r\n#L4#棕色小狗的指令是什么？#l\r\n#L5#粉红和白色兔子的指令是什么？#l\r\n#L6#小魔龙的指令是什么？#l\r\n#L7#圣诞麋鹿的指令是什么？#l\r\n#L8#黑色猪的指令是什么？#l\r\n#L9#熊猫的指令是什么？#l\r\n#L10#哈士奇的指令是什么？#l\r\n#L11#迪诺龙、妮诺龙的指令是什么？#l\r\n#L12#猴子的指令是什么？#l\r\n#L13#火鸡的指令是什么？#l\r\n#L14#白虎的指令是什么？#l\r\n#L15#企鹅的指令是什么？#l\r\n#L16#金猪的指令是什么？#l\r\n#L17#机器人的指令是什么？#l\r\n#L18#迷你雪吉拉的指令是什么？#l\r\n#L19#巴洛谷的指令是什么？#l\r\n#L20#宝宝龙的指令是什么？#l\r\n#L21#绿色/红色/蓝色龙的指令是什么？#l\r\n#L22#黑龙的指令是什么？#l\r\n#L23#黑色鬼精灵的指令是什么？#l\r\n#L24#豪猪的指令是什么？#l\r\n#L25#雪人的指令是什么？#l\r\n#L26#臭鼬的指令是什么？#l\r\n#L27#请教我如何转移宠物能力点。#l");
        } else if (status == 1) {
            sel = selection;
            if (selection == 0) {
                status = 3;
                cm.sendNext("所以你想了解更多关于宠物的事情。很久以前，我制作了一个玩偶，喷洒了生命之水，并对它施了咒语，创造出了一个神奇的动物。我知道这听起来不可思议，但它是一个变成了真实生物的玩偶。它们非常理解并且能很好地跟随人类。");
            } else if (selection == 1) {
                status = 6;
                cm.sendNext("根据您给出的指令，宠物可能会喜欢、讨厌，并对其显示其他种类的反应。如果您给宠物一个指令，它能很好地跟随您，您的亲密度会提高。双击宠物，您可以检查亲密度、等级、饱食度等等...");
            } else if (selection == 2) {
                status = 11;
                cm.sendNext("垂死……嗯，严格来说它们并不算是活着的，所以我不知道是否用“垂死”这个词合适。它们是用我的魔法力量和生命之水变成活物的娃娃。当然，它们活着的时候，就像活生生的动物一样……");
            } else if (selection == 3) {
                cm.sendNext("这些是#r棕色小猫和黑色小猫#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏猫, 不行, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b笨蛋, 我讨厌你, 笨蛋#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1~30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b说话, 说, 聊天#k (等级 10 ~ 30)\r\n#b可爱#k (等级 10 ~ 30)\r\n#b站起来, 站立, 起来#k (等级 20 ~ 30)");
            } else if (selection == 4) {
                cm.sendNext("这些是#r棕色小狗#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏狗, 不行, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b笨蛋, 我讨厌你, 坏狗, 笨蛋#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1~30)\r\n#b撒尿#k (等级 1 ~ 30)\r\n#b说话, 说, 聊天#k (等级 10 ~ 30)\r\n#b趴下#k (等级 10 ~ 30)\r\n#b站起来, 站, 起立#k (等级 20 ~ 30)");
            } else if (selection == 5) {
                cm.sendNext("这些是#r粉红兔子和白兔子#k的指令。指令旁边提到的等级是宠物需要的等级才能响应。\r\n#b坐#k (等级 1 ~ 30)\r\n#b坏, 不行, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b站起来, 站立, 起立#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1~30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b说话, 说, 聊天#k (等级 10 ~ 30)\r\n#b拥抱#k (等级 10 ~ 30)\r\n#b睡觉, 困, 上床#k (等级 20 ~ 30)");
            } else if (selection == 6) {
                cm.sendNext("这些是#r小魔龙#k的指令。指令旁边提到的等级是宠物需要的等级才能响应。\r\n#bsit#k (等级 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (等级 1 ~ 30)\r\n#bup, stand, rise#k (等级 1 ~ 30)\r\n#biloveyou#k (等级 1~30)\r\n#bpee#k (等级 1 ~ 30)\r\n#btalk, say, chat#k (等级 10 ~ 30)\r\n#bthelook, charisma#k (等级 10 ~ 30)\r\n#bdown#k (等级 10 ~ 30)\r\n#bgoodboy, goodgirl#k (等级 20 ~ 30)");
            } else if (selection == 7) {
                cm.sendNext("这些是#r圣诞麋鹿#k的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#bsit#k (等级 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (等级 1 ~ 30)\r\n#bup, stand#k (等级 1 ~ 30)\r\n#bstupid, ihateyou, dummy#k (等级 1 ~ 30)\r\n#bmerryxmas, merrychristmas#k (等级 1 ~ 30)\r\n#biloveyou#k (等级 1~30)\r\n#bpoop#k (等级 1 ~ 30)\r\n#btalk, say, chat#k (等级 11 ~ 30)\r\n#blonely, alone#k (等级 11 ~ 30)\r\n#bcutie#k (等级 11 ~ 30)\r\n#bmush, go#k (等级 21 ~ 30)");
            } else if (selection == 8) {
                cm.sendNext("这些是#r黑猪#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏猪, 不行, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1~30)\r\n#b握手#k (等级 1 ~ 30)\r\n#b笨蛋, 我讨厌你, 笨蛋#k (等级 1 ~ 30)\r\n#b说话, 聊天, 说#k (等级 10 ~ 30)\r\n#b微笑#k (等级 10 ~ 30)\r\n#b魅力, 魅力#k (等级 20 ~ 30)");
            } else if (selection == 9) {
                cm.sendNext("这些是#r熊猫#k的指令。指令旁边提到的等级是宠物需要达到的等级才能响应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b放松, 休息#k (等级 1 ~ 30)\r\n#b坏, 不行, 坏孩子, 坏孩子#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1 ~ 30)\r\n#b站起来, 站立, 起立#k (等级 1 ~ 30)\r\n#b说话, 聊天, 说#k (等级 10 ~ 30)\r\n#b让我们玩#k (等级 10 ~ 30)\r\n#b嗯, 呃#k (等级 10 ~ 30)\r\n#b睡觉#k (等级 20 ~ 30)");
            } else if (selection == 10) {
                cm.sendNext("这些是#r哈士奇#k的指令。指令旁边提到的等级是宠物需要达到的等级才能响应。\r\n#bsit#k (等级 1 ~ 30)\r\n#bbad, no, badgirl, badboy#k (等级 1 ~ 30)\r\n#bstupid, ihateyou, baddog, dummy#k (等级 1 ~ 30)\r\n#bhand#k (等级 1 ~ 30)\r\n#bpoop#k (等级 1 ~ 30)\r\n#biloveyou#k (等级 1 ~ 30)\r\n#bdown#k (等级 10 ~ 30)\r\n#btalk, chat, say#k (等级 10 ~ 30)\r\n#bup, stand, rise#k (等级 20 ~ 30)");
            } else if (selection == 11) {
                cm.sendNext("这些是#r迪诺龙和妮诺龙#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏孩子, 不行, 坏男孩, 坏女孩#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b微笑, 笑#k (等级 1 ~ 30)\r\n#b笨蛋, 我讨厌你, 笨蛋#k (等级 1 ~ 30)\r\n#b说话, 聊天, 说#k (等级 10 ~ 30)\r\n#b可爱#k (等级 10 ~ 30)\r\n#b睡觉, 小睡, 困#k (等级 20 ~ 30)");
            } else if (selection == 12) {
                cm.sendNext("这些是#r猴子#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b休息#k (等级 1 ~ 30)\r\n#b坏孩子, 不乖#k (等级 1 ~ 30)\r\n#b撒尿#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1 ~ 30)\r\n#b站起来#k (等级 1 ~ 30)\r\n#b说话, 聊天#k (等级 10 ~ 30)\r\n#b玩耍#k (等级 10 ~ 30)\r\n#b我想你#k (等级 10 ~ 30)\r\n#b睡觉, 上床睡觉, 困了#k (等级 20 ~ 30)");
            } else if (selection == 13) {
                cm.sendNext("这些是#r火鸡#k的指令。指令旁边提到的等级是宠物需要达到的等级才能响应。\r\n#bsit#k (等级 1 ~ 30)\r\n#bno, rudeboy, mischief#k (等级 1 ~ 30)\r\n#bstupid#k (等级 1 ~ 30)\r\n#biloveyou#k (等级 1 ~ 30)\r\n#bup, stand#k (等级 1 ~ 30)\r\n#btalk, chat, gobble#k (等级 10 ~ 30)\r\n#byes, goodboy#k (等级 10 ~ 30)\r\n#bsleepy, birdnap, doze#k (等级 20 ~ 30)\r\n#bbirdeye, thanksgiving, fly, friedbird, imhungry#k (等级 30)");
            } else if (selection == 14) {
                cm.sendNext("这些是#r白虎#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏孩子, 不行, 坏男孩, 坏女孩#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b休息, 放松#k (等级 1 ~ 30)\r\n#b笨蛋, 我讨厌你, 笨蛋#k (等级 1 ~ 30)\r\n#b说话, 聊天, 说#k (等级 10 ~ 30)\r\n#b悲伤, 伤心#k (等级 10 ~ 30)\r\n#b等待#k (等级 20 ~ 30)");
            } else if (selection == 15) {
                cm.sendNext("这些是 #r企鹅#k 的指令。指令旁边提到的等级是宠物需要达到的等级才能执行该指令。\r\n#b坐#k (等级 1 ~ 30)\r\n#b坏, 不行, 坏孩子, 坏女孩#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b站起来, 站, 起来#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1 ~ 30)\r\n#b说话, 聊天, 说#k (等级 10 ~ 30)\r\n#b拥抱, 抱抱我#k (等级 10 ~ 30)\r\n#b挥动, 举手#k (等级 10 ~ 30)\r\n#b睡觉#k (等级 20 ~ 30)\r\n#b亲吻, 亲亲, 亲一个#k (等级 20 ~ 30)\r\n#b飞#k (等级 20 ~ 30)\r\n#b可爱, 可爱的#k (等级 20 ~ 30)");
            } else if (selection == 16) {
                cm.sendNext("这些是 #r金猪#k 的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏孩子, 不行, 坏蛋, 坏女孩#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1 ~ 30)\r\n#b说话, 聊天, 说#k (等级 11 ~ 30)\r\n#b爱我, 拥抱我#k (等级 11 ~ 30)\r\n#b睡觉, 困了, 去睡觉#k (等级 21 ~ 30)\r\n#b无视 / 印象深刻 / 离开#k (等级 21 ~ 30)\r\n#b翻滚, 给我钱#k (等级 21 ~ 30)");
            } else if (selection == 17) {
                cm.sendNext("这些是#r机器人#k的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b站起来, 站立, 起立#k (等级 1 ~ 30)\r\n#b笨蛋, 我讨厌你, 笨蛋#k (等级 1 ~ 30)\r\n#b坏, 不, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b攻击, 冲锋#k (等级 1 ~ 30)\r\n#b我爱你#k (等级 1 ~ 30)\r\n#b好, 魅力, 魅力#k (等级 11 ~ 30)\r\n#b说话, 聊天, 聊天, 说#k (等级 11 ~ 30)\r\n#b变装, 变化, 变形#k (等级 11 ~ 30)");
            } else if (selection == 18) {
                cm.sendNext("这些是#r迷你雪吉拉#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b坏孩子, 不乖, 坏男孩, 坏女孩#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b跳舞, 舞动, 摇摆#k (等级 1 ~ 30)\r\n#b可爱, 可爱宝贝, 漂亮, 可爱的#k (等级 1 ~ 30)\r\n#b我爱你, 喜欢你, 我的爱#k (等级 1 ~ 30)\r\n#b说话, 聊天, 说#k (等级 11 ~ 30)\r\n#b睡觉, 小睡, 困了, 去睡觉#k (等级 11 ~ 30)");
            } else if (selection == 19) {
                cm.sendNext("这些是#r巴洛谷#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#bliedown#k (等级 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (等级 1 ~ 30)\r\n#biloveyou|mylove|likeyou#k (等级 1 ~ 30)\r\n#bcute|cutie|pretty|adorable#k (等级 1 ~ 30)\r\n#bpoop#k (等级 1 ~ 30)\r\n#bsmirk|crooked|laugh#k (等级 1 ~ 30)\r\n#bmelong#k (等级 11 ~ 30)\r\n#bgood|thelook|charisma#k (等级 11 ~ 30)\r\n#bspeak|talk|chat|say#k (等级 11 ~ 30)\r\n#bsleep|nap|sleepy#k (等级 11 ~ 30)\r\n#bgas#k (等级 21 ~ 30)");
            } else if (selection == 20) {
                cm.sendNext("这些是#r宝宝龙#k的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b不行|坏|坏女孩|坏男孩#k (等级 1 ~ 30)\r\n#b我爱你|爱你#k (等级 1 ~ 30)\r\n#b拉屎#k (等级 1 ~ 30)\r\n#b笨蛋|讨厌你|傻瓜#k (等级 1 ~ 30)\r\n#b可爱#k (等级 11 ~ 30)\r\n#b说话|聊天|说#k (等级 11 ~ 30)\r\n#b睡觉|困|上床#k (等级 11 ~ 30)");
            } else if (selection == 21) {
                cm.sendNext("这些是#r绿/红/蓝龙#k的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#b坐#k (等级 15 ~ 30)\r\n#b不|坏|坏女孩|坏男孩#k (等级 15 ~ 30)\r\n#b我爱你|爱你#k (等级 15 ~ 30)\r\n#b便便#k (等级 15 ~ 30)\r\n#b笨蛋|讨厌你|傻瓜#k (等级 15 ~ 30)\r\n#b说|聊天|说话#k (等级 15 ~ 30)\r\n#b睡觉|困|上床#k (等级 15 ~ 30)\r\n#b变身#k (等级 21 ~ 30)");
            } else if (selection == 22) {
                cm.sendNext("这些是#r黑龙#k的指令。指令旁边提到的等级是宠物需要达到的等级才能响应。\r\n#b坐下#k (等级 15 ~ 30)\r\n#b不行|坏|坏女孩|坏男孩#k (等级 15 ~ 30)\r\n#b我爱你|爱你#k (等级 15 ~ 30)\r\n#b拉屎#k (等级 15 ~ 30)\r\n#b笨蛋|讨厌你|傻瓜#k (等级 15 ~ 30)\r\n#b说话|聊天|说#k (等级 15 ~ 30)\r\n#b睡觉|困|上床#k (等级 15 ~ 30)\r\n#b可爱, 变化#k (等级 21 ~ 30)");
            } else if (selection == 23) {
                cm.sendNext("这些是#r黑色鬼精灵#k的指令。指令旁边提到的等级是宠物需要达到的等级才能执行该指令。\r\n#bsit#k (等级 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (等级 1 ~ 30)\r\n#bplaydead, poop#k (等级 1 ~ 30)\r\n#btalk|chat|say#k (等级 1 ~ 30)\r\n#biloveyou, hug#k (等级 1 ~ 30)\r\n#bsmellmyfeet, rockout, boo#k (等级 1 ~ 30)\r\n#btrickortreat#k (等级 1 ~ 30)\r\n#bmonstermash#k (等级 1 ~ 30)");
            } else if (selection == 24) {
                cm.sendNext("这些是#r豪猪#k的指令。指令旁边提到的等级显示了宠物需要的等级才能执行该指令。\r\n#bsit#k (等级 1 ~ 30)\r\n#bno|bad|badgirl|badboy#k (等级 1 ~ 30)\r\n#biloveyou|hug|goodboy#k (等级 1 ~ 30)\r\n#btalk|chat|say#k (等级 1 ~ 30)\r\n#bcushion|sleep|knit|poop#k (等级 1 ~ 30)\r\n#bcomb|beach#k (等级 10 ~ 30)\r\n#btreeninja#k (等级 20 ~ 30)\r\n#bdart#k (等级 20 ~ 30)");
            } else if (selection == 25) {
                cm.sendNext("这些是 #r雪人#k 的指令。指令旁边提到的等级显示了宠物需要的等级才能回应。\r\n#b坐下#k (等级 1 ~ 30)\r\n#b笨蛋, 我讨厌你, 笨蛋#k (等级 1 ~ 30)\r\n#b我爱你, 我的爱, 我喜欢你#k (等级 1 ~ 30)\r\n#b圣诞快乐#k (等级 1 ~ 30)\r\n#b可爱, 可爱, 可爱, 漂亮#k (等级 1 ~ 30)\r\n#b梳理, 海滩/坏, 不, 坏女孩, 坏男孩#k (等级 1 ~ 30)\r\n#b说话, 聊天, 说/睡觉, 困了, 去睡觉#k (等级 10 ~ 30)\r\n#b变化#k (等级 20 ~ 30)");
            } else if (selection == 26) {
                cm.sendNext("这些是#r臭鼬#k的指令。指令旁边提到的等级显示了宠物需要的等级才能响应。\r\n#bsit#k (等级 1 ~ 30)\r\n#bbad/no/badgirl/badboy#k (等级 1 ~ 30)\r\n#brestandrelax, poop#k (等级 1 ~ 30)\r\n#btalk/chat/say, iloveyou#k (等级 1 ~ 30)\r\n#bsnuggle/hug, sleep, goodboy#k (等级 1 ~ 30)\r\n#bfatty, blind, badbreath#k (等级 10 ~ 30)\r\n#bsuitup, bringthefunk#k (等级 20 ~ 30)");
            } else if (selection == 27) {
                status = 14;
                cm.sendNext("为了转移宠物的能力点、亲密度和等级，需要使用宠物能力点重置卷轴。如果你把这个卷轴带到艾琳尼亚的仙女玛尔那里，她会把宠物的等级和亲密度转移到另一个宠物身上。我特别给你这个是因为我能感受到你对宠物的情感。不过，我不能白白地把它给你。我可以以25万金币的价格把这本书卖给你。哦，我差点忘了！即使你有了这本书，如果没有新的宠物来转移能力点的话，它也没用。");
            }
            if (selection > 2 && selection < 27) {
                cm.dispose();
            }
        } else if (status == 2) {
            if (sel == 0) {
                cm.sendNextPrev("但是生命之水只在世界树的最底部出现一点点，所以我不能给它太多生命时间……我知道，这很不幸……但即使它再次变成玩偶，我总是可以把生命注入它，所以在你和它在一起的时候要对它好。");
            } else if (sel == 1) {
                cm.sendNextPrev("和宠物交谈，关注它，它的亲密度将提高，最终它的整体等级也会提高。随着亲密度的提高，宠物的整体等级也会很快提高。随着整体等级的提高，有一天宠物甚至可能会稍微像人一样说话，所以努力提升它。当然，这并不容易……");
            } else if (sel == 2) {
                cm.sendNextPrev("一段时间后……没错，它们停止了移动。魔法效果消失，生命之水干涸后，它们就会变回木偶。但这并不意味着它们永远停止了，因为一旦你倒上生命之水，它们就会重新活过来。");
            } else if (sel == 27) {
                cm.sendYesNo("25万金币将被扣除。你真的想购买吗？");
            }
        } else if (status == 3) {
            if (sel == 0) {
                cm.sendNextPrev("哦，是的，当你给它们特殊指令时，它们会做出反应。你可以责备它们，爱护它们……这都取决于你如何照顾它们。它们害怕离开主人，所以要对它们好，向它们展示爱。它们会很快感到伤心和孤独……");
            } else if (sel == 1) {
                cm.sendNextPrev("它可能是一个活的玩偶，但它们也有生命，所以它们也会感到饥饿。#b饱食度#k 显示宠物的饥饿水平。100 是最高值，数值越低，宠物就越饥饿。一段时间后，它甚至不会听从你的命令，而会主动进攻，所以要小心。");

            } else if (sel == 2) {
                cm.sendNextPrev("即使它有一天再次移动，看到它们完全停止还是很伤心的。请在它们还活着并移动时对它们好一点。也要好好喂养它们。知道有一些活着的东西只跟随并听从你，是不是很好呢？");
            } else if (sel == 27) {
                if (cm.getMeso() < 250000 || !cm.canHold(4160011)) {
                    cm.sendOk("请检查您的背包是否有空位，或者您是否没有足够的金币。");
                } else {
                    cm.gainMeso(-250000);
                    cm.gainItem(4160011, 1);
                }
                cm.dispose();
            }
        } else if (status == 4) {
            if (sel != 1) {
                cm.dispose();
            }
            cm.sendNextPrev("哦，是的！宠物不能吃普通的人类食物。相反，我的弟子#bDoofus#k在Henesys市场上出售#b宠物食品#k，所以如果你需要给你的宠物喂食，就去Henesys找他吧。最好提前买好食物，等宠物真的饿了再喂食。");
        } else if (status == 5) {
            cm.sendNextPrev("哦，如果你长时间不给宠物喂食，它会自己回家。你可以把它从家里带出来喂食，但这对宠物的健康不太好，所以尽量定期喂养它，这样它就不会降到那个程度了，好吗？我觉得这样就可以了。");
        } else {
            cm.dispose();
        }
    }
}