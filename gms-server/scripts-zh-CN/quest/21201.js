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
    if (mode == -1) {
        qm.sendNext("“嘿！至少说你试过了”!");
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.sendNext("“嘿！至少说你试过了！”");
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            qm.sendNext("“首先你承诺打败黑魔法师并为我制造一件著名的武器，然后你抛弃我几百年，现在你告诉我你不记得我是谁了？什么？！你以为我会让你逃脱惩罚吗？你是那个乞求并渴望我的人！”");
        }//Giant Polearm
        else if (status == 1) {
            qm.sendNextPrev("“我确实告诉#p1203000#如果我能证明自己的价值，他就为我制作一把长柄武器。”", 2);
        } else if (status == 2) {
            qm.sendNextPrev("经过了那么多的乞求，你难道不应该对我多一点爱和尊重吗？你知道，像我这样的武器是罕见而美妙的东西。我是终极的#p1201001#，可以帮助你打败黑法师。你怎么能把我抛弃好几百年呢?");
        } else if (status == 3) {
            qm.sendNextPrev("“嘿，我从没求过你。”", 2);
        } else if (status == 4) {
            qm.sendNextPrev("“什么？你从来没为我乞求过？哈哈！ #p1203000# 告诉我你曾经跪下，流着泪为我乞求，而且……等一下。阿伦！你刚刚想起我是谁了吗？”?");
        } else if (status == 5) {
            qm.sendNextPrev("“也许有一点...”", 2);
        } else if (status == 6) {
            qm.sendNextPrev("“阿兰，是你！*嗅嗅* 等等，*咳* 我没有变得情绪化，只是过敏。我知道黑魔法师夺走了你的能力，所以你可能连把我抱起来的力气都没有……但至少你还记得我！我很高兴你的记忆开始恢复。”");
        } else if (status == 7) {
            qm.sendAcceptDecline("“即使你失去了记忆，你仍然是我的主人。你过去经历了一些非常艰苦的训练，我相信你的身体仍然记得那些艰难时光中学到的技能。好吧，我会恢复你的能力。”!");
        } else if (status == 8) {
            if (!qm.isQuestCompleted(21201)) {
                if (!qm.canHold(1142130)) {
                    qm.sendOk("“哇，你的#b装备栏#k已满。我需要你至少腾出一个空位来完成这个任务”.");   // thanks MedicOP for finding an issue here
                    return;
                }

                qm.gainItem(1142130, true);
                qm.changeJobById(2110);

                const GameConfig = Java.type('org.gms.config.GameConfig');
                if (GameConfig.getServerBoolean("use_full_aran_skill_set")) {
                    qm.teachSkill(21100000, 0, 20, -1);   //polearm mastery
                    qm.teachSkill(21100002, 0, 30, -1);   //final charge
                    qm.teachSkill(21100004, 0, 20, -1);   //combo smash
                    qm.teachSkill(21100005, 0, 20, -1);   //combo drain
                }

                qm.completeQuest();
            }

            qm.sendNext("“你的水平已经不再像往日的辉煌时期了，所以我无法恢复你所有的旧能力。不过，我能恢复的几种能力应该能帮助你更快升级。现在赶紧去训练吧，这样你才能恢复成以前的自己。”");
        } else if (status == 9) {
            qm.dispose();
        }
    }
}