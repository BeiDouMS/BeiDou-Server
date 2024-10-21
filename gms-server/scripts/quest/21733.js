/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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

    if (status == 0) {
        qm.sendSimple('Yo, where are you? We have a situation here! \r\n#b#L0# (Yo...? #p1002104# always called me "hero", not "yo"...)#l');
        return;
    } else if (status == 1) {
        if (mode == 0) { // END CHAT
            qm.dispose();
            return;
        }
        qm.sendAcceptDecline("I have some very important information! Quick, come to #b#m104000004##k!");
    } else {
        if (mode == 0) { // DECLINE
            qm.sendOk("What are you saying? I'm in a rush right now! Stop with the nonsense and just come over here!");
            qm.dispose();
            return;
        }
        // ACCEPT
        qm.forceStartQuest();
        qm.sendOk("It's seriously urgent! Come as quickly as possible!");
        qm.dispose();
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            qm.sendNext("Aran, thank you very much! Somehow the Puppeteer managed to bypass the security of Lith Harbor. He was trying to seek revenge because of the other day. Luckily, you came by. Nicely done!");
        } else if (status == 1) {
            qm.sendNext("I will teach you the #rPolearm Mastery#k skill, to reward your actions here. You will be able to improve your accuracy and the overall mastery of your polearm arts.");
        } else if (status == 2) {
            qm.gainExp(8000);
            qm.teachSkill(21100000, 0, 20, -1); // polearm mastery

            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}