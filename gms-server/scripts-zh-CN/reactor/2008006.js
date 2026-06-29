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

/*@author Ronan
 *Reactor : Orbis PQ LP Player - 2008006.js
 * Makes Chamberlain Eak spawn box.

 @update dwang
 周一	Beachway	可爱的音乐
 周二	WelcomeToTheHell	恐怖的音乐
 周三	Aquarium	有趣的音乐
 周四	CaveOfHontale	忧郁的音乐
 周五	EvilEyes	冰冷的音乐
 周六	MoonlightShadow	晴朗的音乐
 周日	ForTheGlory	雄壮的音乐

 */

function act() {
    //
    var eim = rm.getEventInstance();
    var day = eim.getInstanceMap(920010400).getReactorByName("music").getEventState();
    switch(day) {
        case 0:
             rm.changeMusic("Bgm08.img/ForTheGlory");
            break;
        case 1:
             rm.changeMusic("Bgm03.img/Beachway");
            break;
        case 2:
             rm.changeMusic("Bgm06.img/WelcomeToTheHell");
            break;
        case 3:
             rm.changeMusic("Bgm11.img/Aquarium");
            break;
        case 4:
             rm.changeMusic("Bgm14.img/CaveOfHontale");
            break;
        case 5:
             rm.changeMusic("Bgm02.img/EvilEyes");
            break;
        case 6:
             rm.changeMusic("Bgm01.img/MoonlightShadow");
            break;
    }
    eim.setProperty("statusStg3", "0");
}
