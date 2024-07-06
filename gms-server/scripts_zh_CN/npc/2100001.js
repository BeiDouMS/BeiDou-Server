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
        cm.sendNext("如果你不着急的话，请稍后再来。你看，现在有这么多工作要做，我根本无法按时把它们交给你。");
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
        cm.sendYesNo("你是来精炼矿石还是宝石的？你有多少矿石并不重要，如果你不找像我这样的大师来精炼，它们就不会见到阳光。你觉得呢，要不要现在就精炼它们？");
    }
    if (status == 1 && mode == 1) {
        var selStr = "I like your attitude! Let's just take care of this right now. What kind of ores would you like to refine? #b";
        var options = ["Refine mineral ore", "Refine jewel ores", "Refine crystal ores"];
        for (var i = 0; i < options.length; i++) {
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        }
        cm.sendSimple(selStr);
    } else if (status == 2 && mode == 1) {
        selectedType = selection;

        if (selectedType == 0) { //mineral refine
            var selStr = "Which mineral would you like to refine?#b";
            var minerals = ["Bronze Plate", "Steel Plate", "Mithril Plate", "Adamantium Plate", "Silver Plate", "Orihalcon Plate", "Gold Plate", "Lithium"];
            for (var i = 0; i < minerals.length; i++) {
                selStr += "\r\n#L" + i + "# " + minerals[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = false;
        } else if (selectedType == 1) { //jewel refine
            var selStr = "Which jewel would you like to refine?#b";
            var jewels = ["Garnet", "Amethyst", "Aquamarine", "Emerald", "Opal", "Sapphire", "Topaz", "Diamond", "Black Crystal"];
            for (var i = 0; i < jewels.length; i++) {
                selStr += "\r\n#L" + i + "# " + jewels[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = false;
        } else if (selectedType == 2) { //Crystal refine
            var selStr = "A crystal? That's a rare item indeed. Don't worry, I can refine it just as well as others. Which crystal would you like to refine? #b";
            var crystals = ["Power Crystal", "Wisdom Crystal", "DEX Crystal", "LUK Crystal"];
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

        var prompt = "So, you want me to make some #t" + item + "#s? In that case, how many do you want me to make?";
        cm.sendGetNumber(prompt, 1, 1, 100)
    } else if (status == 4 && mode == 1) {
        if (equip) {
            selectedItem = selection;
            qty = 1;
        } else {
            qty = (selection > 0) ? selection : (selection < 0 ? -selection : 1);
        }

        var prompt = "You want me to make ";
        if (qty == 1) {
            prompt += "a #t" + item + "#?";
        } else {
            prompt += qty + " #t" + item + "#?";
        }

        prompt += " In that case, I'm going to need specific items from you in order to make it. Make sure you have room in your inventory, though!#b";

        if (mats instanceof Array) {
            for (var i = 0; i < mats.length; i++) {
                prompt += "\r\n#i" + mats[i] + "# " + matQty[i] * qty + " #t" + mats[i] + "#";
            }
        } else {
            prompt += "\r\n#i" + mats + "# " + matQty * qty + " #t" + mats + "#";
        }

        if (cost > 0) {
            prompt += "\r\n#i4031138# " + cost * qty + " meso";
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
            cm.sendOk("恐怕你的背包空间不够。");
        } else if (cm.getMeso() < cost * qty) {
            cm.sendOk("恐怕你支付不起我的服务费。");
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
                cm.sendOk("请检查并确认您是否携带了所有必要的物品。如果是的话，请检查您的杂项物品栏，看看是否有空位。");
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
                cm.sendOk("好了，完成了。你觉得怎么样，是不是一件艺术品？嗯，如果你需要其他东西，你知道在哪里找我。");
            }
        }

        cm.dispose();
    }
}