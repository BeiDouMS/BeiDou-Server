/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            const GameConfig = Java.type('org.gms.config.GameConfig');
            if (GameConfig.getServerBoolean("use_enable_custom_npc_script")) {
                cm.openShopNPC(2082014);
            } else if (cm.isQuestStarted(3749)) {
                cm.sendOk("我们已经找到了敌人的终极武器！沿着船头区域前进，你会找到我的妹妹 #b#p2082013##k。向她报告，获取有关任务的进一步指示。");
            } else {
                cm.sendDefault();
            }

            cm.dispose();
        }
    }
}