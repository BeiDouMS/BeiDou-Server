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
	Map(s): 		Aran Training Map 2
	Description: 		Quest - Help Kid
	Quest ID : 		21000
*/

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            status -= 2;
        } else {
            qm.sendNext("不行，战神……要是抛下孩子我们自己逃掉……就算能活下去也没什么意义！我知道这个要求对你而言很勉强，不过还是请你再考虑考虑！");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("糟糕！有个孩子被留在森林里了！我们不能丢下孩子就这么逃走！战神……请你救救孩子吧！你伤得这么重，还要你去战斗，我们心里也很过意不去……但只有你能够救那个孩子啊！");
    } else if (status == 1) {
        qm.forceStartQuest();
        qm.sendNext("#b孩子可能在森林的深处#k！必须在黑魔法师找到我们之前，启动方舟，所以必须尽快救出孩子才行！");
    } else if (status == 2) {
        qm.sendNextPrev("关键是不要慌张，战神。如果你要查看任务进行状态，按#bQ键#k就能在任务栏中查看。");
    } else if (status == 3) {
        qm.sendNextPrev("拜托了，战神！救救孩子吧！我们不能再有人因为黑魔法师而牺牲了！");
    } else if (status == 4) {
        qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1");
        qm.dispose();
    }
}


function end(mode, type, selection) {
    qm.forceCompleteQuest();
    qm.dispose();
}