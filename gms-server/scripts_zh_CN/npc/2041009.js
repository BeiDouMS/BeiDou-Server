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

/* Mini
	Ludibrium Random Hair/Hair Color Change.

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/
var status = 0;
var beauty = 0;
var hairprice = 1000000;
var haircolorprice = 1000000;
var mhair_r = Array(30190, 30220, 30250, 30540, 30610, 30620, 30640, 30650, 30660, 30840, 30870, 30940, 30990);
var fhair_r = Array(31170, 31270, 31290, 31510, 31540, 31550, 31600, 31640, 31680, 31810, 31830, 31840, 31870);
var mhair_e = Array(30030, 30190, 30220, 30250, 30540, 30610, 30620, 30640, 30650, 30660, 30840, 30990);
var fhair_e = Array(31170, 31270, 31430, 31510, 31540, 31550, 31600, 31680, 31810, 31830, 31840, 31870);
var hairnew = Array();

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
            cm.sendSimple("嗨，我是这里的助手。别担心，我完全能胜任这个任务。如果你碰巧有#b#t5150006##k、#b#t5150012##k或#b#t5151006##k，那就让我来处理剩下的事情，好吗？\r\n#L0#理发：#i5150006##t5150006##l\r\n#L1#理发：#i5150012##t5150012##l\r\n#L2#染发：#i5151006##t5151006##l");
        } else if (status == 1) {
            if (selection == 0) {
                beauty = 3;
                hairnew = Array();
                if (cm.getPlayer().getGender() == 0) {
                    for (var i = 0; i < mhair_r.length; i++) {
                        pushIfItemExists(hairnew, mhair_r[i] + parseInt(cm.getPlayer().getHair() % 10));
                    }
                }
                if (cm.getPlayer().getGender() == 1) {
                    for (var i = 0; i < fhair_r.length; i++) {
                        pushIfItemExists(hairnew, fhair_r[i] + parseInt(cm.getPlayer().getHair() % 10));
                    }
                }
                cm.sendYesNo("如果您使用REG优惠券，您的发型将随机改变，并有机会获得我设计的新实验风格。您要使用#b#t5150012##k来真正改变您的发型吗？");
            } else if (selection == 1) {
                beauty = 1;
                hairnew = Array();
                if (cm.getPlayer().getGender() == 0) {
                    for (var i = 0; i < mhair_e.length; i++) {
                        pushIfItemExists(hairnew, mhair_e[i] + parseInt(cm.getPlayer().getHair() % 10));
                    }
                }
                if (cm.getPlayer().getGender() == 1) {
                    for (var i = 0; i < fhair_e.length; i++) {
                        pushIfItemExists(hairnew, fhair_e[i] + parseInt(cm.getPlayer().getHair() % 10));
                    }
                }
                cm.sendYesNo("如果你使用了经验值券，你的发型将会随机改变，并有机会获得我设计的新实验性发型。你要使用 #b#t5150012##k 真的改变你的发型吗？");
            } else if (selection == 2) {
                beauty = 2;
                haircolor = Array();
                var current = parseInt(cm.getPlayer().getHair() / 10) * 10;
                for (var i = 0; i < 8; i++) {
                    pushIfItemExists(haircolor, current + i);
                }
                cm.sendYesNo("如果你使用普通的优惠券，你的发型将会随机改变。你还想使用 #b#t5151006##k 来改变吗？");
            }
        } else if (status == 2) {
            cm.dispose();
            if (beauty == 1) {
                if (cm.haveItem(5150012)) {
                    cm.gainItem(5150012, -1);
                    cm.setHair(hairnew[Math.floor(Math.random() * hairnew.length)]);
                    cm.sendOk("享受你的新发型吧！");
                } else {
                    cm.sendOk("嗯...看起来你没有我们指定的优惠券...恐怕我不能给你理发。对不起...");
                }
            } else if (beauty == 2) {
                if (cm.haveItem(5151006)) {
                    cm.gainItem(5151006, -1);
                    cm.setHair(haircolor[Math.floor(Math.random() * haircolor.length)]);
                    cm.sendOk("享受你的新发色吧！");
                } else {
                    cm.sendOk("嗯...看起来你没有我们指定的优惠券...恐怕我不能给你染发。很抱歉...");
                }
            } else if (beauty == 3) {
                if (cm.haveItem(5150006)) {
                    cm.gainItem(5150006, -1);
                    cm.setHair(haircolor[Math.floor(Math.random() * haircolor.length)]);
                    cm.sendOk("享受你的新发色！");
                } else {
                    cm.sendOk("嗯...看起来你没有我们指定的优惠券...恐怕我不能在没有它的情况下给你染发。对不起...");
                }
            } else if (beauty == 0) {
                if (selection == 0 && cm.getMeso() >= hairprice) {
                    cm.gainMeso(-hairprice);
                    cm.gainItem(5150012, 1);
                    cm.sendOk("享受！");
                } else if (selection == 1 && cm.getMeso() >= haircolorprice) {
                    cm.gainMeso(-haircolorprice);
                    cm.gainItem(5151006, 1);
                    cm.sendOk("享受！");
                } else {
                    cm.sendOk("你没有足够的金币来购买优惠券！");
                }
            }
        }
    }
}