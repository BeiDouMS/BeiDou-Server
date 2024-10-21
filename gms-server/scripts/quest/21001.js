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
            qm.sendAcceptDecline("*Sniff sniff* I was so scared... Please take me to Athena Pierce.");
            break;
        case 1:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("*Sob* Aran has declined my request!");
                qm.dispose();
                break;
            }
            // ACCEPT
            qm.gainItem(4001271, 1);
            qm.forceStartQuest();
            qm.warp(914000300, 0);
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
            qm.sendYesNo("You made it back safely! What about the child?! Did you bring the child with you?!");
            break;
        case 1:
            if (type == 1 && mode == 0) { // NO
                qm.sendNext("What about the child? Please give me the child!");
                qm.dispose();
            }
            // YES
            qm.sendNext("Oh, what a relief. I'm so glad...", 9);
            break;
        case 2:
            qm.sendNextPrev("Hurry and board the ship! We don't have much time!", 3);
            break;
        case 3:
            qm.sendNextPrev("We don't have any time to waste. The Black Mage's forces are getting closer and closer! We're doomed if we don't leave right right this moment!", 9);
            break;
        case 4:
            qm.sendNextPrev("Leave, now!", 3);
            break;
        case 5:
            qm.sendNextPrev("Aran, please! I know you want to stay and fight the Black Mage, but it's too late! Leave it to the others and come to Victoria Island with us!", 9);
            break;
        case 6:
            qm.sendNextPrev("No, I can't!", 3);
            break;
        case 7:
            qm.sendNextPrev("Athena Pierce, why don't you leave for Victoria Island first? I promise I'll come for you later. I'll be alright. I must fight the Black Mage with the other heroes!", 3);
            break;
        case 8:
            qm.gainItem(4001271, -1);
            qm.removeEquipFromSlot(-11);
            qm.forceCompleteQuest();

            qm.warp(914090010, 0); // Initialize Aran Tutorial Scenes
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}