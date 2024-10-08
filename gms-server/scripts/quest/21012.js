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

    if (mode == -1) { // END CHAT
        qm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendNext("Welcome, hero! What's that? You want to know how I knew who you were? That's easy. I eavesdropped on some people talking loudly next to me. I'm sure the rumor has spread through the entire island already. Everyone knows that you've returned!");
            break;
        case 1:
            qm.sendNextPrev("Ah, I'm so sorry. I was so happy to have finally met you that I guess I got a little carried away. Whew, deep breaths. Deep breaths. Okay, I feel better now. But um...can I ask you a favor? Please?");
            break;
        case 2:
            qm.sendAcceptDecline("Hm, how about trying out that sword? Wouldn't that bring back some memories? How about #bfighthing some monsters#k?");
            break;
        case 3:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendOk("Hm... You don't think that would help? Think about it. It could help, you know...");
                qm.dispose();
                break;
            }
            // ACCEPT
            if (!qm.isQuestStarted(21012)) {
                qm.forceStartQuest();
            }
            qm.sendNext("It just so happens that there are a lot of #rTutorial Murus #knear here. How about defeating just #r3 #kof them? It could help you remember a thing or two.");
            break;
        case 4:
            qm.sendNextPrev("Ah, you've also forgotten how to use your skills? #bPlace skills in the quick slots for easy access. #kYou can also place consumable items in the slots, so use the slots to your advantage.");
            break;
        case 5:
            qm.guideHint(17);
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}

function end(mode, type, selection) {
    if (mode == -1) { // END CHAT
        qm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendYesNo("Hm... Your expression tells me that the exercise didn't jog any memories. But don't you worry. They'll come back, eventually. Here, drink this potion and power up!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2000022# 10 #t2000022#\r\n#v2000023# 10 #t2000023#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 57 exp");
            break;
        case 1:
            if (type == 1 && mode == 0) { // NO
                qm.sendNext("What? You don't want the potion?");
                qm.dispose();
                break;
            }
            // YES
            if (!qm.canHold(2000022) || !qm.canHold(2000023)) {
                qm.dropMessage(1, "Your inventory is full");
                qm.dispose();
                break;
            }
            qm.forceCompleteQuest();
            qm.gainExp(57);
            qm.gainItem(2000022, 10);
            qm.gainItem(2000023, 10);
            qm.sendNext("#b(Even if you're really the hero everyone says you are... What good are you without any skills?)", 3);
        default:
            qm.dispose();
            break;
    }
}