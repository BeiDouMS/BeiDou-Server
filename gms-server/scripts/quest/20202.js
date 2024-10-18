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
/* Author: Xterminator 
	NPC Name: 		Oz
	Map(s): 		Empress' Road : Ereve (130000000)
	Description: 		Quest - Knighthood Exam: Blaze Wizard
*/
/*
    Author:         Magical-H
    Description:    骑士团转职通用脚本
 */
var job = {
    1 : "DAWNWARRIOR",				// 魂骑士
    2 : "BLAZEWIZARD",				// 炎术士
    3 : "WINDARCHER",				// 风灵使者
    4 : "NIGHTWALKER",				// 夜行者
    5 : "THUNDERBREAKER"			// 奇袭者
};
var medalid = 1142066;             //定义初级勋章，根据转职次数自动发放对应的勋章
var completeQuestID = 29906;        //定义初级勋章任务，根据转职次数自动完成对应的勋章任务
var ProofofExamID = 4032096;          //定义考试的证物，根据职业自动设为对应的ID
var jobId = null;
var jobLevel = null;
var QuestID = null;

var chrLevel = {
    1 : 10,
    2 : 30,
    3 : 70,
    4 : 120
};                  //定义角色等级限制
var status = -1;

function end(mode, type, selection) {
    if(QuestID == null) {
        QuestID = qm.getQuest();
        jobId = String(QuestID).slice(-1);  //通过任务ID获取职业ID
        jobLevel = String(QuestID).substring(2, 3);  //通过任务ID获取几转
        chrLevel = chrLevel[jobLevel];               //通过转职次数绑定等级限制

        job = Java.type('org.gms.client.Job')[job[jobId] + jobLevel];   //获取转职职业类
        medalid += (jobLevel - 1);    //绑定对应的转职勋章
        completeQuestID += (jobLevel - 1);    //绑定完成对应给予转职勋章的任务
        ProofofExamID += (jobId - 1);        //绑定对应的考试证物
    }
    if (mode == -1) {
        qm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            qm.sendNext("I guess you are not ready to tackle on the responsibilities of an official knight.");
            qm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            qm.sendYesNo(`So you brought all of #t${ProofofExamID}#... Okay, I believe that your are now qualified to become an official knight. Do you want to become one?\r\n\r\n#b ${job.getName()}#k?`);
        } else if (status == 1) {
            if (qm.getPlayer().getJob().getId() == (1000+jobId*100) && qm.getPlayer().getRemainingSp() > ((qm.getPlayer().getLevel() - chrLevel) * 3)) {
                qm.sendNext("You have too much #bSP#k with you. Use some more on the 1st-level skill.");
                qm.dispose();
            } else {
                if (qm.getPlayer().getJob().getId() != (1010+jobId*100)) {
                    if (!qm.canHold(medalid)) {
                        qm.sendOk(`The Queen will bestow upon you #b#v${medicaled}##t${medicaled}##k, and you must leave one space #k in the #b equipment bar #k# r to accept it. \r\n\r\nIf you already possess this medal, please discard it.`);
                        qm.dispose();
                        return;
                    }
                    qm.gainItem(ProofofExamID, -30);
                    qm.getPlayer().changeJob(job);
                    qm.completeQuest();
                    qm.gainItem(medalid, 1); //The original process was a medal given by the Empress's mission, which required cooperation with WZ to add automatic task completion codes to the mission. It was quite troublesome, but I have provided it here.
                    qm.completeQuest(completeQuestID); //Complete the queen's task directly, so that she won't have a book on her head all the time.
                }
                qm.sendNext(`You are a Knight-in-Training no more. You are now an official knight of the Cygnus Knights.\r\nReceived the medal bestowed by the Queen:\r\n#b#v${medalid}##t${medalid}##k`);
            }
        } else if (status == 2) {
            qm.sendNextPrev("I have given you some #bSP#k. I have also given you a number of skills for a Dawn Warrior that's only available to knights, so I want you to work on it and hopefully cultivate it as much as your soul.");
        } else if (status == 3) {
            qm.sendPrev("Now that you are officially a Cygnus Knight, act like one so you will keep the Empress's name up high.");
        } else if (status == 4) {
            qm.dispose();
        }
    }
}