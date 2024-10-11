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
/**
 * @author: Ronan
 * @npc: Pio
 * @func: Gachapon Loot Announcer
 */

var status;
var lootNames;
var lootIds;

function start() {
    const Gachapon = Java.type('org.gms.server.gachapon.Gachapon');
    lootNames = Gachapon.GachaponType.getLootNames();
    lootIds = Gachapon.GachaponType.getLootIds();

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
            var sendStr = "Hi, #r#p" + cm.getNpc() + "##k here! I'm announcing all obtainable loots from the Gachapons. Which Gachapon machine would you like to look?\r\n\r\n#b";
            for (let i = 0; i < lootNames.length; i++) {
                sendStr += "#L" + i + "##b" + lootNames[i] + "#k#l\r\n";
            }
            cm.sendSimple(sendStr);
        } else if (status == 1) {
            var sendStr = "Loots from #b" + lootNames[selection] + "#k\r\n\r\n";
            var ServerManager = Java.type('org.gms.manager.ServerManager');
            var gachaponService = ServerManager.getApplicationContext().getBean("gachaponService");
            var gachaponRewardDOS = gachaponService.getRewardsByNpcId(lootIds[selection]);
            for (let i = 0; i < gachaponRewardDOS.length; i++) {
                var gachaponRewardDO = gachaponRewardDOS[i];
                sendStr += "#v" + gachaponRewardDO.getItemId() + "#   -  #z" + gachaponRewardDO.getItemId() + "#\r\n";
            }
            cm.sendPrev(sendStr);
        } else if (status == 2) {
            cm.dispose();
        }
    }
}