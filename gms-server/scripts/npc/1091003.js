/*
	Serryl (1091003)
	Location: The Nautilus
*/

/**
 Author: xQuasar
 */

 function start() {
    cm.getPlayer().setCS(true);
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        cm.dispose();
    }
    if (status == 0 && mode == 1) {
        var selStr = "什么？你想自己做武器和手套吗？说真的。。。如果你没有经验，很难自己去做。。。我会帮你的。我当海盗已经20年了，20年来，我为这里的船员制作了各种物品。这对我来说很容易。";
        var options = ["做一个拳甲", "做一把手铳", "制作一副手套"];
        for (var i = 0; i < options.length; i++) {
            selStr += "\r\n#b#L" + i + "# " + options[i] + "#l#k";
        }

        cm.sendSimple(selStr);
    } else if (status == 1 && mode == 1) {
        selectedType = selection;
        if (selectedType == 0) { //Making a Knuckler
            var selStr = "只要你带上所需的材料，我就给你做一个好的拳甲。你想做哪个拳甲？";
            var knucklers = ["拳甲 (等级要求: 15, 海盗)", "格斗指虎 (等级要求: 20, 海盗)", "三日月冲拳 (等级要求: 25, 海盗)", "全覆式拳甲 (等级要求: 30, 海盗)", "双翼拳甲 (等级要求: 35, 海盗)", "刺棘拳甲 (等级要求: 40, 海盗)", "蛇吻 (等级要求: 50, 海盗)"];
            for (var i = 0; i < knucklers.length; i++) {
                selStr += "\r\n#b#L" + i + "# " + knucklers[i] + "#l#k";
            }
            equip = true;
            cm.sendSimple(selStr);
        } else if (selectedType == 1) { //Making a Gun
            var selStr = "只要你带上所需的材料，我就给你做一个好的手铳。你想做哪个手铳？";
            var guns = ["单发手铳 (等级要求: 15, 海盗)", "大型手铳 (等级要求: 20, 海盗)", "突击手铳 (等级要求: 25, 海盗)", "银枪 (等级要求: 30, 海盗)", "红杰克 (等级要求: 35, 海盗)", "黑郁金香 (等级要求: 40, 海盗)", "金钱豹 (等级要求: 50, 海盗)"];
            for (var i = 0; i < guns.length; i++) {
                selStr += "\r\n#b#L" + i + "# " + guns[i] + "#l#k";
            }
            equip = true;
            cm.sendSimple(selStr);
        } else if (selectedType == 2) { //Making a pair of pirate gloves
            var selStr = "只要你带上所需的材料，我就给你做一个好的手套。你想做哪个手套？";
            var gloves = ["胶皮手套", "皮腕轮", "铜腕轮", "摸鱼手", "鲨皮手套", "追命", "幽明", "搜魂"];
            for (var i = 0; i < gloves.length; i++) {
                selStr += "\r\n#b#L" + i + "# " + gloves[i] + "#l#k";
            }
            equip = true;
            cm.sendSimple(selStr);
        }
        if (equip) {
            status++;
        }
    } else if (status == 3 && mode == 1) {
        if (equip) {
            selectedItem = selection;
            qty = 1;
        } else {
            qty = (selection > 0) ? selection : (selection < 0 ? -selection : 1);
        }

        if (selectedType == 0) { //Making a Knuckler
            var itemSet = [1482001, 1482002, 1482003, 1482004, 1482005, 1482006, 1482007];
            var matSet = [4000021, [4011001, 4011000, 4000021, 4003000], [4011000, 4011001, 4003000], [4011000, 4011001, 4000021, 4003000], [4011000, 4011001, 4000021, 4003000], [4011000, 4011001, 4021000, 4000021, 4003000], [4000039, 4011000, 4011001, 4000030, 4000021, 4003000]];
            var matQtySet = [20, [1, 1, 10, 5], [2, 1, 10], [1, 1, 30, 10], [2, 2, 30, 20], [1, 1, 2, 50, 20], [150, 1, 2, 20, 20, 20]];
            var costSet = [1000, 2000, 5000, 15000, 30000, 50000, 100000];
            var levelLimitSet = [15, 20, 25, 30, 35, 40, 50];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
            levelLimit = levelLimitSet[selectedItem];
        } else if (selectedType == 1) { //Making a Gun
            var itemSet = [1492001, 1492002, 1492003, 1492004, 1492005, 1492006, 1492007];
            var matSet = [[4011000, 4003000, 4003001], [4011000, 4003000, 4003001, 4000021], [4011000, 4003000], [4011001, 4000021, 4003000], [4011006, 4011001, 4000021, 4003000], [4011004, 4011001, 4000021, 4003000], [4011006, 4011004, 4011001, 4000030, 4003000]];
            var matQtySet = [[1, 5, 1], [1, 10, 5, 10], [2, 10], [2, 10, 10], [10, 2, 5, 10], [1, 2, 10, 20], [1, 2, 4, 30, 30]];
            var costSet = [1000, 2000, 5000, 15000, 30000, 50000, 100000];
            var levelLimitSet = [15, 20, 25, 30, 35, 40, 50];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
            levelLimit = levelLimitSet[selectedItem];
        } else if (selectedType == 2) { //Making a pair of pirate gloves
            var itemSet = [1082180, 1082183, 1082186, 1082189, 1082192, 1082195, 1082198, 1082201];
            var matSet = [[4000021, 4021003], 4000021, [4011000, 4000021], [4021006, 4000021, 4003000], [4011000, 4000021, 4003000], [4000021, 4011000, 4011001, 4003000], [4011000, 4000021, 4000030, 4003000], [4011007, 4021008, 4021007, 4000030, 4003000]];
            var matQtySet = [[15, 1], 35, [2, 20], [2, 50, 10], [3, 60, 15], [80, 3, 3, 25], [3, 20, 40, 30], [1, 1, 1, 50, 50]];
            var costSet = [1000, 8000, 15000, 25000, 30000, 40000, 50000, 70000];
            var levelLimitSet = [15, 20, 25, 30, 35, 40, 50, 60];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
            levelLimit = levelLimitSet[selectedItem];
        }

        prompt = "制作 #t" + item + "# 需要以下条件. 等级要求 " + levelLimit + "级, 因此在获得此项目之前，请检查并确保您真的有这些材料。您怎么看？你真的想要一个吗？\r\n";

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
    } else if (status == 4 && mode == 1) {
        var pass = true;

        if (!cm.canHold(item)) {
            cm.sendOk("首先检查你的物品栏是否有空位。");
            cm.dispose();
            return;
        } else if (cm.getMeso() < cost * qty) {
            cm.sendNext("请确保你拥有制作这个物品所需的所有必要物品。另外，确保你的装备栏有足够的空间。如果你的背包已经满了，我就无法给你这个物品了，你知道的。");
            cm.dispose();
            return;
        } else {
            if (mats instanceof Array) {
                for (var i = 0; pass && i < mats.length; i++) {
                    if (!cm.haveItem(mats[i], matQty[i] * qty)) {
                        pass = false;
                    }
                }
            } else if (!cm.haveItem(mats, matQty * qty)) {
                pass = false;
            }
            /*if (mats instanceof Array) {
                for(var i = 0; pass && i < mats.length; i++)
                {
                    if (matQty[i] * qty == 1)	{
                        if (!cm.haveItem(mats[i]))
                        {
                            pass = false;
                        }
                    }
                    else {
                        var count = 0;
                        var iter = cm.getChar().getInventory(InventoryType.ETC).listById(mats[i]).iterator();
                        while (iter.hasNext()) {
                            count += iter.next().getQuantity();
                        }
                        if (count < matQty[i] * qty)
                            pass = false;
                    }
                }
            }
            else {
                var count = 0;
                var iter = cm.getChar().getInventory(InventoryType.ETC).listById(mats).iterator();
                while (iter.hasNext()) {
                    count += iter.next().getQuantity();
                }
                if (count < matQty * qty)
                    pass = false;
            }
                            */
        }

        if (pass == false) {
            cm.sendNext("请确保你拥有制作这个物品所需的所有必要物品。另外，确保你的装备栏有足够的空间。如果你的背包已经满了，我就无法给你这个物品了，你知道的。");
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

            if (item == 4003000)//screws
            {
                cm.gainItem(4003000, 15 * qty);
            } else {
                cm.gainItem(item, qty);
            }
            cm.sendOk("都搞定了。如果你还需要什么……嗯，我不会走开的。");
        }
        cm.dispose();
    }
}