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

/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Kerny - Pilot
 -- By ---------------------------------------------------------------------------------------------
 Information
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Information
 2.0 - Second Version by Jayd
 ---------------------------------------------------------------------------------------------------
 **/
var k2s;
var airport;
var s2k;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }
    if (mode == 1) {
        status++;
    }
    if (mode == 0) {
        if (cm.getMapId() == 540010101) {
            cm.sendOk("请稍等一下，我们即将到达新加坡！谢谢您的耐心等待。");
            cm.dispose();
            return;
        } else {
            cm.sendOk("请稍等一下，我们即将到达废弃都市！感谢您的耐心。");
            cm.dispose();
            return;
        }
    }
    if (status == 0) {
        if (cm.getMapId() == 540010001) {
            cm.sendYesNo("飞机马上就要起飞了，你确定要现在离开吗？机票是不可退的。");
            airport = 1;
        } else if (cm.getMapId() == 540010002) {
            cm.sendOk("我们马上就要到达昆明市，请坐下等一会儿。");
            cm.dispose();
            s2k = 1;
        } else if (cm.getMapId() == 540010101) {
            cm.sendOk("我们马上就要到新加坡了，请坐下等候。");
            cm.dispose();
            k2s = 1;
        }
    } else if (status == 1) {
        if (k2s == 1) {
            cm.warp(103000000);
            cm.sendOk("希望很快再见到你！");
            cm.dispose();
        } else if (airport == 1) {
            cm.warp(540010000);
            cm.sendOk("希望很快再见到你！");
            cm.dispose();
        } else if (s2k == 1) {
            cm.warp(540010000);
            cm.sendOk("希望很快再见到你！");
            cm.dispose();
        }
    }
}