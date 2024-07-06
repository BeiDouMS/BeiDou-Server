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
 ? - Victoria Road: Pet-Walking Road (100000202)
 -- By ---------------------------------------------------------------------------------------------
 Xterminator
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Xterminator
 2.0 - Second Version by Moogra
 ---------------------------------------------------------------------------------------------------
 **/

function start() {
    cm.sendYesNo("#b(我看到有东西被草覆盖着。我应该把它拔出来吗？)");
}

function action(mode, type, selection) {
    if (mode == -1) {
    } else if (mode == 0) {
        cm.sendNext("#b(我没太在意，所以我没碰它。)");
    } else if (mode == 1) {
        cm.sendNext("#b(呸... 这是宠物的粪便！)");
        cm.gainItem(4031922, 1);
    }
    cm.dispose();
}