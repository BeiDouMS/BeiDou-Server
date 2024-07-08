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
    [1462052, 1],	// Raven's Eye
    [1462006, 1], 	// Silver Crow
    [1462009, 1],	// Gross Jaeger
    [1452012, 1],	// Marine Arund
    [1472031, 1],        // Black Mamba
    [2044701, 1],        // Claw for ATT 60%
    [2044501, 1],        // Bow for ATT 60%
    [3010041, 1],        // Skull Throne
    [0, 750000]];       // Mesos

eQuestPrizes[1] = [[1332077, 1],  // Raven's Beak
    [1322062, 1],	// Crushed Skull
    [1302068, 1], 	// Onyx Blade
    [4032016, 1],        // Tao of Sight
    [2043001, 1],        // One Handed Sword for Att 60%
    [2043201, 1],        // One Handed BW for Att 60%
    [2044401, 1],        // Polearm for Att 60%
    [2044301, 1],        // Spear for Att 60%
    [3010041, 1],        // Skull Throne
    [0, 1250000]];       // Mesos

eQuestPrizes[2] = [[1472072, 1],   //Raven's Claw
    [1332077, 1],	// Raven's Beak
    [1402048, 1], 	// Raven's Wing
    [1302068, 1],        // Onyx Blade
    [4032017, 1],        // Tao of Harmony
    [4032015, 1],        // Tao of Shadows
    [2043023, 1],        // One-Handed Sword for Att 100%[2]
    [2043101, 1],        // One-Handed Axe for Att 60%
    [2043301, 1],        // Dagger for Att 60%
    [3010040, 1],        // The Stirge Seat
    [0, 2500000]];       // Mesos

eQuestPrizes[3] = [[1002801, 1],   //Raven Ninja Bandana
    [1382008, 1],	// Kage
    [1382006, 1], 	// Thorns
    [4032016, 1],        // Tao of Sight
    [4032015, 1],        // Tao of Shadows
    [2043701, 1],        // Wand for Magic Att 60%
    [2043801, 1],        // Staff for Magic Att 60%
    [3010040, 1],        // The Stirge Seat
    [0, 1750000]];       // Mesos

eQuestPrizes[4] = new Array([0, 3500000]);	// Mesos
eQuestPrizes[5] = new Array([0, 3500000]);	// Mesos
eQuestPrizes[6] = new Array([0, 3500000]);	// Mesos
eQuestPrizes[7] = new Array([0, 3500000]);	// Mesos

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
        cm.sendOk("嗯...这对你来说不应该是个坏交易。在合适的时间来找我，你可能会得到一个更好的物品。无论如何，如果你改变主意了，就让我知道。");
        cm.dispose();
        return;
    }

    status++;
    if (status == 0) { // first interaction with NPC
        if (cm.getQuestStatus(8225) != 2) {
            cm.sendNext("嘿，我不是强盗，好吗？");
            cm.dispose();
            return;
        }

        cm.sendNext("嘿，有点时间吗？我的工作是在这里收集物品然后在其他地方出售，但最近怪物变得更加敌对，所以很难得到好的物品... 你觉得呢？想和我做点生意吗？");
    } else if (status == 1) {
        cm.sendYesNo("这个交易很简单。你给我我需要的东西，我给你你需要的东西。问题是，我和很多人打交道，所以我能提供的物品可能每次见到我的时候都会改变。你觉得怎么样？还想做吗？");
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

        cm.sendYesNo("让我看看，你想用我的东西交换你的 #b" + qnt + " #t" + requiredItem + "##k，对吧？在交易之前，请确保你的消耗品或其他物品栏有空位。现在，你想和我交易吗？");
    } else if (status == 4) {
        itemSet = (Math.floor(Math.random() * eQuestPrizes[lastSelection].length));
        reward = eQuestPrizes[lastSelection];
        prizeItem = reward[itemSet][0];
        prizeQuantity = reward[itemSet][1];
        if (!cm.haveItem(requiredItem, qnt)) {
            cm.sendOk("“嗯... 你确定你有 #b" + qnt + " #t" + requiredItem + "##k 吗？如果是的话，请检查一下你的物品栏是否已满。”");
        } else if (prizeItem == 0) {
            cm.gainItem(requiredItem, -qnt);
            cm.gainMeso(prizeQuantity);
            cm.sendOk("对于你的 #b" + qnt + " #t" + requiredItem + "##k，这里有 #b" + prizeQuantity + " 金币#k。你觉得怎么样？你喜欢我给你的物品吗？我打算在这里待一段时间，所以如果你收集更多物品，我随时可以交易…");
        } else if (!cm.canHold(prizeItem)) {
            cm.sendOk("你的使用等等物品栏似乎已经满了。你需要腾出空间才能和我交易！清理一下，然后找到我。");
        } else {
            cm.gainItem(requiredItem, -qnt);
            cm.gainItem(prizeItem, prizeQuantity);
            cm.sendOk("对于你的 #b" + qnt + " #t" + requiredItem + "##k，这是我的 #b" + prizeQuantity + " #t" + prizeItem + "##k。你觉得怎么样？你喜欢我给你的回报物品吗？我打算在这里待一段时间，所以如果你收集到更多物品，我随时可以交易…");
        }
        cm.dispose();
    }
}

function makeChoices(a) {
    var result = "Ok! First you need to choose the item that you'll trade with. The better the item, the more likely the chance that I'll give you something much nicer in return.\r\n";
    var qnty = [50, 25];

    for (var x = 0; x < a.length; x++) {
        result += " #L" + x + "##v" + a[x] + "#  #b#t" + a[x] + "# #kx " + qnty[Math.floor(x / 4)] + "#l\r\n";
    }
    return result;
}