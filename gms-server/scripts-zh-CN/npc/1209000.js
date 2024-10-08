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

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }

    if (type == 0 && mode == 0) {
        status--;
    } else {
        status++;
    }

    if (status == 0) {
        cm.sendNext("现在才醒？战神？伤口还好吧？……什么？现在的状况？");
    } else if (status == 1) {
        cm.sendNextPrev("避难准备都做好了，所有的人都上了方舟。避难船飞行的时候就只有听天由命了，没啥可担心的。准备得差不多就该向金银岛出发了。");
    } else if (status == 2) {
        cm.sendNextPrev("战神的同伴们？他们……已经去找黑魔法师了。在我们避难的时候，他们打算阻止黑魔法师的进攻……什么？你也要去找黑魔法师？不行！你伤得太重，跟我们一起吧！");
    } else if (status == 3) {
        cm.setQuestProgress(21000, 21002, 1);
        cm.showIntro("Effect/Direction1.img/aranTutorial/Trio");
        cm.dispose();
    }
} 