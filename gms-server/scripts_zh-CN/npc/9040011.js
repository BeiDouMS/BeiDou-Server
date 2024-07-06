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
    cm.sendOk("<通知> \r\n 你是属于一个拥有足够勇气和信任的公会吗？那就接受公会任务的挑战吧！\r\n\r\n#b参与条件：#k\r\n1. 公会必须至少有6名成员！\r\n2. 公会任务的领导必须是公会的会长或副会长！\r\n3. 如果参与公会任务的成员数量少于6人，或者领导决定提前结束任务，公会任务可能会提前结束！");
    cm.dispose();
}