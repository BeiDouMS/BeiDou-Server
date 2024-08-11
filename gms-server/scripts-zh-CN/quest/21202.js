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

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0 && type == 1) {
            qm.sendNext("“你不想努力工作以获得终极武器吗？”");
        }
        qm.dispose();
        return;
    }
    if (status == 0) {
        qm.sendNext("“嗯..像你这样年轻的人在这个偏僻的地方做什么？”");
    } else if (status == 1) {
        qm.sendNextPrev("“我来拿最好的长柄武器！”", 2);
    } else if (status == 2) {
        qm.sendNextPrev("最好的长柄武器？你应该能在某个城镇或其他地方购买到它..");
    } else if (status == 3) {
        qm.sendNextPrev("我听说你是枫叶世界里最好的铁匠！我只想要一件你打造的武器！", 2);
    } else if (status == 4) {
        qm.sendAcceptDecline("“我现在太老了，不能再制造武器了，但.. 我确实有一把很久以前做的长柄武器。它现在仍然状况良好。但我不能给你，因为那把长柄武器非常锋利，锋利到甚至会伤害它的主人。你还想要吗？”");
    } else if (status == 5) {
        qm.sendOk("“好吧，如果你这么说.. 我无法反对。我告诉你。我会给你一个快速测试，如果你通过了，巨型长柄武器就是你的。前往#b训练中心#k并挑战那里的#r伤疤熊#k。你的任务是带回#b30个接受标志#k。”");
    } else if (status == 6) {
        qm.startQuest();
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0 && type == 1) {
            qm.sendNext("“嗯？经历了这么多之后你现在还犹豫不决吗？好吧，如果你愿意的话再好好考虑考虑。反正最后它会是你的。”");
        }
        qm.dispose();
        return;
    }
    if (status == 0) {
        if (qm.haveItem(4032311, 30)) {
            qm.sendNext("“哦，你把#t4032311#带来了吗？你比我想象的要强大！但更重要的是，我对你在毫不犹豫地接下这件危险武器时所表现出的勇气印象深刻。你值得拥有它。#p1201001#是你的了。”");
        } else {
            qm.sendNext("去选择30 #t4032311#。");
            qm.dispose();
        }
    } else if (status == 1) {
        qm.sendNextPrev("#b(过了很长时间，#p1203000#把小心用布包着的#p1201001#递给了你。)");
    } else if (status == 2) {
        qm.sendYesNo("这是你要的长枪，编号#p1201002#。请好好保管。");
    } else if (status == 3) {
        //qm.showVideo("Polearm");
        qm.completeQuest();
        qm.removeAll(4032311);
        qm.dispose();
    }
}