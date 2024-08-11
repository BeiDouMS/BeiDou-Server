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
			qm.sendNext("不，勇士...我们不能丢下一个孩子我知道这要求很过分，但请重新考虑。求你了!");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
			qm.sendAcceptDecline("哦，不！我想森林里还有个孩子！勇士，我很抱歉，但你可以救孩子吗？我知道你受伤了，但我没有其他人要问!");
    } else if (status == 1) {
        qm.forceStartQuest();
			qm.sendNext("#那孩子可能在森林深处迷路了！我们必须在黑魔法师找到我们之前逃走你必须冲进森林把孩子带回来!");
    } else if (status == 2) {
			qm.sendNextPrev("别慌，勇士。如果您想要检查 \r\任务，按 #bQ#k 再看看任务之窗");
    } else if (status == 3) {
			qm.sendNextPrev("求你了，勇士！我求你了。我不能忍受再有人被黑法师杀死！");
    } else if (status == 4) {
        qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1");
        qm.dispose();
    }
}


function end(mode, type, selection) {
    qm.forceCompleteQuest();
    qm.dispose();
}