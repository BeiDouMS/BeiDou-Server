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
function start() {
    if (im.getMapId() == 106020500 && im.isQuestActive(2324)) {
        var player = im.getPlayer();
        var portal = im.getMap().getPortal("investigate2");
        if (portal != null && portal.getPosition().distance(player.getPosition()) < 150) {
            player.gainExp(3300 * player.getExpRate());
            im.forceCompleteQuest(2324);
            im.removeAll(2430015);
            im.playerMessage(6, "使用尖刺消除剂清除道路上的荆棘。");
            im.warp(106020501);
        } else {
            im.playerMessage(5,"尽可能得离荆棘更近些才能完全清除");
        }
    } else {
        im.playerMessage(5,"这里没有荆棘需要清除");
    }
    im.dispose();
}
