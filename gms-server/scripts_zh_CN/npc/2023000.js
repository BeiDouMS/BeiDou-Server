/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

var toMap = [211040200, 220050300, 220000000, 240030000];
var inMap = [211000000, 220000000, 221000000, 240000000];
var cost = [10000, 25000, 25000, 65000];
var location;
var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.sendNext("嗯，请仔细考虑一下。这不便宜，但您绝对不会对我们的顶级服务感到失望！");
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            for (var i = 0; i < toMap.length; i++) {
                if (inMap[i] == cm.getPlayer().getMap().getId()) {
                    location = i;
                    break;
                }
            }
            cm.sendNext("你好！这辆出租车会比箭头飞得更快，把你带到奥西里亚的危险地方！我们从#m" + inMap[location] + "#到奥西里亚大陆上的#b#m" + toMap[location] + "##k去！费用是#b" + cost[location] + " 枚金币#k。我知道有点贵，但能避开所有危险区域，绝对物有所值！");
        } else if (status == 1) {
            cm.sendYesNo("你想支付 #b" + cost[location] + " 冒险币#k 前往 #b#m" + toMap[location] + "##k 吗？");
        } else if (status == 2) {
            if (cm.getMeso() < cost[location]) {
                cm.sendNext("你似乎没有足够的金币。非常抱歉，除非你付款，否则我无法帮助你。多打怪赚更多金币，等你有足够的金币再回来吧。");
            } else {
                cm.warp(toMap[location], location != 1 ? 0 : 1);
                cm.gainMeso(-cost[location]);
            }
            cm.dispose();
        }
    }
}