/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Muhammad
	Map(s): 		Ariant:The Town of Ariant(260000200)
	Description: 	Jewel Refiner
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

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode <= 0 && status == 0) {
        cm.sendNext("如果你现在还不急，就等会儿再来。你也看到了，现在我手上的工作多得要命，可能没法按时交付你给我的任务。");
        cm.dispose();
        return;
    }
    if (mode <= 0 && status >= 1) {
        cm.dispose();
        return;
    }
    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 0) {
        cm.sendYesNo("你是来这里冶炼矿石母矿或宝石母矿的吧？不论有多少母矿，只有经过我这样的冶炼大师之手，才能让它们重现世间。怎么样，你想要开始冶炼它们吗？");
    }
    if (status == 1 && mode == 1) {
        var selStr = "我喜欢你的作风！我们现在就开工吧。你想要冶炼哪种母矿？ #b";
        var options = ["冶炼矿石母矿","冶炼宝石母矿","冶炼水晶母矿"];
        for (var i = 0; i < options.length; i++) {
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        }
        cm.sendSimple(selStr);
    } else if (status == 2 && mode == 1) {
        selectedType = selection;

        if (selectedType == 0) { //mineral refine
            var selStr = "你想要冶炼哪种矿石？#b";
            var minerals = ["青铜","钢铁","锂矿石","朱矿石","银","紫矿石","黄金","锂"];
            for (var i = 0; i < minerals.length; i++) {
                selStr += "\r\n#L" + i + "# " + minerals[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = false;
        } else if (selectedType == 1) { //jewel refine
            var selStr = "你想要冶炼哪种宝石？#b";
            var jewels = ["石榴石","紫水晶","海蓝石","祖母绿","蛋白石","蓝宝石","黄晶","钻石","黑水晶"];
            for (var i = 0; i < jewels.length; i++) {
                selStr += "\r\n#L" + i + "# " + jewels[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = false;
        } else if (selectedType == 2) { //Crystal refine
            var selStr = "水晶？这可真是稀有。别担心，我冶炼它们的手艺就像对矿石和宝石那样熟练。你想要冶炼哪种水晶？#b";
            var crystals = ["力量水晶","智慧水晶","敏捷水晶","幸运水晶"];
            for (var i = 0; i < crystals.length; i++) {
                selStr += "\r\n#L" + i + "# " + crystals[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = false;
        }
    } else if (status == 3 && mode == 1) {
        selectedItem = selection;

        if (selectedType == 0) { //mineral refine
            var itemSet = [4011000, 4011001, 4011002, 4011003, 4011004, 4011005, 4011006, 4011008];
            var matSet = [4010000, 4010001, 4010002, 4010003, 4010004, 4010005, 4010006, 4010007];
            var matQtySet = [10, 10, 10, 10, 10, 10, 10, 10];
            var costSet = [270, 270, 270, 450, 450, 450, 720, 270];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 1) { //jewel refine
            var itemSet = [4021000, 4021001, 4021002, 4021003, 4021004, 4021005, 4021006, 4021007, 4021008];
            var matSet = [4020000, 4020001, 4020002, 4020003, 4020004, 4020005, 4020006, 4020007, 4020008];
            var matQtySet = [10, 10, 10, 10, 10, 10, 10, 10, 10];
            var costSet = [450, 450, 450, 450, 450, 450, 450, 900, 2700];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 2) { //Crystal refine
            var itemSet = [4005000, 4005001, 4005002, 4005003];
            var matSet = [4004000, 4004001, 4004002, 4004003];
            var matQtySet = [10, 10, 10, 10];
            var costSet = [4500, 4500, 4500, 4500];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }

        var prompt = "想要制作#t" + item + "#s，对吗？那么，你想制作多少？";
        cm.sendGetNumber(prompt, 1, 1, 100)
    } else if (status == 4 && mode == 1) {
        if (equip) {
            selectedItem = selection;
            qty = 1;
        } else {
            qty = (selection > 0) ? selection : (selection < 0 ? -selection : 1);
        }

        var prompt = "你想制作 ";
        if (qty == 1) {
            prompt += "一件 #t" + item + "#?";
        } else {
            prompt += qty + "件 #t" + item + "#?";
        }

        prompt += " 那么，请确认你准备好了相应材料，并且背包里有充足的空间。#b";

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
    } else if (status == 5 && mode == 1) {
        var complete = true;
        var recvItem = item, recvQty;

        if (item >= 2060000 && item <= 2060002) //bow arrows
        {
            recvQty = 1000 - (item - 2060000) * 100;
        } else if (item >= 2061000 && item <= 2061002) //xbow arrows
        {
            recvQty = 1000 - (item - 2061000) * 100;
        } else if (item == 4003000)//screws
        {
            recvQty = 15 * qty;
        } else {
            recvQty = qty;
        }

        if (!cm.canHold(recvItem, recvQty)) {
            cm.sendOk("背包空间不足。");
        } else if (cm.getMeso() < cost * qty) {
            cm.sendOk("金币不足的话，我无法为你制作。");
        } else {
            if (mats instanceof Array) {
                for (var i = 0; complete && i < mats.length; i++) {
                    if (matQty[i] * qty == 1) {
                        if (!cm.haveItem(mats[i])) {
                            complete = false;
                        }
                    } else {

                        if (cm.haveItem(mats[i], matQty[i] * qty)) {
                            complete = false;
                        }
                    }
                }
            } else {
                if (!cm.haveItem(mats, matQty * qty)) {
                    complete = false;
                }
            }

            if (!complete) {
                cm.sendOk("请确保材料足够，并且有足够的其他栏空间。");
            } else {
                if (mats instanceof Array) {
                    for (var i = 0; i < mats.length; i++) {
                        cm.gainItem(mats[i], -matQty[i] * qty);
                    }
                } else {
                    cm.gainItem(mats, -matQty * qty);
                }

                if (cost > 0) {
                    cm.gainMeso(-cost * qty);
                }

                cm.gainItem(recvItem, recvQty);
                cm.sendOk("东西都在这里，完成了。简直就是艺术品，你不这样认为吗？总之，如果还有其他需要，就再来找我吧。");
            }
        }

        cm.dispose();
    }
}