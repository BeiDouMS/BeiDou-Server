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
	if(type == 1 && mode == 0) {
		qm.sendOk("哦，那样啊。英雄果然很忙啊....哭哭。要是改变主意了，随时可以来找我。");
		qm.dispose();
		return;
 	}else{
		qm.dispose();
		return;
	}
    }

    if (status == 0) {
	qm.sendNext("等等，你...不可能...你就是那个 #p1201000# 一直在谈论这些？! #p1201000#! 别只是点头…告诉我！这就是你一直在等待的英雄吗？! ")		
    } else if (status == 1) {
        qm.sendNextPrev("   #i4001171#");
    } else if (status == 2) {
	qm.sendNextPrev(" 我很抱歉。我只是太激动了... *555..555* 天啊，我都快哭了。你一定很开心, #p1201000#.");
    } else if (status == 3) {
	qm.sendAcceptDecline("等一下...你没带武器据我所知，每个英雄都有特殊的武器。哦，你一定是在和黑法师的战斗中把它弄丢了");
    } else if (status == 4) {
	qm.forceStartQuest();
	qm.sendOk("我的兄弟 #bPuir #k就在街上，他很想见你！想认识你' 但我很忙，你能过去和Puir打个招呼吗？求你了...");
    } else if (status == 5) {
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0) {
	    qm.sendNext("*555..555* 这把剑对你来说还不够好吗？我很荣幸...");
	    qm.dispose();
        }else{
            qm.dispose();
            return;
        }  
	}		
    if (status == 0) 
	qm.sendNext("等等，你……不可能……你就是林一直在说的那个英雄吗？！丽琳！别只是点头…告诉我！这就是你一直在等待的英雄吗？!");
    else if (status == 1) {   	
    qm.sendNextPrev("#i4001171#");
    } else if (status == 2) { 
	qm.sendNextPrev("我很抱歉。我只是太激动了... *555..555* 天啊，我都快哭了。你一定很高兴，丽琳。");
    } else if (status == 3) { 
	qm.sendNextPrev("等一下...你没带武器据我所知，每个英雄都有特殊的武器。哦，你一定是在和黑法师的战斗中把它弄丢了。");    
    } else if (status == 4) {  
	qm.sendYesNo("这还不足以替换你的武器，但是 #b你先拿着这把剑#k. 这是我给你的礼物英雄不可能空手而归.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v1302000# 1 #t1302000#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 35 exp");	
    } else if (status == 5) {
        if (qm.isQuestCompleted(21011)) {
            qm.dropMessage(1, "Unknown Error");
        } else if (qm.canHold(1302000)) {
            qm.gainItem(1302000, 1);
            qm.gainExp(35);
            qm.forceCompleteQuest();
		qm.sendNext("#b(你的技术和赫洛差不多除了一把剑？你这辈子有没有拿过剑？你不记得...你是怎么装备它的？)", 3);
        } else {
		qm.dropMessage(1,"你的背包已经满了");   
        }
    } else if (status == 6) {
        qm.guideHint(16);
        qm.dispose();
    }
}