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
            qm.sendAcceptDecline("Oh, no! I think there's still a child in the forest! Aran, I'm very sorry, but could you rescue the child? I know you're injured, but I don't have anyone else to ask!");
            break;
        case 1:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("No, Aran... We can't leave a kid behind. I know it's a lot to ask, but please reconsider. Please!");
                qm.dispose();
                break;
            }
            if (!qm.isQuestStarted(21000) && !qm.isQuestCompleted(21000)) {
                qm.forceStartQuest();
            }
            qm.sendNext("#bThe child is probably lost deep inside the forest!#k We have to escape before the Black Mage finds us. You must rush into the forest and bring the child back with you!");
            break;
        case 2:
            qm.sendNextPrev("Don't panic, Aran. If you wish to check the status of the \r\nquest, press #bQ#k and view the Quest window.");
            break;
        case 3:
            qm.sendNextPrev("Please, Aran! I'm begging you. I can't bear to lose another person to the Black Mage!");
            break;
        case 4:
            qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1");
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}