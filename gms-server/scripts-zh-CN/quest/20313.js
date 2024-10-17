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
	NPC Name: 		Irena (1101005)
	Description: 	Wind Archer 3rd job advancement
	Quest: 			Shinsoo's Teardrop

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

function start(mode, type, selection) {
    if(QuestID == null) {
        QuestID = qm.getQuest();
        jobId = String(QuestID).slice(-1);  //通过任务ID获取职业ID
        jobLevel = String(QuestID).substring(2, 3);  //通过任务ID获取几转
        chrLevel = chrLevel[jobLevel];               //通过转职次数绑定等级限制

        job = Java.type('org.gms.client.Job')[job[jobId] + jobLevel];   //获取转职职业类
        medalid += (jobLevel - 1);    //绑定对应的转职勋章
        completeQuestID += (jobLevel - 1);    //绑定完成对应给予转职勋章的任务
    }
    if (mode == -1) {
        qm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            qm.sendNext("我猜你还没准备好.");
            qm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            qm.sendNext("你所带回来的宝石是神兽的眼泪，它拥有非常强大的力量。如果被黑磨法师给得手了，那我们全部都可能要倒大楣了...\r\n");
        } else if (status == 1) {
            qm.sendYesNo("女皇为了报答你的努力，将任命你为皇家骑士团的#b高级骑士 - " + job.getName() + "#k，你准备好了嘛?");
        } else if (status == 2) {
            nPSP = (qm.getPlayer().getLevel() - chrLevel) * 3;
            if (qm.getPlayer().getRemainingSp() > nPSP) {
                qm.sendNext("请检查你的技能点数是否已经加完。");
            } else {
                if (!qm.canHold(medalid)) {
                    qm.sendOk(`女皇将赋予你#b#v${medalid}##t${medalid}##k，你必须将#b装备栏#k#r空出1个格子#k才可以接受。\r\n\r\n如果你已拥有该勋章，请你将其丢弃。`);
                } else {
                    qm.getPlayer().changeJob(job);  //更改职业
                    qm.completeQuest();
                    qm.gainItem(medalid, 1); //原始流程是女皇任务给的勋章，需要配合WZ给任务29908加入自动完成任务代码,比较麻烦，在这里给了。
                    qm.completeQuest(completeQuestID); //直接完成女皇的任务，这样女皇头顶不会一直顶着书本。
                    qm.sendNext(`从这一刻起，女皇任命你为高级骑士。请继续努力，成为一名享受更艰难的冒险过程的#b高级骑士#k吧！\r\n获得女皇赋予的勋章：\r\n#b#v${medalid}##t${medalid}##k`);
                }
            }
        } else if (status == 3) {
            qm.dispose();
        }
    }
}