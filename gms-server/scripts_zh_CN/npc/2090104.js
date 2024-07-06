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

/* Noma
	Mu Lung Random/VIP Eye Change.

        GMS-like revised by Ronan -- contents found thanks to Mitsune (GamerBewbs), Waltzing, AyumiLove
*/
var status = 0;
var beauty = 0;
var mface_r = Array(20002, 20005, 20007, 20011, 20014, 20017, 20029);
var fface_r = Array(21001, 21010, 21013, 21018, 21020, 21021, 21030);

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}

function pushIfItemsExists(array, itemidList) {
    for (var i = 0; i < itemidList.length; i++) {
        var itemid = itemidList[i];

        if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
            array.push(itemid);
        }
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
            cm.sendSimple("嘿，我是诺玛，我正在帮助帕塔进行面部改变和应用隐形眼镜，作为我的实习研究。使用#b#t5152027##k或#b#t5152042##k，我可以改变你的外貌。现在，你想要使用什么？\r\n#L1#整形手术：#i5152027##t5152027##l\r\n#L2#美瞳：#i5152042##t5152042##l");
        } else if (status == 1) {
            if (selection == 1) {
                beauty = 1;
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
                cm.sendYesNo("如果你使用普通的优惠券，你的脸可能会变成一个随机的新样子……你还想用#b#t5152027##k来做吗？");
            } else if (selection == 2) {
                beauty = 2;
                if (cm.getPlayer().getGender() == 0) {
                    var current = cm.getPlayer().getFace()
                        % 100 + 20000;
                }
                if (cm.getPlayer().getGender() == 1) {
                    var current = cm.getPlayer().getFace()
                        % 100 + 21000;
                }
                colors = Array();
                pushIfItemsExists(colors, [current, current + 100, current + 300, current + 500, current + 600, current + 700]);
                cm.sendYesNo("如果你使用普通优惠券，你将获得一副随机的化妆隐形眼镜。你打算使用#b#t5152042##k，真的改变你的眼睛吗？");
            }
        } else if (status == 2) {
            cm.dispose();
            if (beauty == 1) {
                if (cm.haveItem(5152027)) {
                    cm.gainItem(5152027, -1);
                    cm.setFace(facenew[Math.floor(Math.random() * facenew.length)]);
                    cm.sendOk("享受你的新面容吧！");
                } else {
                    cm.sendOk("对不起，但我觉得你现在没有我们的整形手术优惠券。没有优惠券，恐怕我不能为你做手术。");
                }
            }
            if (beauty == 2) {
                if (cm.haveItem(5152042)) {
                    cm.gainItem(5152042, -1);
                    cm.setFace(colors[Math.floor(Math.random() * colors.length)]);
                    cm.sendOk("享受你的新款和升级版的美瞳隐形眼镜吧！");
                } else {
                    cm.sendOk("对不起，但我觉得你现在没有我们的化妆镜片优惠券。没有优惠券，恐怕我不能为你做这件事。");
                }
            }
        }
    }
}