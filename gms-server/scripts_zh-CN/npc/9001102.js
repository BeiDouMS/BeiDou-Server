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
/**
 * @Author : iAkira, Kevintjuh93
 **/
var status = 0;
var selected = 0;

function start() {
    if (cm.getPlayer().getMapId() == 100000000) {
        cm.sendNext("看那儿！你看到了吗？没有？一个飞碟刚刚飞过去了……那边！！看，有人被拖进了飞碟……啊啊啊啊，是嘎嘎！#r嘎嘎刚刚被飞碟绑架了！#k");
    }
}

function action(m, t, s) {
    if (m > 0) {
        status++;
        if (cm.getPlayer().getMapId() == 100000000) { // warper completed
            if (status == 1) {
                if (cm.getPlayer().getLevel() >= 12) {
                    cm.sendYesNo("我们现在该怎么办？这只是一个谣言，但是……我听说如果你被外星人绑架，会发生可怕的事情……或许这就是现在发生在Gaga身上的事情！请，请救救Gaga！#bGaga可能有点不确定和迷茫，但#k他的心真的很好。我不能让可怕的事情发生在他身上。对了！月球上的爷爷可能知道如何救他！我会送你去月球，所以请去见爷爷，救出Gaga！！！");
                } else {
                    cm.sendOk("哦！看来你还没有达到救出嘎嘎的等级要求。请在达到12级或更高级时再回来。");
                }

            } else if (status == 2) {
                cm.sendNext("非常感谢。请救救嘎嘎！月球上的爷爷会帮助你。");
            } else if (status == 3) {
                cm.warp(922240200, 0);
                cm.dispose();
            }
        }
    } else if (m < 1) {
        cm.dispose();
    }
}