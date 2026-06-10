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
/*
-- HeavenMS 083 Script ----------------------------------------------------------------------------
    NPC - Simon
-- By ---------------------------------------------------------------------------------------------
    Jayd
-- Version Info -----------------------------------------------------------------------------------
    1.0 - First Version by Jayd
---------------------------------------------------------------------------------------------------
 */

var status;
// 西蒙负责在幸福村和沙龙神殿之间传送玩家。
var SHALOM_TEMPLE = 681000000;
var HAPPYVILLE = 209000000;
var toShalomTemple, backToHappyville;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            cm.sendNext("如果你改变主意了，随时再来找我。");
            cm.dispose();
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (cm.getMapId() == HAPPYVILLE) {
                toShalomTemple = 1;
                cm.sendYesNo("沙龙神殿和幸福村的其他地方不太一样，你想前往 #b沙龙神殿#k 吗？");
            } else if (cm.getMapId() == SHALOM_TEMPLE) {
                backToHappyville = 1;
                cm.sendYesNo("你想返回 #b幸福村#k 吗？");
            }
        } else if (status == 1) {
            if (toShalomTemple == 1) {
                cm.warp(SHALOM_TEMPLE, 0);
                cm.dispose();
            } else if (backToHappyville == 1) {
                cm.warp(HAPPYVILLE, 0);
                cm.dispose();
            }
        }
    }
}
