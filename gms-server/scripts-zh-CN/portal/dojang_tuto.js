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
	Map(s):	So Gong's maps
	Desc:   doesn't do anything man. ANYTHING.
*/

function enter(pi) {
    if (pi.getPlayer().getMap().getMonsterById(9300216) != null) {
        pi.getPlayer().enteredScript("dojang_Msg", pi.getPlayer().getMap().getId());
        pi.getPlayer().finishDojoTutorial();
        pi.getClient().getChannelServer().resetDojo(pi.getPlayer().getMap().getId());
        pi.getClient().getChannelServer().dismissDojoSchedule(pi.getPlayer().getMap().getId(), pi.getParty());
        pi.playPortalSound();
        pi.warp(925020001, 0);
        return true;
    } else {
        pi.getPlayer().message("萧公：哈哈！你想像个懦夫一样逃跑吗？我不会让你这么容易逃掉的！");
        return false;
    }
}