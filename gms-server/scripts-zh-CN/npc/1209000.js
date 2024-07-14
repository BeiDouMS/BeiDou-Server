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
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 0 && type == 0) {
        status--;
    } else if (mode == -1) {
        cm.dispose();
        return;
    } else {
        status++;
    }
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        cm.sendNext("阿兰，你醒了！你感觉怎么样？嗯？你想知道发生了什么事情吗？");
    } else if (status == 1) {
        cm.sendNext("我们几乎准备好逃跑了。你不用担心。我尽可能找到的每个人都已经登上了方舟，神树也同意引领我们的道路。一旦完成剩下的准备工作，我们就会立即前往金银岛。");
    } else if (status == 2) {
        cm.sendNext("其他的英雄们？他们已经离开去对抗黑魔法师了。他们在为我们争取时间逃跑。什么？你想和他们一起战斗吗？不！你不能！你受伤了。你必须和我们一起离开！");
    } else if (status == 3) {
        //cm.setQuestProgress(21002, 1);
        cm.showIntro("Effect/Direction1.img/aranTutorial/Trio");
        cm.dispose();
    }
} 