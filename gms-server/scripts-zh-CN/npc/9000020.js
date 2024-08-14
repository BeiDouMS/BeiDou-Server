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
status = -1;


var travelFrom = [777777777, 541000000];
var travelFee = [3000, 10000];

var travelMap = [800000000, 550000000];
var travelPlace = ["日本蘑菇神社", "马来西亚特区"];
var travelPlaceShort = ["蘑菇神社", "大都市"];
var travelPlaceCountry = ["日本", "马来西亚"];
var travelAgent = ["I", "#r#p9201135##k"];

var travelDescription = ["如果你想感受日本的精髓，没有什么比参观日本文化大熔炉更好的了。蘑菇神社是一个神话般的地方，供奉着古代无与伦比的蘑菇神。",
    "如果你想在乐观的环境中感受热带的炎热，马来西亚的居民非常欢迎你。此外，大都市本身就是当地经济的核心，众所周知，这个地方总是有事情可做或参观。"];

var travelDescription2 = ["看看为蘑菇神服务的女巫，我强烈建议尝试日本街头出售的章鱼烧、烤肉和其他美味的食物。现在，让我们前往#b蘑菇神社#k，这是一个神话般的地方。",
    "一到那里，我强烈建议你安排一次去甘榜村的旅行。为什么？你肯定已经知道奇幻主题公园《幽灵世界》了吧？不它只是把最好的主题公园放在那里，值得一游！现在，让我们前往#b马来西亚特区#k。"];

var travelType;
var travelStatus;

function start() {
    travelStatus = getTravelingStatus(cm.getPlayer().getMapId());
    action(1, 0, 0);
}

function getTravelingStatus(mapid) {
    for (var i = 0; i < travelMap.length; i++) {
        if (mapid == travelMap[i]) {
            return i;
        }
    }

    return -1;
}

function getTravelType(mapid) {
    for (var i = 0; i < travelFrom.length; i++) {
        if (mapid == travelFrom[i]) {
            return i;
        }
    }

    return 0;
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0 && status == 4) {
            status -= 2;
        } else {
            cm.dispose();
            return;
        }
    }

    if (travelStatus != -1) {
        if (status == 0) {
            cm.sendSimple("旅行怎么样？你喜欢吗？\r\n#b\r\n#L0#是的，我已经旅行完了。我可以回到 #m" + cm.getPlayer().peekSavedLocation("WORLDTOUR") + "# 吗？\r\n#L1#不，我想继续探索这个地方。");
        } else if (status == 1) {
            if (selection == 0) {
                cm.sendNext("好的。我会把你带回到你去日本之前的地方。如果你将来又想要旅行的话，请告诉我！");
            } else if (selection == 1) {
                cm.sendOk("好的。如果你改变主意了，请告诉我。");
                cm.dispose();
            }
        } else if (status == 2) {
            var map = cm.getPlayer().getSavedLocation("WORLDTOUR");
            if (map == -1) {
                map = 104000000;
            }

            cm.warp(map);
            cm.dispose();
        }
    } else {
        if (status == 0) {
            travelType = getTravelType(cm.getPlayer().getMapId());
            cm.sendNext("如果你厌倦了单调的日常生活，不如出去换换心情？没有什么比沉浸在新文化中，每分钟学到新东西更令人愉悦的了！是时候让你出去旅行了。我们枫叶旅行社建议你去进行一次#b世界之旅#k！你担心旅行费用吗？不用担心！我们枫叶旅行社精心制定了一项计划，让你仅需#b" + cm.numberWithCommas(travelFee[travelType]) + " 金币#k就可以旅行！");
        } else if (status == 1) {
            cm.sendSimple("我们目前为您提供这个旅行地点：#b" + travelPlace[travelType] + "#k。" + travelAgent[travelType] + "将在那里作为您的旅行向导为您服务。请放心，目的地数量将会随着时间的推移而增加。现在，您想前往" + travelPlaceShort[travelType] + "吗？#b\r\n#L0#是的，带我去" + travelPlaceShort[travelType] + "（" + travelPlaceCountry[travelType] + ")");
        } else if (status == 2) {
            cm.sendNext("你想去#b" + travelPlace[travelType] + "#k旅行吗？" + travelDescription[travelType]);
        } else if (status == 3) {
            if (cm.getMeso() < travelFee[travelType]) {
                cm.sendNext("你没有足够的金币来进行旅行。");
                cm.dispose();
                return;
            }
            cm.sendNextPrev(travelDescription2[travelType]);
        } else if (status == 4) {
            cm.gainMeso(-travelFee[travelType]);
            cm.getPlayer().saveLocation("WORLDTOUR");
            cm.warp(travelMap[travelType], 0);
            cm.dispose();
        }
    }
}
