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
	if(type == 2 && mode == 0) {
		qm.sendOk("你不觉得这有帮助吗？好好想想。它可以帮助，你知道...");
		qm.dispose();
		return;
 	}else{
		qm.dispose();
		return;
	}  
	}
    if (status == 0) 
	qm.sendNext("欢迎，英雄！那是什么？你想知道我是怎么知道你是谁的吗？这很容易。我偷听一些人在我旁边大声说话。我相信谣言已经传遍全岛了。大家都知道你回来了")		
    else if (status == 1) {
	qm.sendNextPrev("嗯，试试那把剑怎么样？不会让你想起什么吗？不如打一些怪物？");
    } else if (status == 2) {
	qm.sendAcceptDecline("啊，我很抱歉。我很高兴终于见到你了，我想我有点忘乎所以了。喔，深呼吸。深呼吸。好吧，我现在感觉好多了。但我可以请你帮个忙吗？求你了");
    } else if (status == 3) {
	qm.forceStartQuest();
	qm.sendNext("正好这附近有很多 #r#o9300383#s#k 请您去击退 #r3只#k。	搞不好会想起些什么。");
    } else if (status == 4) { 
	qm.sendNextPrev("啊，该不会连技能使用方法都忘光了吧？ #b将技能放入快捷栏就可以轻松使用#k。 不只是技能，连消耗道具也可以放进去，请多加利用。") ;  
    } else if (status == 5) { 
	qm.guideHint(17); 
	qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0) {
            qm.sendNext("什么？你不喜欢药水？");
            qm.dispose();
            return;
        } else {
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
	qm.sendOk("嗯...看您的表情，似乎什么都没有想起来...可是请不要担心。总有一天会好起来的。来，请您喝下这些药水打起精神来!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2000022# 10 #t2000022#\r\n#v2000023# 10 #t2000023#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 57 exp");
    } else if (status == 1) {
        if (qm.isQuestCompleted(21012)) {
            qm.dropMessage(1, "Unknown Error");
        } else if (qm.canHold(2000022) && qm.canHold(2000023)) {
            qm.forceCompleteQuest();
            qm.gainExp(57);
            qm.gainItem(2000022, 10);
            qm.gainItem(2000023, 10);
	    qm.sendOk("#b(就算我是真正的英雄...可是什么能力都没有的英雄还有用处吗?)", 3);
        } else {
            qm.dropMessage(1,"背包满了");  
            qm.dispose();
        }
    } else if (status == 2) {
        qm.dispose();
    }
}