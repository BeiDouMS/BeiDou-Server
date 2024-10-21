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
	Author : Generic
	NPC Name: 		Cygnus
	Map(s): 		Ereve: Empress' Road
	Description: 		Quest - Greetings from the Young Empress
	Quest ID : 		20000
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode > 0) {
            status++;
        } else {
            status--;
        }
        if (status == 0)
            qm.sendNext("啊，你来了。。。这真令人兴奋。我很感激你成为皇家骑士的决定。我等你这样的人等了很久了。有勇气面对黑魔法师而不退缩的人...");
        else if (status == 1)
            qm.sendNext("对抗想吞没整个枫叶世界的黑魔法师的邪恶本性，他的弟子的狡猾本性，以及对抗疯狂怪物的身体战等着你。总有一天，你甚至可以把自己变成仇敌，折磨你 ...");
        else if (status == 2)
            qm.sendOk("但我不担心这些。我相信你一定能战胜这一切，保护枫树世界免受黑魔法师的伤害。当然，你必须变得比现在强一点，对吧?");
        else if (status == 3) {
            qm.gainItem(1142065, 1); // Noblesse Medal * 1
            qm.gainExp(20); //gain 20 exp!!
            qm.forceStartQuest();
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}