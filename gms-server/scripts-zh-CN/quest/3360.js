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
	NPC Name: 		Parwen
	Description: 		Quest - Verifying the password
*/
var status = -1;
var pass;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            qm.sendNext("快点，快点。如果你不够聪明，就拿出笔和纸来吧！");
            qm.dispose();
            return;
        }

        if (status == 0) {
            qm.sendNext("哦！终于来了！很高兴你来得及时。我有给你打开秘密通道的主钥匙！哈哈哈哈！是不是很惊奇？说出来很惊奇吧！");
        } else if (status == 1) {
            qm.sendAcceptDecline("好了，现在，这个钥匙非常长且复杂。我需要你牢牢记住它。我不会再重复，所以最好把它写在某个地方。你准备好了吗？");
        } else if (status == 2) {
            pass = generateString();
            qm.sendOk("钥匙代码是 #b" + pass + "#k。记住了吗？把钥匙插入秘密通道的门上，你就可以自由地走进通道了。");
        } else if (status == 3) {
            qm.forceStartQuest();
            qm.setQuestProgress(3360, pass);
            qm.dispose();
        }
    }
}


function generateString() {
    var thestring = "";
    var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    var rnum;
    for (var i = 0; i < 10; i++) {
        rnum = Math.floor(Math.random() * chars.length);
        thestring += chars.substring(rnum, rnum + 1);
    }
    return thestring;
}
