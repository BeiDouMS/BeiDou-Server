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
@author kevintjuh93
*/
function enter(pi) {
    pi.blockPortal();
    if (pi.containsAreaInfo(21002, "normal=o")) {
        return false;
    }
    pi.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialGuide1");
    pi.message("按一下Ctrl键，能够对怪兽进行一般攻击。");
    pi.updateAreaInfo(21002, "normal=o;arr0=o;mo1=o;mo2=o;mo3=o");
    return true;
}  