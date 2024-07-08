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
var party;
var preamble;
var mobcount;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else if (mode == 0) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        var eim = cm.getPlayer().getEventInstance();
        var nthtext = "last";
        if (status == 0) {
            party = eim.getPlayers();
            preamble = eim.getProperty("leader" + nthtext + "preamble");
            mobcount = eim.getProperty("leader" + nthtext + "mobcount");
            if (preamble == null) {
                cm.sendOk("嗨。欢迎来到第" + nthtext + "阶段。这是你与#bboss#k战斗的地方。我们开始吧？");
                status = 9;
            } else {
                if (!isLeader()) {
                    if (mobcount == null) {
                        cm.sendOk("请告诉你的#b队长#k来找我谈话。");
                        cm.dispose();
                    } else {
                        cm.warp(109020001, 0);
                        cm.dispose();
                    }
                }
                if (mobcount == null) {
                    cm.sendYesNo("你完了？！");
                }
            }
        } else if (status == 1) {
            //if (cm.mobCount(600010000)==0) {
            if (cm.mapMobCount() == 0) {
                cm.sendOk("干得好！你干掉了他们！");
            } else {
                cm.sendOk("你在说什么？杀死那些怪物！");
                cm.dispose();
            }
        } else if (status == 2) {
            cm.sendOk("你可以继续到下一阶段！");
        } else if (status == 3) {
            cm.clear();
            eim.setProperty("leader" + nthtext + "mobcount", "done");
            var map = eim.getMapInstance(109020001);
            var members = eim.getPlayers();
            cm.warpMembers(map, members);
            cm.givePartyExp(2500, eim.getPlayers());
            cm.dispose();
        } else if (status == 10) {
            eim.setProperty("leader" + nthtext + "preamble", "done");
//            cm.summonMobAtPosition(8220000,25000000,1500000,1,-762,-1307);
//            cm.summonMobAtPosition(8220001,15000000,750000,1,-788,-851);
//            cm.summonMobAtPosition(9410015,15000000,750000,1,128,-851);
            cm.dispose();
        }
    }
}


function isLeader() {
    if (cm.getParty() == null) {
        return false;
    } else {
        return cm.isLeader();
    }
}