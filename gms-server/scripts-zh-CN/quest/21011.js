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
	Author : kevintjuh93
*/
var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            qm.sendOk("啊，好吧。我明白。英雄们都很忙。啜泣... 不过，如果你有空的时候...");
            qm.dispose();
            return;
        } else {
            qm.dispose();
            return;
        }
    }

    if (status == 0) {
        qm.sendNext("和#p1201000#在一起的，难道……难道就是传说中的英雄？#p1201000#！别不耐烦地点头，给我们介绍介绍呀！这位就是传说中的英雄吗？！")
    } else if (status == 1) {
        qm.sendNextPrev("   #i4001171#");
    } else if (status == 2) {
        qm.sendNextPrev("……真对不起，太激动了，忍不住嗓门大了些。呜呜～真是令人激动……唉，眼泪都快出来了……#p1201000#这回可开心了。");
    } else if (status == 3) {
        qm.sendAcceptDecline("等等……英雄大人怎么能没有武器呢？我听说每个英雄都有自己的独特武器……啊，估计是和黑魔法师战斗的时候遗失了。");
    } else if (status == 4) {
        qm.forceStartQuest();
        qm.sendOk("我的弟弟 #b#p1202002##k 就在街那头，他一直很想见你！我知道你很忙，但请你能不能抽空去看看他，跟他打个招呼？拜托你了...");
    } else if (status == 5) {
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            qm.sendNext("呜呜，你是嫌这把剑太寒碜吗？");
            qm.dispose();
        } else {
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendNext("和#p1201000#在一起的，难道……难道就是传说中的英雄？#p1201000# ！别不耐烦地点头，给我们介绍介绍呀！这位就是传说中的英雄吗？！");
    } else if (status == 1) {
        qm.sendNextPrev("#i4001171#");
    } else if (status == 2) {
        qm.sendNextPrev("……真对不起，太激动了，忍不住嗓门大了些。呜呜～真是令人激动……唉，眼泪都快出来了……#p1201000#这回可开心了。");
    } else if (status == 3) {
        qm.sendNextPrev("等等……英雄大人怎么能没有武器呢？我听说每个英雄都有自己的独特武器……啊，估计是和黑魔法师战斗的时候遗失了。");
    } else if (status == 4) {
        qm.sendYesNo("虽然寒碜了点，不过#b先拿这把剑用着吧#k。算是送给英雄的礼物。英雄如果没有武器，岂不是会有些奇怪？\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v1302000# 1个 #t1302000#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 35 exp");
    } else if (status == 5) {
        if (qm.isQuestCompleted(21011)) {
            qm.dropMessage(1, "Unknown Error");
        } else if (qm.canHold(1302000)) {
            qm.gainItem(1302000, 1);
            qm.gainExp(35);
            qm.forceCompleteQuest();
            qm.sendNext("#b（看自己这技能水平没一点英雄的样子……这把剑感觉也很陌生。以前的我是用剑的吗？这把剑怎么用呢？）#k", 3);
        } else {
            qm.dropMessage(1, "你的背包已满。");
        }
    } else if (status == 6) {
        qm.guideHint(16);
        qm.dispose();
    }
}