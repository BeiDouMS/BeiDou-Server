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
/* Fredrick NPC (9030000)
 * @author kevintjuh93
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        cm.dispose();
        return;
    }
    if (status == 0) {
        if (!cm.hasMerchant() && cm.hasMerchantItems()) {
            cm.showFredrick();
        } else {
            if (cm.hasMerchant()) {
				var tan = cm.getClient().getWorldServer().getHiredMerchant(cm.getPlayer().getId());
				var text = "你雇佣的商店已开设"
				text += `\r\n摊位在 #r频道${tan.getChannel()} 自由市场〈#r#e${tan.getMapId() - 910000000}#n〉#k`;
                cm.sendOk(text);
            } else {
                cm.sendOk("你没有任何物品或金币可以取回。");
            }
        }
        cm.dispose();
    }else{
        cm.dispose();
    }
}
