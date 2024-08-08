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
var status = 0;
var price = 1000000;
var skin = Array(0, 1, 2, 3, 4);

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
            cm.sendSimple("哦，你好！欢迎来到鲁塔比姆美容中心！你有兴趣晒黑变性感吗？或者想要拥有美丽雪白的肌肤？如果你有#b#t5153002##k，你可以让我们来照顾剩下的事情，拥有你一直梦寐以求的肌肤！\r\n#L2#美容护肤：#i5153002##t5153002##l");
        } else if (status == 1) {
            if (selection == 2) {
                cm.sendStyle("通过我们的专业机器，你可以在手术前看到自己在治疗后的样子。你想要什么样的表情？快来选择你喜欢的风格吧～！", skin);
            }
        } else if (status == 2) {
            cm.dispose();
            if (cm.haveItem(5153002) == true) {
                cm.gainItem(5153002, -1);
                cm.setSkin(skin[selection]);
                cm.sendOk("享受你的新肤色吧！");
            } else {
                cm.sendOk("嗯...您没有需要接受护肤折扣的券。抱歉，恐怕我们不能为您服务...");
            }
        }
    }
}