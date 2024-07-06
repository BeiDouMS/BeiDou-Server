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
var travelPlace = ["Mushroom Shrine of Japan", "Trend Zone of Malaysia"];
var travelPlaceShort = ["Mushroom Shrine", "Metropolis"];
var travelPlaceCountry = ["Japan", "Malaysia"];
var travelAgent = ["I", "#r#p9201135##k"];

var travelDescription = ["If you desire to feel the essence of Japan, there's nothing like visiting the Shrine, a Japanese cultural melting pot. Mushroom Shrine is a mythical place that serves the incomparable Mushroom God from ancient times.",
    "If you desire to feel the heat of the tropics on an upbeat environment, the residents of Malaysia are eager to welcome you. Also, the metropolis itself is the heart of the local economy, that place is known to always offer something to do or to visit around."];

var travelDescription2 = ["Check out the female shaman serving the Mushroom God, and I strongly recommend trying Takoyaki, Yakisoba, and other delocious food sold in the streets of Japan. Now, let's head over to #bMushroom Shrine#k, a mythical place if there ever was one.",
    "Once there, I strongly suggest you to schedule a visit to Kampung Village. Why? Surely you've come to know about the fantasy theme park Spooky World? No? It's simply put the greatest theme park around there, it's worth a visit! Now, let's head over to the #bTrend Zone of Malaysia#k."];

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
            cm.sendNext("如果你厌倦了单调的日常生活，不如出去换换心情？没有什么比沉浸在新文化中，每分钟学到新东西更令人愉悦的了！是时候让你出去旅行了。我们枫叶旅行社建议你去进行一次#b世界之旅#k！你担心旅行费用吗？不用担心！我们枫叶旅行社精心制定了一项计划，让你仅需#b" + cm.numberWithCommas(travelFee[travelType]) + " 枫币#k就可以旅行！");
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