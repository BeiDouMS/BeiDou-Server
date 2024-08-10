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
		qm.sendNext("我相信在你的旅途中它会派上用场的。拜托，别拒绝我的提议。");
		qm.dispose();
		return;
 	}else{
		qm.dispose();
		return;
	}
    }

    if (status == 0) {
	qm.sendSimple("啊，你是英雄。我一直很想见你. \r\n#b#L0#(好像有点害羞...)#l");		
    } else if (status == 1) {
	qm.sendAcceptDecline("我有一件东西我一直想送给你作为礼物很长一段时间...我知道你很忙，尤其是当你在你的城市的路上，但你会接受我的礼物吗？");
    } else if (status == 2) {
        qm.forceStartQuest();
	qm.sendNext("礼物的各个部分都装在附近的一个盒子里。对不起，麻烦你了，你能不能把盒子弄坏，给我拿个 #b#t4032309##k 和 #b#t4032310##k? 我马上给你组装好.", 9);
    } else if (status == 3) {
        qm.guideHint(18);
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if(type == 1 && mode == 0) {
			qm.sendNext("什么？你不想要？");
            qm.dispose();
            return;
        } else {
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
	qm.sendYesNo("啊，你把所有的部件都带来了。给我几秒钟把它们组装起来..像这样。..就像那样。..还有...\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v3010062# 1 #t3010062#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 95 exp");
    } else if (status == 1) {
        if (qm.isQuestCompleted(21013)) {
            qm.dropMessage(1, "Unknown Error");
        }
        qm.forceCompleteQuest();
        qm.gainExp(95);
        qm.gainItem(4032309, -1);
        qm.gainItem(4032310, -1);
        qm.gainItem(3010062, 1);
	    qm.sendNext("给你一把整装好的椅子我一直想送给你一把椅子作为礼物，因为我知道英雄偶尔也可以好好休息一下。", 9);	
    } else if (status == 2) {
	qm.sendNext("英雄不是不可战胜的。英雄是人。我相信你有时会面对挑战甚至犹豫不决。但你是英雄，因为你有能力克服你可能遇到的任何障碍。", 9);
    } else if (status == 3) {
        qm.guideHint(19);
        qm.dispose();
    }
}