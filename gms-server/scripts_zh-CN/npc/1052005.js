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
/* Dr. Feeble
	Henesys Random Eye Change.

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/
var status = 0;
var beauty = 0;
var price = 1000000;
var mface_r = Array(20000, 20005, 20008, 20012, 20016, 20022, 20032);
var fface_r = Array(21000, 21002, 21008, 21014, 21020, 21024, 21029);
var facenew = Array();

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {  // disposing issue with stylishs found thanks to Vcoc
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendSimple("嗨，我其实不应该这样做，但是用一个#b#t5152000##k，我还是会为你做。但别忘了，结果会是随机的！\r\n#L2#整形手术：#i5152000##t5152000##l");
        } else if (status == 1) {
            if (selection == 2) {
                facenew = Array();
                if (cm.getPlayer().getGender() == 0) {
                    for (var i = 0; i < mface_r.length; i++) {
                        pushIfItemExists(facenew, mface_r[i] + cm.getPlayer().getFace()
                            % 1000 - (cm.getPlayer().getFace()
                                % 100));
                    }
                }
                if (cm.getPlayer().getGender() == 1) {
                    for (var i = 0; i < fface_r.length; i++) {
                        pushIfItemExists(facenew, fface_r[i] + cm.getPlayer().getFace()
                            % 1000 - (cm.getPlayer().getFace()
                                % 100));
                    }
                }
                cm.sendYesNo("如果你使用普通的优惠券，你的脸可能会变成一个随机的新样子……你还想用#b#t5152000##k来做吗？");
            }
        } else if (status == 2) {
            cm.dispose();
            if (cm.haveItem(5152000) == true) {
                cm.gainItem(5152000, -1);
                cm.setFace(facenew[Math.floor(Math.random() * facenew.length)]);
                cm.sendOk("享受你的新面容吧！");
            } else {
                cm.sendOk("嗯...看起来你没有这个地方专门的优惠券。很抱歉要说这个，但没有优惠券，你就不能进行整形手术了...");
            }
        }
    }
}