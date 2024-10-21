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
	NPC Name: 		Eckhart
	Map(s): 		Empress' Road : Ereve (130000000)
	Description: 		Quest - Knighthood Exam: Night Walker
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
            qm.sendNext("我猜你还没准备好?");
            qm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            qm.sendYesNo(`既然你带来了所有的#b#v${ProofofExamID}##t${ProofofExamID}##k，那我现在相信你有资格成为#b正式骑士 - ${job.getName()}#k，你想成为其中的一员吗？`);
        } else if (status == 1) {
            if (qm.getPlayer().getJob().getId() == (1000+jobId*100) && qm.getPlayer().getRemainingSp() > ((qm.getPlayer().getLevel() - chrLevel) * 3)) {
                qm.sendNext("你还有技能点没有使用完，所以你还不能成为正式的骑士！在一转技能上使用更多的SP.");
                qm.dispose();
            } else {
                if (qm.getPlayer().getJob().getId() != (1010+jobId*100)) {
                    if (!qm.canHold(medalid)) {
                        qm.sendOk(`女皇将赋予你#b#v${medalid}##t${medalid}##k，你必须将#b装备栏#k#r空出1个格子#k才可以接受。\r\n\r\n如果你已拥有该勋章，请你将其丢弃。`);
                        qm.dispose();
                        return;
                    }
                    qm.gainItem(ProofofExamID, -30);
                    qm.getPlayer().changeJob(job);
                    qm.completeQuest();
                    qm.gainItem(medalid, 1); //原始流程是女皇任务给的勋章，需要配合WZ给任务加入自动完成任务代码,比较麻烦，在这里给了。
                    qm.completeQuest(completeQuestID); //直接完成女皇的任务，这样女皇头顶不会一直顶着书本。
                }
                qm.sendNext(`训练已经结束。你现在皇家骑士团的骑士官员.\r\n获得女皇赋予的勋章：\r\n#b#v${medalid}##t${medalid}##k`);
            }
        } else if (status == 2) {
            qm.sendNextPrev("我给了你一些#b技能点#k。我还传授了你一些只有骑士才能掌握的灵魂大师的技能，所以我希望你能努力钻研，并尽可能地培养它们，就像培养你的灵魂一样。");
        } else if (status == 3) {
            qm.sendPrev("既然你已经正式成为皇家骑士，那就要以骑士的标准行事，这样才能维护女皇的威名。");
        } else if (status == 4) {
            qm.dispose();
        }
    }
}