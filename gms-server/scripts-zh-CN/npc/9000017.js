/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
/* Coco
        Refining NPC: 
	* Chaos scroll SYNTHETIZER (rofl)
        * 
        * @author RonanLana (ronancpl)
*/

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;
var qty;
var equip;
var last_use; //last item is a use item

function start() {
    cm.getPlayer().setCS(true);
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        cm.sendOk("好吧...如果你想合成卷轴，再来找我们。");
        cm.dispose();
        return;
    }

    if (status == 0) {
        const GameConfig = Java.type('org.gms.config.GameConfig');
        if (!GameConfig.getServerBoolean("use_enable_custom_npc_script")) {
            cm.sendOk("嗨，我是 #b#p" + cm.getNpc() + "##k。");
            cm.dispose();
            return;
        }

        var selStr = "嘿，旅行者！靠近点...我们这里有一桩#b不错的生意#k。想知道是什么，就继续听我说。";
        cm.sendNext(selStr);
    } else if (status == 1) {
        var selStr = "我们掌握了合成#b#t2049100##k的方法！当然，制作它并不轻松。不过别担心，只要准备材料并支付#b1,200,000金币#k的手续费，我就能帮你合成。还要继续吗？";
        cm.sendYesNo(selStr);
    } else if (status == 2) {
        //selectedItem = selection;
        selectedItem = 0;

        var itemSet = [2049100, 7777777];
        var matSet = new Array([4031203, 4001356, 4000136, 4000082, 4001126, 4080100, 4000021, 4003005]);
        var matQtySet = new Array([100, 60, 40, 80, 10, 8, 200, 120]);
        var costSet = [1200000, 7777777];
        item = itemSet[selectedItem];
        mats = matSet[selectedItem];
        matQty = matQtySet[selectedItem];
        cost = costSet[selectedItem];

        var prompt = "你想让我们制作多少个#t" + item + "#？";
        cm.sendGetNumber(prompt, 1, 1, 100)
    } else if (status == 3) {
        qty = (selection > 0) ? selection : (selection < 0 ? -selection : 1);
        last_use = false;

        var prompt = "你要制作";
        if (qty == 1) {
            prompt += "1个#t" + item + "#吗？";
        } else {
            prompt += qty + "个#t" + item + "#吗？";
        }

        prompt += "那么我们需要以下材料才能开始合成。请先确认你的消耗栏有足够空间！#b";

        if (mats instanceof Array) {
            for (var i = 0; i < mats.length; i++) {
                prompt += "\r\n#i" + mats[i] + "# " + matQty[i] * qty + " #t" + mats[i] + "#";
            }
        } else {
            prompt += "\r\n#i" + mats + "# " + matQty * qty + " #t" + mats + "#";
        }

        if (cost > 0) {
            prompt += "\r\n#i4031138# " + cost * qty + " 金币";
        }
        cm.sendYesNo(prompt);
    } else if (status == 4) {
        var complete = true;

        if (cm.getMeso() < cost * qty) {
            cm.sendOk("金币不够。我们做的是生意，不是免费帮忙。准备好费用后再来吧。");
        } else if (!cm.canHold(item, qty)) {
            cm.sendOk("开始交易前，请先确认你的消耗栏有足够空位。");
        } else {
            if (mats instanceof Array) {
                for (var i = 0; complete && i < mats.length; i++) {
                    if (matQty[i] * qty == 1) {
                        complete = cm.haveItem(mats[i]);
                    } else {
                        complete = cm.haveItem(mats[i], matQty[i] * qty);
                    }
                }
            } else {
                complete = cm.haveItem(mats, matQty * qty);
            }

            if (!complete) {
                cm.sendOk("材料还不齐。没有所有材料，我们无法开始合成。把材料准备好后再来找我们。");
            } else {
                if (mats instanceof Array) {
                    for (var i = 0; i < mats.length; i++) {
                        cm.gainItem(mats[i], -matQty[i] * qty);
                    }
                } else {
                    cm.gainItem(mats, -matQty * qty);
                }
                cm.gainMeso(-cost * qty);
                cm.gainItem(item, qty);
                cm.sendOk("完成了！当然会成功，我们的手艺可是很可靠的。很高兴和你做这笔生意。");
            }
        }
        cm.dispose();
    }
}
