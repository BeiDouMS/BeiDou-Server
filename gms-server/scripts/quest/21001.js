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
	    if(type == 15 && mode == 0) {
		qm.sendNext("勇士拒绝了我的请求!");
		    qm.dispose();
			return;
		}else{
		    qm.dispose();
			return;
		}
	}
		if (status == 0) {
			qm.sendAcceptDecline("我好害怕,请带我去妈妈那.");
		} else if (status == 1) {
			qm.gainItem(4001271, 1);
			qm.forceStartQuest();
			qm.warp(914000300, 0);
			qm.dispose();
		}	
}

function end(mode, type, selection) {
        status++;
        if (mode != 1) {
                if (type == 1 && mode == 0) {
                        qm.sendNext("孩子怎么办？请把孩子给我!");
                }
                
                qm.dispose();
                return;
        }
        
        if (status == 0)
                qm.sendYesNo("你安全回来了！孩子怎么办？！你带孩子来了吗？！");
        else if (status == 1) {
                qm.sendNext("哦，真是松了一口气。我太高兴了...", 9);
        } else if (status == 2)
                qm.sendNextPrev("快点上船！我们时间不多了！", 3);
        else if (status == 3)
                qm.sendNextPrev("我们没时间浪费了。黑法师的力量越来越近了！如果我们现在不马上离开，我们就完了！", 9);
        else if (status == 4)
                qm.sendNextPrev("现在就走!", 3);
        else if (status == 5)
                qm.sendNextPrev("勇士，求你了！我知道你想留下来和黑魔法师战斗，但太晚了！把它留给其他人，和我们一起去维多利亚岛！", 9);
        else if (status == 6)
                qm.sendNextPrev("不，我不能！", 3);
        else if (status == 7) {
                qm.sendNextPrev("你为什么不先去维多利亚岛？我答应你，我以后会来找你。我不会有事的我要和其他英雄一起对抗黑法师！", 3);
        } else if (status == 8) {
                qm.gainItem(4001271, -1);
                qm.removeEquipFromSlot(-11);
                qm.forceCompleteQuest();

        qm.warp(914090010, 0); // Initialize Aran Tutorial Scenes
        qm.dispose();
    }
}