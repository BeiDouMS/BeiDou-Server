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
/* Ms. Tan
	Henesys Skin Change.
*/
var status;
var skin = Array(0, 1, 2, 3, 4);
var price = 1000000;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {  // disposing issue with stylishs found thanks to Vcoc
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }


        if (status == 0) {
            cm.sendSimple("嗨，你好！欢迎来到明斯皮肤护理中心！你想要像我一样拥有紧致健康的皮肤吗？使用 #b#t5153000##k，你可以让我们来照顾剩下的事情，拥有你一直想要的肌肤~！\r\n#L1#皮肤护理：#i5153000##t5153000##l");
        } else if (status == 1) {
            if (cm.haveItem(5153000)) {
                cm.sendStyle("使用我们的专业机器，您可以提前看到治疗后的自己。您想做什么样的皮肤护理？选择您喜欢的风格。", skin);
            } else {
                cm.sendOk("嗯...你没有你需要接受治疗的护肤券。抱歉，恐怕我们不能为你做这个...");
                cm.dispose();

            }
        } else {
            cm.gainItem(5153000, -1);
            cm.setSkin(selection);
            cm.sendOk("享受你的新肤色吧！");
            cm.dispose();
        }
    }
}