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
/* Rebirth NPC
    @author Ronan
    @author wejrox
*/
var status;
var jobId = 0;

function start() {
    status = -1;
    const YamlConfig = Java.type('org.gms.config.YamlConfig');
    if (!YamlConfig.config.server.USE_REBIRTH_SYSTEM) {
        cm.sendOk("转生在这个服务器上是不允许的，你是怎么到这里来的？");
        cm.dispose();
        return;
    }
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode === 1) {
        status++;
    } else {
        cm.dispose();
        return;
    }
    if (status === 0) {
        cm.sendNext("当你想要再次重生时，来找我吧。你目前总共有 #r" + cm.getChar().getReborns() + " #krebirths。");
    } else if (status === 1) {
        cm.sendSimple("你今天想让我做什么呢：\r\n\r\n#L0##b我想转生！#l\r\n#L1##b现在什么都不想做...#k#l");
    } else if (status === 2) {
        if (selection === 0) {
            if (cm.getChar().getLevel() === cm.getChar().getMaxClassLevel()) {
                cm.sendSimple("我明白了... 你想选择哪条路？\r\n\r\n#L0##b探险家（初心者）#l\r\n#L1##b天鹅骑士团（贵族）#l\r\n#L2##b阿兰（传说）#l");
            } else {
                cm.sendOk("看起来你的冒险之旅还没有结束……当你达到等级 " + cm.getChar().getMaxClassLevel() +"时再回来吧。");
                cm.dispose();
            }
        } else if (selection === 1) {
            cm.sendOk("See you soon!")
            cm.dispose();
        }
    } else if (status === 3) {
        // 0 => beginner, 1000 => noblesse, 2000 => legend
        // makes this very easy :-)
        jobId = selection * 1000;

        var job = "";
        if (selection === 0) job = "Beginner";
        else if (selection === 1) job = "Noblesse";
        else if (selection === 2) job = "Legend";
        cm.sendYesNo("你确定要转职成为一个" + 职业 + "吗？");
    }
    else if (status === 4 && type === 1) {
        cm.getChar().executeRebornAsId(jobId);
        cm.sendOk("你现在已经重生了。总共 #r" + cm.getChar().getReborns() + "#k 次重生。");
        cm.dispose();
    }
}