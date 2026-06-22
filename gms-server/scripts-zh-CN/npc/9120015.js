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
 Konpei - Showa Town(801000000)
 -- By ---------------------------------------------------------------------------------------------
 Information
 -- Version Info -----------------------------------------------------------------------------------
 1.1 - Fixed by Moogra
 1.0 - First Version by Information
 ---------------------------------------------------------------------------------------------------
 **/
var status = 0;

function start() {
    cm.sendSimple("你找我有什么事？\r\n#L0##b打听藏身处的情报。#l\r\n#L1#带我去藏身处。#l\r\n#L2#没什么。#l#k");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            if (selection == 0) {
                cm.sendNext("我可以带你去藏身处，但那里到处都是找麻烦的暴徒。想进去，就必须有足够的实力和胆量。藏身处深处有统领这一带帮派的头目，顶层的头目房间每天只能进入一次，千万不要在那里逗留太久。头目本身很强，路上也会遇到许多棘手的敌人，这绝不是轻松的战斗。");
                cm.dispose();
            } else if (selection == 1) {
                cm.sendNext("勇敢的冒险者，我一直在等你。如果继续放任那些家伙，昭和村迟早会出事。请你前往藏身处，击败盘踞在 5 楼的头目。一路上敌人很多，千万保持警惕。看你的眼神，我知道你已经准备好了。走吧！");
            } else {
                cm.sendOk("我很忙！没事的话就别打扰我。");
                cm.dispose();
            }
        } else {
            cm.warp(801040000, "in00");
            cm.dispose();
        }
    }
}