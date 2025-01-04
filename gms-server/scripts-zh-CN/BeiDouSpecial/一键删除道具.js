var status;
var text;
var column = ["装备", "消耗", "设置", "其他", "商城"];
var sel;


function start() {
    levelStart();
}

// 对话开始
function levelStart() {
    text = "#e删除道具#n\r\n\r\n";
    for (let i = 1; i <= 5; i++) {
        text += "#L" + i + "#删除" + column[i-1] + "栏的道具#l\r\n";
    }
    // 选择删除哪一栏
    cm.sendNextSelectLevel("ChooseInventory", text);
}

// 选择了背包栏
function levelChooseInventory(choose) {
    sel = choose;
    // 选择全部清除，还是删除指定
    cm.sendSelectLevel("ChooseType", "#L1#清除所有道具#l\r\n#L2#删除指定道具#l\r\n");
}

// 选择了删除方式1
function levelChooseType1() {
    // 选择否回到levelStart，选择是执行levelDoClear
    cm.sendYesNoLevel("Start", "DoClear", "#r是否要清除" + column[sel-1] + "栏的所有道具？？？此操作不可逆！#k");
}

// 选择了删除方式2
function levelChooseType2() {
    text = "选择要删除的道具\r\n\r\n";
    let hasVal = false;
    for (let i = 0; i < 96; i++) {
        let item = cm.getInventory(sel).getItem(i);
        if (item) {
            hasVal = true;
            text += "#L" + item.getPosition() + "##t" + item.getItemId() + "##i" + item.getItemId() + "##l\r\n";
        }
    }
    if (!hasVal) {
        // 回到levelStart
        cm.sendNextLevel("Start", "背包栏下没有道具！");
        return;
    }
    // 选择单个道具
    cm.sendNextSelectLevel("DoRemove", text);
}

// 是否清除选择了是
function levelDoClear() {
    cm.removeAllByInventory(sel);
    // 回到levelStart
    cm.sendOkLevel("Start", "清除完毕！");
}

// 执行删除操作
function levelDoRemove(choose) {
    cm.removeAllByInventorySlot(sel, choose);
    // 回到选择单个道具
    cm.sendNextLevel("ChooseType2", "清除完毕！");
}
