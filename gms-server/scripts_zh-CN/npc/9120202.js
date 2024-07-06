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
/* Konpei
 * 
 * @Author Ronan
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        var eim = cm.getEventInstance();
        if (!eim.isEventCleared()) {
            if (status == 0) {
                cm.sendYesNo("如果你现在离开，就无法返回了。你确定要离开吗？");
            } else if (status == 1) {
                cm.warp(801040004, 1);
                cm.dispose();
            }
        } else {
            if (status == 0) {
                cm.sendNext("你们做到了，干得漂亮！现在我们的城市摆脱了那些暴徒的暴政！作为城市的代表，请接受这份奖励，作为对你们努力的认可，我会把你们带回城里。");
            }

            if (status == 1) {
                var eim = cm.getEventInstance();
                if (!eim.giveEventReward(cm.getPlayer())) {
                    cm.sendNext("请先在您的物品栏腾出空间…");
                } else {
                    cm.warp(801040101);
                }

                cm.dispose();
            }
        }
    }
}