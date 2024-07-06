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
/* 	Noel
	Singapore Random Face Changer
	@Author AAron (aaroncsn), Cody
	Side note by aaron [If there is something wrong PM me on fMS]

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/
var status = 0;
var beauty = 0;
var mface_r = Array(20002, 20005, 20006, 20013, 20017, 20021, 20024);
var fface_r = Array(21002, 21003, 21014, 21016, 21017, 21021, 21027);
var facenew = Array();

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function start() {
    cm.sendSimple("如果你使用这张普通的优惠券，你的脸可能会变成一个随机的新样子...你还想用 #b#t5152037##k 来做吗？我会帮你做。但别忘了，它会是随机的！\r\n\#L2#好的！（使用 #i5152037# #t5152037#）#l");
}

function action(mode, type, selection) {
    if (mode < 1)  // disposing issue with stylishs found thanks to Vcoc
    {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 1) {
            if (!cm.haveItem(5152037)) {
                cm.sendOk("嗯...看起来你没有这个地方专门的优惠券。很抱歉要说这个，但没有优惠券，你就不能进行整形手术了...");
                cm.dispose();
                return;
            }

            facenew = Array();
            if (cm.getPlayer().getGender() == 0) {
                for (var i = 0; i < mface_r.length; i++) {
                    pushIfItemExists(facenew, mface_r[i] + cm.getPlayer().getFace() % 1000 - (cm.getPlayer().getFace() % 100));
                }
            }
            if (cm.getPlayer().getGender() == 1) {
                for (var i = 0; i < fface_r.length; i++) {
                    pushIfItemExists(facenew, fface_r[i] + cm.getPlayer().getFace() % 1000 - (cm.getPlayer().getFace() % 100));
                }
            }
            cm.sendYesNo("如果你使用普通的优惠券，你的脸可能会变成一个随机的新样子……你还想用#b#t5152037##k来做吗？");
        } else if (status == 2) {
            cm.gainItem(5152037, -1);
            cm.setFace(facenew[Math.floor(Math.random() * facenew.length)]);
            cm.sendOk("享受你的新面容吧！");

            cm.dispose();
        }
    }
}