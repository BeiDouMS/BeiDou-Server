/**
 * @author: lee
 * @npc: 阿得拉
 * @map: Multiple towns on MapleStory
 * @func: 回收商人
 */

let status;
let sel;
let scroll100 = [
    2041009, 2040000, 2040003, 2040300, 2040400, 2040500, 2040503, 2040600, 2040700, 2040703, 2040706,
    2040800, 2040900, 2041000, 2041003, 2043000, 2043100, 2043200, 2043300, 2043700, 2043800, 2044000,
    2044100, 2044200, 2044300, 2044400, 2044500, 2044600, 2044700, 2044800, 2044900
];
let scroll60 = [
    // 60%卷轴
    2040901, 2040931, 2040927, 2040924, 2040311, 2040317, 2040326, 2040321, 2040601, 2040625, 2040621,
    2041004, 2041013, 2041019, 2041001, 2041010, 2041007, 2041022, 2041016, 2040504, 2040501, 2040516,
    2040513, 2040532, 2040401, 2040418, 2040421, 2040425, 2040413, 2040801, 2040824, 2040001, 2040029,
    2040017, 2040004, 2040025, 2040701,
    // 物攻>0
    2044801, 2044807, 2044101, 2044701, 2043301, 2044601, 2044112, 2044501, 2044001, 2044012, 2044401,
    2040914, 2044412, 2040804, 2040826, 2044301, 2040759, 2043101, 2044901, 2044312, 2043112, 2044201,
    2044212, 2043001, 2043017,
    // 魔攻>0
    2043009, 2040919, 2040817, 2043801, 2040301, 2043701,
    // 移速
    2040707, 2040613, 2048001,
    // 跳跃
    2040704, 2040618, 2048004,
    // 腰带属性
    2041304, 2041307, 2041301, 2041310,
    // 戒指属性
    2041107, 2041104, 2041101, 2041110,
    // 宠物属性
    2048012, 2048011, 2048010, 2048013,

];
let scroll10 = [
    // 10%卷轴
    2040902, 2040933, 2040928, 2040925, 2040310, 2040329, 2040318, 2040323, 2040331, 2040328, 2040602,
    2040622, 2041058, 2041005, 2041014, 2041020, 2041002, 2041011, 2041008, 2041023, 2041017, 2040505,
    2040517, 2040514, 2040534, 2040402, 2040419, 2040422, 2040427, 2040412, 2040802, 2040825, 2040002,
    2040031, 2040016, 2040005, 2040026,
    // 腰带属性
    2041305, 2041302, 2041308, 2041311,
    // 戒指属性
    2041108, 2041102, 2041105, 2041111,
    // 物攻>0
    2044809, 2044702, 2044602, 2044502, 2043302, 2044802, 2044402, 2044414, 2043202, 2043214, 2044302,
    2044314, 2043102, 2044014, 2044015, 2044102, 2044202, 2043114, 2044214, 2044114, 2040805, 2043002,
    2044002, 2040760, 2044902, 2043019, 2040915,
    // 魔攻>0
    2040330, 2043008, 2040920, 2040816, 2043802, 2040302, 2043702,
    // 移速
    2040702, 2040708, 2040612, 2040502, 2040627, 2048002,
    // 跳跃
    2040705, 2040619, 2048005,
    // 鞋子防滑
    2040727
];
let tempId, tempQty;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode === -1 || (mode === 0 && type > 0)) {
        cm.dispose();
        return;
    }

    updateStatus(mode);

    if (status === 0) {
        sel = undefined;
        cm.sendSimple("你好旅行者，我是 #p9209000#，如果你有什么不想要的道具，我可以出个好价钱！\r\n\r\n" +
            generateMenu(['回收卷轴'])
        );
    } else if (status === 1 && selection === 1 || sel === 1) {
        sellScroll(selection);
    } else if (status === 1 && selection === 2 || sel === 2) {
        cm.sendOk("Ok");
        cm.dispose();
    }
}

function sellScroll(selection) {
    sel = 1;
    if (status === 1) {
        let menu = generateScrollMenu(scroll100)
            + generateScrollMenu(scroll60)
            + generateScrollMenu(scroll10);
        let str = "#e卷轴统一回收价：100% #b5k#k / 60% #b1w#k / 10% #b2w#n\r\n\r\n"
            + menu;
        if (menu === "") {
            cm.sendOk(str);
            cm.dispose();
        } else {
            cm.sendSimple(str);
        }

    } else if (status === 2) {
        tempId = selection;
        tempQty = cm.getItemQuantity(selection);
        cm.sendYesNo("你确定要回收 #r" + tempQty + "#k 张 #b#z" + tempId + "##k？");
    } else if (status === 3) {
        if (cm.getItemQuantity(tempId) < tempQty) {
            cm.sendOk("emm...")
            cm.dispose();
            return;
        }

        let extra = false;
        let fee = tempQty * getScrollFee();
        let str = "总共是 #b" + fee + "#k 金币，请收好！"

        // 抽奖 1/1000 概率获得祝福卷轴
        let randomNumber = Math.random();
        console.log(randomNumber)
        if (randomNumber <= (0.001 * tempQty)) {
            extra = true;
            str += "\r\n\r\n#r恭喜你！#k幸运地获得了一张#b#z2340000#"
        }

        cm.gainItem(tempId, -tempQty);
        cm.gainMeso(fee);
        if (extra) {
            cm.gainItem(2340000, 1);
        }
        cm.sendOk(str);
        cm.dispose();
    }
}

function getScrollFee() {
    if (scroll100.includes(tempId)) return 5000;
    if (scroll60.includes(tempId)) return 10000;
    if (scroll10.includes(tempId)) return 20000;
    return 0;
}

function generateScrollMenu(data) {
    let str = "";
    for (let i = 0; i < data.length; i++) {
        let id = data[i];
        if (cm.haveItem(id)) {
            str += "#L" + id + "##v" + id + "##b#z" + id + "##k (#r#e" + cm.getItemQuantity(id) + "#k#n张)#l\r\n";
        }
    }
    return str;
}

function updateStatus(mode) {
    if (mode === 1) {
        status++;
    } else {
        status--;
    }
}

function generateMenu(array) {
    let menu = "";
    for (let i = 1; i <= array.length; i++) {
        menu += "#L" + i + "##b" + i + ". " + array[i - 1] + "#k#l\r\n";
    }
    return menu;
}