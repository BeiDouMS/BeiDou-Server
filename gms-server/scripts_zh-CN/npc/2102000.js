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

function start() {
    if (cm.haveItem(4031045)) {
        var em = cm.getEventManager("Genie");
        if (em.getProperty("entry") == "true") {
            cm.sendYesNo("这将不是一次短途飞行，所以你需要先处理一些事情，我建议你在登机前先做好这些。你还想要登上魔灯吗？");
        } else {
            cm.sendOk("这个精灵正在准备起飞。很抱歉，你将不得不等下一班车。乘车时间表可通过售票亭的导游获取。");
            cm.dispose();
        }
    } else {
        cm.sendOk("确保你有阿里安特的船票才能在这个魔灯中旅行。检查你的背包。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode <= 0) {
        cm.sendOk("好的，如果你改变主意，就跟我说话！");
        cm.dispose();
        return;
    }

    var em = cm.getEventManager("Genie");
    if (em.getProperty("entry") == "true") {
        cm.warp(260000110);
        cm.gainItem(4031045, -1);
    } else {
        cm.sendOk("这个精灵正在准备起飞。很抱歉，你将不得不等下一班车。乘车时间表可通过售票亭的导游获取。");
    }

    cm.dispose();
}