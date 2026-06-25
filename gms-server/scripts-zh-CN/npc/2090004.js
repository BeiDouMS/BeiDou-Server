/* @author aaroncsn <MapleSea Like>
 * @author Ronan
    NPC Name:       Mr. Do
    Map(s):         Mu Lung: Mu Lung(2500000000)
    Description:    Potion Creator
 */

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var matMeso;
var rewdSet;
var makeQty = 1;

var itemSet;
var matSet;
var matQtySet;
var matQtyMeso;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        cm.sendOk("想清楚需要我帮你做什么之后，再来找我吧。我现在可是很忙的。");
        cm.dispose();
        return;
    }

    if (status == 0) {
        if (cm.isQuestActive(3821) && !cm.haveItem(4031554) && !cm.haveItem(4161030) && cm.isQuestCompleted(3830)) {
            // player lost his book, help him complete quest anyways
            if (cm.canHold(4031554)) {
                cm.sendOk("哦，那个孩子让你来拿#t4031554#吗？没问题，我本来也欠他一个人情。告诉他，这就算我还债了。明白吗？");
                cm.gainItem(4031554, 1);
                cm.dispose();
                return;
            } else {
                cm.sendOk("哦，那个孩子让你来拿#t4031554#吗？先在你的其他栏里空出位置再来吧。");
                cm.dispose();
                return;
            }
        }
        var selStr = "我会的东西可不少。说吧，你想让我帮你做什么？#b";
        var options = ["制作药品", "制作卷轴", "捐赠药材"];
        for (var i = 0; i < options.length; i++) {
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        }

        cm.sendSimple(selStr);
    } else if (status == 1) {
        selectedType = selection;
        var selStr;
        if (selectedType == 0) { // Make a medicine
            itemSet = [2022145, 2022146, 2022147, 2022148, 2022149, 2022150, 2050004, 4031554];
            matSet = [2022116, 2022116, [4000281, 4000293], [4000276, 2002005], [4000288, 4000292], 4000295, [2022131, 2022132], [4000286, 4000287, 4000293]];
            matQtySet = [3, 3, [10, 10], [20, 1], [20, 20], 10, [1, 1], [20, 20, 20]];
            matQtyMeso = [0, 0, 910, 950, 1940, 600, 700, 1000];

            if (!cm.haveItem(4161030)) {
                cm.sendNext("如果想制作药品，你必须先读过《草药图鉴》。没有足够知识就随便配药，可是非常危险的事。");
                cm.dispose();
                return;
            }

            selStr = "你想制作哪一种药品？#b";

            for (var i = 0; i < itemSet.length; i++) {
                selStr += "\r\n#L" + i + "# #v" + itemSet[i] + "# #t" + itemSet[i] + "##l";
            }
            selStr += "#k";
        } else if (selectedType == 1) { // Make a scroll
            status++;

            selStr = "你想制作哪一种卷轴？#b";
            itemSet = ["单手剑攻击力卷轴", "单手斧攻击力卷轴", "单手钝器攻击力卷轴",
                "短剑攻击力卷轴", "短杖魔力卷轴", "长杖魔力卷轴",
                "双手剑攻击力卷轴", "双手斧攻击力卷轴", "双手钝器攻击力卷轴",
                "枪攻击力卷轴", "矛攻击力卷轴", "弓攻击力卷轴", "弩攻击力卷轴",
                "拳套攻击力卷轴", "指节攻击力卷轴", "火枪攻击力卷轴#k"];

            for (var i = 0; i < itemSet.length; i++) {
                selStr += "\r\n#L" + i + "# " + itemSet[i] + "#l";
            }
        } else { // Donate medicine ingredients
            status++;

            selStr = "你想捐赠药材吗？这可是好事！药材以#b100个#k为一组接受捐赠，捐赠者会获得可用于制作卷轴的珠子。你想捐赠哪一种？#b";
            itemSet = [4000276, 4000277, 4000278, 4000279, 4000280, 4000291, 4000292, 4000286, 4000287, 4000293, 4000294, 4000298, 4000284, 4000288, 4000285, 4000282, 4000295, 4000289, 4000296, 4000297];

            for (var i = 0; i < itemSet.length; i++) {
                selStr += "\r\n#L" + i + "# #v" + itemSet[i] + "# #t" + itemSet[i] + "##l";
            }
        }

        cm.sendSimple(selStr);
    } else if (status == 2) {
        selectedItem = selection;
        cm.sendGetText("你想制作多少个#b#t" + itemSet[selectedItem] + "##k？");
    } else if (status == 3) {
        if (selectedType == 0) { // Medicines
            var text = cm.getText();
            makeQty = parseInt(text);
            if (isNaN(makeQty)) {
                makeQty = 1;
            }

            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            matMeso = matQtyMeso[selectedItem];

            var prompt = "你想制作#b" + makeQty + "个#t" + item + "##k吗？制作这些#t" + item + "#需要以下材料：\r\n";
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++) {
                    prompt += "\r\n#i" + mats[i] + "# " + matQty[i] * makeQty + "? #t" + mats[i] + "#";
                }
            } else {
                prompt += "\r\n#i" + mats + "# " + matQty * makeQty + "? #t" + mats + "#";
            }

            if (matMeso > 0) {
                prompt += "\r\n#i4031138# " + matMeso * makeQty + " ??";
            }

            cm.sendYesNo(prompt);
        } else if (selectedType == 1) { // Scrolls
            selectedItem = selection;

            itemSet = [2043000, 2043100, 2043200, 2043300, 2043700, 2043800, 2044000, 2044100, 2044200, 2044300, 2044400, 2044500, 2044600, 2044700, 2044800, 2044900];
            matSet = [[4001124, 4010001], [4001124, 4010001], [4001124, 4010001], [4001124, 4010001], [4001124, 4010001],
                [4001124, 4010001], [4001124, 4010001], [4001124, 4010001], [4001124, 4010001], [4001124, 4010001], [4001124, 4010001],
                [4001124, 4010001], [4001124, 4010001], [4001124, 4010001], [4001124, 4010001], [4001124, 4010001]];
            matQtySet = [[100, 10], [100, 10], [100, 10], [100, 10], [100, 10], [100, 10], [100, 10],
                [100, 10], [100, 10], [100, 10], [100, 10], [100, 10], [100, 10], [100, 10], [100, 10],
                [100, 10]];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            var prompt = "你想制作#b#t" + item + "##k吗？制作#t" + item + "#需要以下材料：";
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++) {
                    prompt += "\r\n#i" + mats[i] + "# " + matQty[i] + "? #t" + mats[i] + "#";
                }
            } else {
                prompt += "\r\n#i" + mats + "# " + matQty + "? #t" + mats + "#";
            }

            cm.sendYesNo(prompt);
        } else if (selectedType == 2) {
            selectedItem = selection;

            itemSet = [4000276, 4000277, 4000278, 4000279, 4000280, 4000291, 4000292, 4000286, 4000287, 4000293, 4000294, 4000298, 4000284, 4000288, 4000285, 4000282, 4000295, 4000289, 4000296, 4000297];
            rewdSet = [7, 7, [7, 8], 10, 11, 8, [7, 8], [7, 9], [7, 8], 9, 10, [10, 11], 11, [11, 12], 13, 13, 14, 15, [15, 16], 17];

            item = itemSet[selectedItem];
            var prompt = "确定要捐赠#b100个#t" + item + "##k吗？";
            cm.sendYesNo(prompt);
        }
    } else if (status == 4) {
        if (selectedType == 0) {
            var complete = true;
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++) {
                    if (!cm.haveItem(mats[i], matQty[i] * makeQty)) {
                        complete = false;
                    }
                }
            } else {
                if (!cm.haveItem(mats, matQty * makeQty)) {
                    complete = false;
                }
            }

            if (cm.getMeso() < matMeso * makeQty) {
                complete = false;
            }

            if (!complete || !cm.canHold(item, makeQty)) {
                cm.sendOk("请确认材料和金币足够，并且消耗栏有足够空间。");
            } else {
                if (mats instanceof Array) {
                    for (var i = 0; i < mats.length; i++) {
                        cm.gainItem(mats[i], -matQty[i] * makeQty);
                    }
                } else {
                    cm.gainItem(mats, -matQty * makeQty);
                }

                if (matMeso > 0) {
                    cm.gainMeso(-matMeso * makeQty);
                }
                cm.gainItem(item, makeQty);
            }

            cm.dispose();
        } else if (selectedType == 1) {
            var complete = true;
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++) {
                    if (!cm.haveItem(mats[i], matQty[i])) {
                        complete = false;
                    }
                }
            } else {
                if (!cm.haveItem(mats, matQty)) {
                    complete = false;
                }
            }

            if (Math.random() >= 0.9) { // A lucky find! Scroll 60%
                item += 1;
            }

            if (!complete || !cm.canHold(item, 1)) {
                cm.sendOk("请确认材料足够，并且消耗栏有足够空间。");
            } else {
                if (mats instanceof Array) {
                    for (var i = 0; i < mats.length; i++) {
                        cm.gainItem(mats[i], -matQty[i]);
                    }
                } else {
                    cm.gainItem(mats, -matQty);
                }

                cm.gainItem(item, 1);
            }

            cm.dispose();
        } else if (selectedType == 2) {
            var complete = true;

            if (!cm.haveItem(item, 100)) {
                complete = false;
            }

            if (!complete) {
                cm.sendOk("请确认药材数量足够，并且其他栏有足够空间。");
                cm.dispose();
                return;
            }

            var reward;
            if (rewdSet[selectedItem] instanceof Array) {
                var length = rewdSet[selectedItem][1] - rewdSet[selectedItem][0];
                reward = rewdSet[selectedItem][0] + Math.round(Math.random() * length);
            } else {
                reward = rewdSet[selectedItem];
            }

            if (!cm.canHold(4001124, reward)) {
                cm.sendOk("请确认其他栏有足够空间。");
            } else {
                cm.gainItem(item, -100);
                cm.gainItem(4001124, reward);
            }

            cm.dispose();
        }
    }
}
