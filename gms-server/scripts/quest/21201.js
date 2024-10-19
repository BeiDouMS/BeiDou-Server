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
            qm.sendNext("First you promise to defeat the Black Mage and make me a famous weapon, then you abandon me for hundreds of years, and now you're telling me you don't remember who I am? What the...?! Do you think I will let you get away with that? You're the one who begged and pined for me!", 8);
            break;
        case 1:
            qm.sendNextPrev("I did tell #p1203000# to make a pole arm for me if I could prove my worth.", 2);
            break;
        case 2:
            qm.sendNextPrev("After all that begging, shouldn't you treat me with a little more love and respect? Ya know, a weapon like me's a rare and wonderful thing. I am the ultimate #p1201001# that can help you defeat the Black Mage. How could you ditch me for hundreds of years?", 8);
            break;
        case 3:
            qm.sendNextPrev("Hey, I never begged for you", 2);
            break;
        case 4:
            qm.sendNextPrev("What? You never begged for me? Ha! #p1203000# told me you got on your knees, begged for me in tears, and... Wait a sec. Aran! Did you just remember who I am?", 8);
            break;
        case 5:
            qm.sendNextPrev("Maybe a little bit...", 2);
            break;
        case 6:
            qm.sendNextPrev("Aran, it is you! *Sniff sniff* Wait, *ahem* I didn't get emotional, it's just allergies. I know the Black Mage has stripped you of your abilities so you probably don't even have the strength to lift me... but at least you remember me! I'm glad that your memory's starting to return.", 8);
            break;
        case 7:
            qm.sendAcceptDecline("Even though you've lost your memory, you're still my master. You endured some very tough training in the past, and I'm sure your body still remembers the skills you got through those hard times. Alright, I'll restore your abilities!");
            break;
        case 8:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("Hey! At least say you tried!");
                qm.dispose();
                break;
            }
            // ACCEPT
            if (!qm.isQuestCompleted(21201)) {
                if (!qm.canHold(1142130)) {
                    qm.sendOk("Wow, your #bequip#k inventory is full. I need you to make at least 1 empty slot to complete this quest.");   // thanks MedicOP for finding an issue here
                    return;
                }

                qm.gainItem(1142130, true);
                qm.changeJobById(2110);

                const YamlConfig = Java.type('org.gms.config.YamlConfig');
                if (YamlConfig.config.server.USE_FULL_ARAN_SKILLSET) {
                    qm.teachSkill(21100000, 0, 20, -1);   //polearm mastery
                    qm.teachSkill(21100002, 0, 30, -1);   //final charge
                    qm.teachSkill(21100004, 0, 20, -1);   //combo smash
                    qm.teachSkill(21100005, 0, 20, -1);   //combo drain
                }

                qm.completeQuest();
            }

            qm.sendOk("Your level isn't what it used to be back in your glory days, so I can't restore all of your old abilities. But the few I can restore should help you level up faster. Now hurry up and train so you can return to the old you.");
            break;
        default:
            qm.dispose();
            break;
    }
}