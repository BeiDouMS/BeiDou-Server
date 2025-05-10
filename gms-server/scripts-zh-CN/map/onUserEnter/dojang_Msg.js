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
	Author: Traitor
	Map(s):	Mu Lung Dojo Entrance
	Desc:   Sends the entrance message or the taunt message from that dojo guy
*/
var messages = [
    "勇闯武陵道场，阁下好胆识！",
    "想尝尝败北的滋味？尽管放马过来！",
    "定让你后悔挑战武陵道场！速来受死！"
];

function start(ms) {
    if (ms.getPlayer().getMap().getId() === 925020000) {
        if (ms.getPlayer().getMap().findClosestPlayerSpawnpoint(ms.getPlayer().getPosition()).getId() === 0) {
            ms.getPlayer().startMapEffect(messages[(Math.random() * messages.length) | 0], 5120024);
        }
        ms.resetDojoEnergy();
    } else {
        ms.getPlayer().resetEnteredScript(); // 玩家进入道场试炼时执行的脚本// 防止断线重连时卡关，在道场传送门处重置状态
        ms.getPlayer().startMapEffect("哼！让我看看你有几斤几两！不打败我就别想离开！", 5120024);// 启动全屏特效并显示BOSS挑衅台词
    }
}
