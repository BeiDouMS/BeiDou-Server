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
/* 9000021 - Gaga
    BossRushPQ recruiter
    @author Ronan
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {
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
            cm.sendNext("嘿，旅行者！我是#p9000021#，我的工作是招募像你这样渴望每天迎接新挑战的旅行者。现在，我的团队正在举办比赛，充分测试像你这样的冒险者的心理和身体能力。");
        } else if (status == 1) {
            cm.sendNext("这些比赛涉及#b连续的boss战#k，其中一些部分之间有一些休息点。这将需要一些策略时间和足够的物资在手，因为它们不是普通的战斗。");
        } else if (status == 2) {
            cm.sendAcceptDecline("如果你觉得自己足够强大，你可以像其他人一样在我们举办权力竞赛的地方加入。...那么，你的决定是什么？您现在进入举行比赛的地方吗？");
        } else if (status == 3) {
            cm.sendOk("非常好。记住，在那里你可以组建一个团队，或者独自进行战斗，这取决于你。祝你好运！");
        } else if (status == 4) {
            cm.getPlayer().saveLocation("BOSSPQ");
            cm.warp(970030000, "out00");
            cm.dispose();
        }
    }
}