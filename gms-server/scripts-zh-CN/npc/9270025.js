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
/* 	Xan
	Lian Hua Hua Skin Care
        @author Moogra
*/
var skin = Array(0, 1, 2, 3, 4);
var status;

function start() {
    status = -1;
    action(1, 0, 0);
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

        if (status == 0) {
            cm.sendSimple("嗨，你好！欢迎来到莲花花护肤中心！你想要像我一样拥有紧致健康的肌肤吗？使用#b#tCBD护肤券##k，让我们来照顾你的肌肤，让你拥有一直想要的肌肤！\r\n\#L1#听起来不错！（使用#i5153010# #t5153010#）#l");
        } else if (status == 1) {
            if (!cm.haveItem(5153010)) {
                cm.sendOk("看起来你没有需要接受治疗的优惠券。很抱歉，看起来我们不能为你做这件事。");
                cm.dispose();
                return;
            }
            cm.sendStyle("With our specialized service, you can see the way you'll look after the treatment in advance. What kind of a skin-treatment would you like to do? Go ahead and choose the style of your liking...", skin);
        } else {
            cm.gainItem(5153010, -1);
            cm.setSkin(selection);
            cm.sendOk("享受你的新肤色吧！");

            cm.dispose();
        }
    }
}