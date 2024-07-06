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
status = -1;
//Need more questions.
quest = ["Which of these NPC's will you NOT see at Ellinia of Victoria Island#b\r\n#L0#Shane\r\n#L1#Francois\r\n#L2#Grendel the Really Old\r\n#L3#Arwen the Fairy\r\n#L4#Roel", "Which of these monsters will you NOT be facing at Ossyria...?#b\r\n#L0#White Fang\r\n#L1#Croco\r\n#L2#Yeti\r\n#L3#Lycanthrope\r\n#L4#Luster Pixie", "Which of these monsters have the highest level...?#b\r\n#L0#Octopus\r\n#L1#Ribbon Pig\r\n#L2#Green Mushroom\r\n#L3#Axe Stump\r\n#L4#Bubbling", "In MapleStory, which of these pairings of potion/results doesn't match...?#b\r\n#L0#Holy Water - Recover from the state of being cursed or sealed up.\r\n#L1#Sunrise Dew - Recover MP 3000\r\n#L2#Hamburger - Recover HP 400\r\n#L3#Salad - Recover MP 200\r\n#L4#Blue Potion - Recover MP 100", "Which of these NPC's have NOTHING to do with pets...?#b\r\n#L0#Cloy\r\n#L1#Mar the Fairy\r\n#L2#Trainer Frod\r\n#L3#Vicious\r\n#L4#Doofus"];
ans = [4, 1, 3, 1, 3];
rand = parseInt(Math.random() * quest.length);

function start() {
    if (cm.getPlayer().gotPartyQuestItem("JBQ") && !cm.haveItem(4031058)) {
        if (cm.haveItem(4005004)) {
            if (!cm.canHold(4031058)) {
                cm.sendNext("接受此试炼前，请确保有一个空闲的ETC槽位。");
            } else {
                cm.sendNext("好的...我将在这里测试你的智慧。回答所有问题正确，你就会通过测试，但是，如果你有一次说谎，那么你就得重新开始，好吗，我们开始吧。");
                return;
            }
        } else {
            cm.sendNext("给我一个 #b#t4005004##k 以便继续问题。");
        }
    }
    cm.dispose();
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.gainItem(4005004, -1);
    }
    if (status > 0) {
        if (selection != ans[rand]) {
            cm.sendNext("你已经失败了这个问题。");
            cm.dispose();
            return;
        }
    }
    while (quest[rand] === "" && status <= 4) {
        rand = parseInt(Math.random() * quest.length);
    }
    if (status <= 4) {
        cm.sendSimple("这是第" + (status + 1) + (status == 0 ? "st" : status == 1 ? "nd" : status == 2 ? "rd" : "th") + "个问题。" + quest[rand]);
        quest[rand] = "";
    } else {
        cm.sendOk("好的。你的所有答案都被证明是真实的。你的智慧得到了验证。拿着这条项链回去吧。");
        cm.gainItem(4031058, 1);
        cm.dispose();
    }
}