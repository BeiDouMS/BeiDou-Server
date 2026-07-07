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

/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Kino Konoko - Zipangu - Mushroom Shrine(800000000)
 -- By ---------------------------------------------------------------------------------------------
 Ronan
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Ronan
 ---------------------------------------------------------------------------------------------------
 **/

function start() {
    if (cm.isQuestCompleted(8074)) {
        cm.sendOk("谢谢你之前帮了我。古代神社总是春意盎然，欢迎你随时回来走走。");
    } else {
        cm.sendOk("欢迎来到古代神社。这里四季如春，也有许多不可思议的传说。愿你旅途平安。");
    }
    cm.dispose();
}
