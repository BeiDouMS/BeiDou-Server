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
	Author : Generic
	NPC Name: 		Kiku
	Map(s): 		Empress' Road : Training Forest I
	Description: 		Quest - Kiku the Training Instructor
	Quest ID : 		20002
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode > 0) {
            status++;
        } else {
            status--;
        if (status == 0)
            qm.sendNext("什么？奈哈特派你来的？啊，你一定是个新手。欢迎，欢迎。我叫基库，我的工作是训练和塑造像你这样的孩子成为真正的骑士. 额... 你为什么那样看着我。。。啊，你以前一定没见过皮尤.");
        else if (status == 1)
            qm.sendNext("我们属于一个叫皮耶斯的种族。你以前和新秀谈过，对吧？站在皇后旁边的那个。是的，新秀也是一个皮尤。他可能是另一个阶级，但是。。。哦，好吧。皮约斯只在埃雷夫发现，所以你可能会发现我们一开始有点奇怪，但你会习惯我们的.");
        else if (status == 2)
            qm.sendAcceptDecline("啊，我不知道你是否意识到了，但你不会在埃雷夫找到任何怪物。任何形式的邪恶都不能踏上这座岛。别担心，你还有机会在这里训练。新秀创造了一个叫做咪咪的幻想生物，它将被用作你的训练伙伴。我们开始吧?");
        else if (status == 3) {
            qm.forceStartQuest();
            qm.forceCompleteQuest();

            qm.gainExp(60);
            qm.gainItem(2000020, 10); // Red Potion for Noblesse * 10
            qm.gainItem(2000021, 10); // Blue Potion for Noblesse * 10
            qm.gainItem(1002869, 1);  // Elegant Noblesse Hat * 1

            qm.sendOk("哈，我喜欢你的热情，但在我们开始之前，你必须先为训练做好准备。确保你装备了武器，并且你的技能已经校准并准备好使用。我也给了你一些药水，所以准备好以防万一。准备好了就告诉我.");
        } else if (status == 4) {
            qm.dispose();
        }
    }
}