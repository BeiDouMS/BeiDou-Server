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
            im.playerMessage(6, "Use a spike remover to remove thorns from the road.");
        } else {
            im.playerMessage(5,"Try to get closer to the thorns as much as possible to completely clear them.");
        }
    } else {
        im.playerMessage(5,"There are no thorns here that need to be cleared");
    }
    im.dispose();
}
