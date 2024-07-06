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

/* Rinz the assistant
	Orbis Random Hair/Hair Color Change.

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/
var status = 0;
var beauty = 0;
var hairprice = 1000000;
var haircolorprice = 1000000;
var mhair_d = Array(30030, 30020, 30000, 30270, 30230);
var fhair_d = Array(31040, 31000, 31250, 31220, 31260);
var mhair_r = Array(30230, 30260, 30280, 30340, 30490, 30530, 30630, 30740);
var fhair_r = Array(31110, 31220, 31230, 31630, 31650, 31710, 31790, 31890, 31930);
var mhair_e = Array(30230, 30280, 30340, 30490, 30530, 30740);
var fhair_e = Array(31110, 31220, 31230, 31710, 31790, 31890, 31930);
var hairnew = Array();

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function start() {
    cm.sendSimple("我是助手Rinz。你有#b#t5154000##k、#b#t5150004##k、#b#t5150013##k或#b#t5151004##k吗？如果有的话，你觉得让我来给你打理发型怎么样？你想怎么处理你的头发呢？\r\n#L0#理发：#i5154000##t5154000##l\r\n#L1#理发：#i5150004##t5150004##l\r\n#L2#理发：#i5150013##t5150013##l\r\n#L3#染发：#i5151004##t5151004##l");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            if (selection == 0) {
                beauty = 4;
                hairnew = Array();
                if (cm.getPlayer().getGender() == 0) {
                    for (var i = 0; i < mhair_d.length; i++) {
                        pushIfItemExists(hairnew, mhair_d[i] + parseInt(cm.getPlayer().getHair() % 10));
                    }
                } else {
                    for (var i = 0; i < fhair_d.length; i++) {
                        pushIfItemExists(hairnew, fhair_d[i] + parseInt(cm.getPlayer().getHair() % 10));
                    }
                }
                cm.sendYesNo("如果你使用DRT优惠券，你的发型将随机改变，并有机会获得我设计的基本款式。你打算使用#b#t5154000##k来真正改变你的发型吗？");
            } else if (selection == 1) {
                beauty = 3;
                hairnew = Array();
                if (cm.getPlayer().getGender() == 0) {
                    for (var i = 0; i < mhair_r.length; i++) {
                        pushIfItemExists(hairnew, mhair_r[i] + parseInt(cm.getPlayer().getHair() % 10));
                    }
                } else {
                    for (var i = 0; i < fhair_r.length; i++) {
                        pushIfItemExists(hairnew, fhair_r[i] + parseInt(cm.getPlayer().getHair() % 10));
                    }
                }
                cm.sendYesNo("如果你使用REG优惠券，你的发型将会随机改变。你要使用#b#t5150004##k并真的改变你的发型吗？");
            } else if (selection == 2) {
                beauty = 1;
                hairnew = Array();
                if (cm.getPlayer().getGender() == 0) {
                    for (var i = 0; i < mhair_e.length; i++) {
                        pushIfItemExists(hairnew, mhair_e[i] + parseInt(cm.getPlayer().getHair() % 10));
                    }
                } else {
                    for (var i = 0; i < fhair_e.length; i++) {
                        pushIfItemExists(hairnew, fhair_e[i] + parseInt(cm.getPlayer().getHair() % 10));
                    }
                }
                cm.sendYesNo("如果你使用了经验值券，你的发型将会随机改变，并有机会获得我设计的新实验性发型。你要使用 #b#t5150013##k 真的改变你的发型吗？");
            } else if (selection == 3) {
                beauty = 2;
                haircolor = Array();
                var current = (cm.getPlayer().getHair() / 10) | 0;
                for (var i = 0; i < 8; i++) {
                    pushIfItemExists(haircolor, current + i);
                }
                cm.sendYesNo("如果你使用普通的优惠券，你的发色将会随机改变。你还想使用 #b#t5151004##k 来改变吗？");
            }
        } else if (status == 2) {
            cm.dispose();
            if (beauty == 1) {
                if (cm.haveItem(5150013)) {
                    cm.gainItem(5150013, -1);
                    cm.setHair(hairnew[Math.floor(Math.random() * hairnew.length)]);
                    cm.sendOk("享受你的新发型吧！");
                } else {
                    cm.sendOk("嗯...看起来你没有我们指定的优惠券...恐怕我不能给你理发。对不起...");
                }
            } else if (beauty == 2) {
                if (cm.haveItem(5151004)) {
                    cm.gainItem(5151004, -1);
                    cm.setHair(haircolor[Math.floor(Math.random() * haircolor.length)]);
                    cm.sendOk("享受你的新发色吧！");
                } else {
                    cm.sendOk("嗯...看起来你没有我们指定的优惠券...恐怕我不能给你染发。很抱歉...");
                }
            } else if (beauty == 3) {
                if (cm.haveItem(5150004)) {
                    cm.gainItem(5150004, -1);
                    cm.setHair(hairnew[Math.floor(Math.random() * hairnew.length)]);
                    cm.sendOk("享受你的新发型吧！");
                } else {
                    cm.sendOk("嗯...看起来你没有我们指定的优惠券...恐怕我不能给你理发。对不起...");
                }
            } else if (beauty == 4) {
                if (cm.haveItem(5154000)) {
                    cm.gainItem(5154000, -1);
                    cm.setHair(hairnew[Math.floor(Math.random() * hairnew.length)]);
                    cm.sendOk("享受你的新发型吧！");
                } else {
                    cm.sendOk("嗯...看起来你没有我们指定的优惠券...恐怕我不能给你理发。很抱歉...");
                }
            } else if (beauty == 0) {
                if (selection == 0 && cm.getMeso() >= hairprice) {
                    cm.gainMeso(-hairprice);
                    cm.gainItem(5150013, 1);
                    cm.sendOk("享受！");
                } else if (selection == 1 && cm.getMeso() >= haircolorprice) {
                    cm.gainMeso(-haircolorprice);
                    cm.gainItem(5151004, 1);
                    cm.sendOk("享受！");
                } else {
                    cm.sendOk("你没有足够的金币来购买优惠券！");
                }
            }
        }
    }
}