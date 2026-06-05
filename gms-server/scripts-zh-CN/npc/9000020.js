/*
    This file is part of the BeiDou MapleStory Server
    Copyright (C) 2025 Magical-H <@Magical-H>

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
/*
    Standard dialog script for NPC 9000020 (导游妮妮).
    Uses start/action instead of the custom Level dialog flow so it works with the
    regular NPC conversation packet handlers.
*/
var status = -1;
var selected = -1;
var travelStatus = -1;
var travelAgency = "北斗航旅";
var feeMin = 5000;
var feeMax = 50000;

var travelMaps = [
    {
        area: "东方神州",
        name: "上海外滩",
        mapId: 701000000,
        portal: 1,
        desc1: "上海外滩是上海市的标志性景点之一，以其壮丽的天际线而闻名。这里是黄浦江畔的一条长堤路，两旁矗立着众多历史建筑和现代化摩天大楼。",
        desc2: "你可以在这里欣赏浦东陆家嘴、外白渡桥以及历史悠久的老建筑群。夜晚灯光璀璨，非常适合散步或拍照留念。"
    },
    {
        area: "东方神州",
        name: "嵩山镇",
        mapId: 702000000,
        portal: 8,
        desc1: "嵩山镇是一个充满历史韵味的小城镇，这里有许多古老的寺庙和文化遗产。",
        desc2: "你可以参观少林寺，体验正宗的禅宗文化，也可以欣赏美丽的山水风光。"
    },
    {
        area: "新加坡",
        name: "驳船码头城",
        mapId: 541000000,
        portal: 4,
        desc1: "驳船码头城是新加坡一个充满活力的历史区域，将现代都市生活与殖民时期建筑融合在一起。",
        desc2: "这里有各种酒吧、餐厅和娱乐场所，也能品尝到辣椒蟹、海南鸡饭等当地美食。"
    },
    {
        area: "马来西亚",
        name: "吉隆大都市",
        mapId: 550000000,
        portal: 4,
        desc1: "如果你想在乐观的环境中感受热带气息，马来西亚的居民非常欢迎你。",
        desc2: "到达那里后，我建议你安排一次去甘榜村的旅行，那里有值得一看的奇幻主题公园。"
    },
    {
        area: "日本",
        name: "古代神社",
        mapId: 800000000,
        portal: 5,
        desc1: "如果你想感受日本的精髓，没有什么比参观古代神社更合适了。这里是一个充满神话气息的地方。",
        desc2: "看看为古代神服务的女巫，也别忘了尝试街头出售的章鱼烧、烤肉和其他美味食物。"
    }
];

function start() {
    travelStatus = getTravelingStatus(cm.getMapId());
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode != 1) {
        cm.dispose();
        return;
    }

    status++;

    if (travelStatus != -1) {
        handleReturn(selection);
    } else {
        handleTravel(selection);
    }
}

function handleReturn(selection) {
    if (status == 0) {
        var saved = peekSavedMap();
        var targetText = saved > 0 ? "#m" + saved + "#" : "最近的城镇";
        cm.sendSimple("旅行怎么样？你喜欢吗？\r\n#b#L0#是的，我已经旅行完了，要回到" + targetText + "。#l\r\n#L1#不，我想继续探索这个地方。#l");
    } else if (status == 1) {
        selected = selection;
        if (selected == 0) {
            var savedMap = peekSavedMap();
            if (savedMap <= 0) {
                cm.sendNext("我这里没有记录到你来之前的位置，我会先送你回到射手村。下次通过导游出发，就能返回原来的地方了。");
            } else {
                cm.sendNext("好的，我这就送你回到 #b#m" + savedMap + "##k。以后想旅行的话，随时再来找我吧！");
            }
        } else {
            cm.sendOk("好的。如果你想回去了，可以随时来找我。祝你旅途愉快！");
            cm.dispose();
        }
    } else if (status == 2) {
        var map = getSavedMap();
        if (map <= 0) {
            map = 100000000;
        }
        cm.warp(map, 0);
        cm.dispose();
    }
}

function handleTravel(selection) {
    if (status == 0) {
        cm.sendNext("如果你觉得日常生活有些单调，不妨换个心情，去外面的世界探索一番吧！\r\n\r\n我们#b#e" + travelAgency + "#k#n已经为你准备好了世界之旅。费用会按照距离自动计算。准备好出发了吗？");
    } else if (status == 1) {
        var text = "请选择你想前往的旅行地点：\r\n\r\n";
        for (var i = 0; i < travelMaps.length; i++) {
            var m = travelMaps[i];
            text += "#b#L" + i + "#【" + m.area + "】 " + m.name + " #k(" + formatMeso(getFee(m.mapId)) + " 金币)#l\r\n";
        }
        cm.sendSimple(text);
    } else if (status == 2) {
        selected = selection;
        if (!isValidSelection(selected)) {
            cm.sendOk("请选择有效的旅行地点。非法的旅行路线无法受理。希望你能理解。");
            cm.dispose();
            return;
        }

        var dest = travelMaps[selected];
        cm.sendNext("你选择的是 #b" + dest.area + " - " + dest.name + "#k。\r\n\r\n" + dest.desc1);
    } else if (status == 3) {
        var dest2 = travelMaps[selected];
        var fee = getFee(dest2.mapId);
        cm.sendNextPrev(dest2.desc2 + "\r\n\r\n这次旅行需要 #b" + formatMeso(fee) + " 金币#k。确认后我就会送你出发。");
    } else if (status == 4) {
        var dest3 = travelMaps[selected];
        var travelFee = getFee(dest3.mapId);
        if (cm.getMeso() < travelFee) {
            cm.sendOk("哎呀，你的金币不足，无法进行这次世界之旅。等你攒够 #b" + formatMeso(travelFee) + " 金币#k 后，随时欢迎再来找我！");
            cm.dispose();
            return;
        }

        cm.getPlayer().saveLocation("WORLDTOUR");
        cm.gainMeso(-travelFee);
        cm.warp(dest3.mapId, dest3.portal);
        cm.dispose();
    }
}

function getTravelingStatus(mapId) {
    for (var i = 0; i < travelMaps.length; i++) {
        if (travelMaps[i].mapId == mapId) {
            return i;
        }
    }
    return -1;
}

function isValidSelection(index) {
    return index >= 0 && index < travelMaps.length;
}

function getFee(targetMapId) {
    var distanceFactor = 0.00004;
    var fee = feeMin + Math.abs(targetMapId - cm.getMapId()) * distanceFactor;
    fee = Math.floor(fee);
    if (fee > feeMax) {
        return feeMax;
    }
    return fee;
}

function formatMeso(value) {
    return String(value).replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function peekSavedMap() {
    var map = cm.getPlayer().peekSavedLocation("WORLDTOUR");
    return normalizeMapId(map);
}

function getSavedMap() {
    var map = cm.getPlayer().getSavedLocation("WORLDTOUR");
    return normalizeMapId(map);
}

function normalizeMapId(map) {
    if (map === null || map === undefined) {
        return -1;
    }
    map = Number(map);
    if (isNaN(map) || map <= 0 || map == 999999999) {
        return -1;
    }
    return map;
}
