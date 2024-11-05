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

var status = -1;

function end(mode, type, selection) {
    status++;
    if (mode != 1){
        if (mode == 0 && type == 1)
            qm.sendNext("嘿！至少你试过了!");
        qm.dispose();
        return;
    }
    if (status == 0) {
        qm.sendNext("等待。。是不是。。你记得怎么做红玉吗？\r\n如何。。。你可能很愚蠢，容易健忘症，但这就是为什么我不能抛弃你。现在把玉给我!"); //Giant Polearm
    } else if (status == 1) {
        qm.sendNextPrev("好吧，现在我又戴上了红玉，让我重新唤醒你更多的能力。我是说，自从我们上次见面以来，你的水平提高了很多，所以我相信这次我可以发挥我的魔力!");
    } else if (status == 2) {
        if(!qm.isQuestCompleted(21302)) {
            if(!qm.canHold(1142131)) {
                qm.sendOk("哇，您的 #b装备#k 库存已满。 我需要您至少腾出1个空位才能完成此任务.");
                qm.dispose();
                return;
            }

            if (qm.haveItem(4032312, 1)) {
                qm.gainItem(4032312, -1);
            }

            qm.gainItem(1142131, true);
            qm.changeJobById(2111);

            const GameConfig = Java.type('org.gms.config.GameConfig');
            if (GameConfig.getServerBoolean("use_full_aran_skill_set")) {
                qm.teachSkill(21110002, 0, 20, -1);   //full swing
            }

            qm.completeQuest();
        }
        
        qm.sendNext("来吧，继续训练，这样您就可以恢复自己的全部能力，这样我们就可以再次一起探索!");    
    } else if (status == 3) {
        qm.dispose();
    }
}