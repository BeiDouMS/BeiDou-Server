/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Joko <Crimsonwood Exchange Quest> - Phantom Forest: Dead Man's Gorge(610010004)
 -- By ---------------------------------------------------------------------------------------------
 Ronan Lana
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Ronan Lana
 ---------------------------------------------------------------------------------------------------
 **/

var status = 0;
var eQuestChoices = [4032007, 4032006, 4032009, 4032008, 4032007, 4032006, 4032009, 4032008];

var eQuestPrizes = [];

eQuestPrizes[0] = [[1002801, 1],  // Raven Ninja Bandana
    [1462052, 1],   // Raven's Eye
    [1462006, 1],   // Silver Crow
    [1462009, 1],   // Gross Jaeger
    [1452012, 1],   // Marine Arund
    [1472031, 1],        // Black Mamba
    [2044701, 1],        // Claw for ATT 60%
    [2044501, 1],        // Bow for ATT 60%
    [3010041, 1],        // Skull Throne
    [0, 750000]];       // 金币

eQuestPrizes[1] = [[1332077, 1],  // Raven's Beak
    [1322062, 1],   // Crushed Skull
    [1302068, 1],   // Onyx Blade
    [4032016, 1],        // Tao of Sight
    [2043001, 1],        // 单手剑 ATT 60%
    [2043201, 1],        // 单手斧 ATT 60%
    [2044401, 1],        // 长柄武器 ATT 60%
    [2044301, 1],        // 长枪 ATT 60%
    [3010041, 1],        // Skull Throne
    [0, 1250000]];       // 金币

eQuestPrizes[2] = [[1472072, 1],   // Raven's Claw
    [1332077, 1],   // Raven's Beak
    [1402048, 1],   // Raven's Wing
    [1302068, 1],        // Onyx Blade
    [4032017, 1],        // Tao of Harmony
    [4032015, 1],        // Tao of Shadows
    [2043023, 1],        // 单手剑 ATT 100%[2]
    [2043101, 1],        // 单手斧 ATT 60%
    [2043301, 1],        // 匕首 ATT 60%
    [3010040, 1],        // The Stirge Seat
    [0, 2500000]];       // 金币

eQuestPrizes[3] = [[1002801, 1],   // Raven Ninja Bandana
    [1382008, 1],   // Kage
    [1382006, 1],   // Thorns
    [4032016, 1],        // Tao of Sight
    [4032015, 1],        // Tao of Shadows
    [2043701, 1],        // 魔杖 魔法 ATT 60%
    [2043801, 1],        // 法杖 魔法 ATT 60%
    [3010040, 1],        // The Stirge Seat
    [0, 1750000]];       // 金币

eQuestPrizes[4] = new Array([0, 3500000]);  // 金币
eQuestPrizes[5] = new Array([0, 3500000]);  // 金币
eQuestPrizes[6] = new Array([0, 3500000]);  // 金币
eQuestPrizes[7] = new Array([0, 3500000]);  // 金币

var requiredItem = 0;
var lastSelection = 0;
var prizeItem = 0;
var prizeQuantity = 0;
var itemSet;
var qnt;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode <= 0) {
        cm.sendOk("嗯……这对你来说不算坏交易。在合适的时间来找我，你可能会得到更好的物品。无论如何，如果你改变主意了，就告诉我。");
        cm.dispose();
        return;
    }

    status++;
    if (status == 0) { // first interaction with NPC
        if (cm.getQuestStatus(8225) != 2) {
            cm.sendNext("嘿，我可不是强盗，好吗？");
            cm.dispose();
            return;
        }

        cm.sendNext("嘿，有时间吗？我的工作是收集这些物品然后在其他地方卖掉，但最近怪物变得更凶，所以很难得到好的材料……你觉得呢？想和我做点交易吗？");
    } else if (status == 1) {
        cm.sendYesNo("交易很简单。你给我我需要的东西，我给你你想要的东西。不过问题是，我要跟很多人打交道，所以每次你来，我能提供的物品可能会不同。你觉得怎么样？还想继续吗？");
    } else if (status == 2) {
        var eQuestChoice = makeChoices(eQuestChoices);
        cm.sendSimple(eQuestChoice);
    } else if (status == 3) {
        lastSelection = selection;
        requiredItem = eQuestChoices[selection];

        if (selection < 4) {
            qnt = 50;
        } else {
            qnt = 25;
        }

        cm.sendYesNo("让我看看，你想用我的东西交换 #b" + qnt + " #t" + requiredItem + "##k，对吗？在交易之前，请确保你的消耗栏或其他物品栏有足够空位。现在，你想交易吗？");
    } else if (status == 4) {
        itemSet = (Math.floor(Math.random() * eQuestPrizes[lastSelection].length));
        reward = eQuestPrizes[lastSelection];
        prizeItem = reward[itemSet][0];
        prizeQuantity = reward[itemSet][1];
        if (!cm.haveItem(requiredItem, qnt)) {
            cm.sendOk("嗯……你确定你有 #b" + qnt + " #t" + requiredItem + "##k 吗？如果有的话，请先确认物品栏是否已满。");
        } else if (prizeItem == 0) {
            cm.gainItem(requiredItem, -qnt);
            cm.gainMeso(prizeQuantity);
            cm.sendOk("这是你的 #b" + qnt + " #t" + requiredItem + "##k，我给你 #b" + prizeQuantity + " 金币#k。你满意吗？我会在这里待一段时间，所以如果你收集更多材料，随时可以来交易…");
        } else if (!cm.canHold(prizeItem)) {
            cm.sendOk("你的使用栏或其他物品栏似乎已满。请腾出空间再来交易！");
        } else {
            cm.gainItem(requiredItem, -qnt);
            cm.gainItem(prizeItem, prizeQuantity);
            cm.sendOk("这是你的 #b" + qnt + " #t" + requiredItem + "##k，我给你 #b" + prizeQuantity + " #t" + prizeItem + "##k。你满意吗？我会在这里待一段时间，所以如果你收集更多材料，随时可以来交易…");
        }
        cm.dispose();
    }
}

function makeChoices(a) {
    var result = "好的！首先选择你要交易的物品。物品越好，我回报给你的奖励可能就越好。\r\n";
    var qnty = [50, 25];

    for (var x = 0; x < a.length; x++) {
        result += " #L" + x + "##v" + a[x] + "#  #b#t" + a[x] + "# #kx " + qnty[Math.floor(x / 4)] + "#l\r\n";
    }
    return result;
}
