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
/* The Glimmer Man
	Amoria PQ Stg1/exit
 */

var status;
var curMap, stage;

function start() {
    curMap = cm.getMapId();
    stage = Math.floor((curMap - 670010200) / 100) + 1;

    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (cm.getMapId() != 670010200) {
                cm.sendYesNo("那么，你打算离开这个地方吗？");
            } else {
                if (cm.isEventLeader()) {
                    var eim = cm.getEventInstance();
                    var st = eim.getIntProperty("statusStg" + stage);

                    if (cm.haveItem(4031595, 1)) {
                        cm.gainItem(4031595, -1);
                        eim.setIntProperty("statusStg" + stage, 1);

                        cm.sendOk("你已经找到了#t4031595#，太棒了！你可以向阿莫斯报告你在这个任务中的成功。");
                    } else if (st < 1 && cm.getMap().countMonsters() == 0) {
                        eim.setIntProperty("statusStg" + stage, 1);

                        var mapObj = cm.getMap();
                        mapObj.toggleDrops();

                        const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
                        const Point = Java.type('java.awt.Point');
                        var mobObj = LifeFactory.getMonster(9400518);
                        mapObj.spawnMonsterOnGroundBelow(mobObj, new Point(-245, 810));

                        cm.sendOk("烈焰魔出现了！打败它就能获得#b#t4031596##k！");
                    } else {
                        if (st < 1) {
                            cm.sendOk("你的任务是恢复魔镜的碎片。为此，你需要一枚#b#t4031596##k，这枚物品会在其他怪物全部被杀死后出现的火焰怪身上掉落。要进入怪物所在的房间，选择与你性别对应的传送门，然后消灭那里的所有怪物。女士们走左边，先生们走右边。");
                        } else {
                            cm.sendOk("你的任务是找回魔镜碎片。打败火焰精灵就能得到#b#t4031596##k。");
                        }
                    }
                } else {
                    cm.sendOk("你的任务是找回魔镜的碎片。为此，你需要一枚#b#t4031596##k，这个物品会在其他怪物全部被消灭后出现的火焰怪身上掉落。要进入怪物所在的房间，选择与你性别对应的传送门，然后消灭那里的所有怪物。女士们走左边，先生们走右边。#b你们的领袖#k必须携带#b#t4031595##k才能获得我的通行证。");
                }

                cm.dispose();
            }
        } else if (status == 1) {
            cm.warp(670010000, "st00");
            cm.dispose();
        }
    }
}