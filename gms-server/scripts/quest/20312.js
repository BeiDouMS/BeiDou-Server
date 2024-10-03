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
/* Author: 		ThreeStep
	NPC Name: 		Oz (1101004)
	Description: 	Blaze Wizard 3rd job advancement
	Quest: 			Shinsoo's Teardrop
*/
/*
    Author:         Magical-H
    Description:    Swan Knights be current job advancement
*/
var job = {
    1 : "DAWNWARRIOR",				// 魂骑士
    2 : "BLAZEWIZARD",				// 炎术士
    3 : "WINDARCHER",				// 风灵使者
    4 : "NIGHTWALKER",				// 夜行者
    5 : "THUNDERBREAKER"			// 奇袭者
};
var medalid = 1142066;             //Define junior medals and automatically issue corresponding medals based on the number of job transfers
var completeQuestID = 29906;        //Define primary medal tasks and automatically complete corresponding medal tasks based on the number of job transfers
var jobId = null;
var jobLevel = null;
var QuestID = null;

var chrLevel = {
    1 : 10,
    2 : 30,
    3 : 70,
    4 : 120
};                  //Define character level restrictions

var status = -1;

function start(mode, type, selection) {
    if(QuestID == null) {
        QuestID = qm.getQuest();
        jobId = String(QuestID).slice(-1);  //Obtain occupation ID through task ID
        jobLevel = String(QuestID).substring(2, 3);  //Obtain several rotations through task ID
        chrLevel = chrLevel[jobLevel];               //Bind level restrictions through the number of job transfers

        job = Java.type('org.gms.client.Job')[job[jobId] + jobLevel];   //Obtain job transition occupational categories
        medalid += (jobLevel - 1);    //Bind the corresponding transfer medal
        completeQuestID += (jobLevel - 1);    //Bind and complete the task of awarding the corresponding transfer medal
    }
    if (mode == -1) {
        qm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            qm.sendNext("Come back when you are ready.");
            qm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            qm.sendNext("The jewel you brought back from the Master of Disguise is Shinsoo's Teardrop. It is the crystalization of Shinsoo's powers. If the Black Mage gets his hands on this, then this spells doom for all of us.");
        } else if (status == 1) {
            qm.sendYesNo("For your effort in preventing a potentially serious disaster, the Empress has decided to present you with a new title. Are you ready to accept it?");
        } else if (status == 2) {
            nPSP = (qm.getPlayer().getLevel() - chrLevel) * 3;
            if (qm.getPlayer().getRemainingSp() > nPSP) {
                qm.sendNext("You still have way too much #bSP#k with you. You can't earn a new title like that, I strongly urge you to use more SP on your 1st and 2nd level skills.");
            } else {
                if (!qm.canHold(medalid)) {
                    qm.sendOk("If you wish to receive the medal befitting the title, you may want to make some room in your equipment inventory.");
                } else {
                    qm.completeQuest();
                    qm.gainItem(medalid, 1); //The original process was a medal given by the Queen's task, and it was necessary to cooperate with WZ to add an auto completion task code to task 29908, which was quite troublesome. I have provided it here.
                    qm.completeQuest(completeQuestID); //Complete the queen's task directly, so that she won't have a book on her head all the time.
                    qm.getPlayer().changeJob(job);  //changeJob
                    qm.sendNext("#h #, as of this moment, you are an Advanced Knight. From this moment on, you shall carry yourself with dignity and respect befitting your new title, an Advanced Knight of Cygnus Knights. May your glory continue to shine as bright as this moment.");
                }
            }
        } else if (status == 3) {
            qm.dispose();
        }
    }
}