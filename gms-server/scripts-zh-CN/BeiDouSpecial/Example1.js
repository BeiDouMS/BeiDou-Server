/**
 * @description 示例脚本1-非nextLevel实现
 * @author 昨日小睡
 */

var status;
var firstSelection;
const BEI_DOU_SATELLITE_MANUAL = 2430033;
const MAPLE_LEAF = 4001126;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode === 1) {
        status++;
    } else {
        status--;
    }

    if (status === 0) {
        let text = "用以下物品可兑换 #r#t2430033##k #i2430033# 一本\r\n\r\n";
        text += "#L0#1000万金币#l\r\n";
        text += "#L1#1000点券#l\r\n";
        text += "#L2#1万枫叶#l\r\n";
        text += "#L3#兑换码#l\r\n";
        cm.sendSimple(text);
    } else if (status === 1) {
        firstSelection = selection;
        if (selection === 0) {
            if (cm.getMeso() < 10000000) {
                cm.sendOk("金币不足");
                cm.dispose();
            } else if (!cm.canHold(BEI_DOU_SATELLITE_MANUAL, 1)) {
                cm.sendOk("背包空间不足");
                cm.dispose();
            } else {
                cm.gainMeso(-10000000);
                successGain(1);
            }
        } else if (selection === 1) {
            if (cm.getPlayer().getCashShop().getCash(1) < 1000) {
                cm.sendOk("点券不足");
                cm.dispose();
            } else if (!cm.canHold(BEI_DOU_SATELLITE_MANUAL, 1)) {
                cm.sendOk("背包空间不足");
                cm.dispose();
            } else {
                cm.getPlayer().getCashShop().gainCash(1, -1000);
                successGain(1);
            }
        } else if (selection === 2) {
            let itemQuantity = cm.getItemQuantity(MAPLE_LEAF);
            if (itemQuantity >= 10000) {
                cm.sendGetNumber("请输入兑换数量", 1, 1, 999);
            } else {
                cm.sendOk("枫叶不足");
                cm.dispose();
            }
        } else if (selection === 3) {
            cm.sendGetText("请输入兑换码");
        } else {
            cm.dispose();
        }
    } else if (status === 2) {
        if (firstSelection === 2) {
            let itemQuantity = cm.getItemQuantity(MAPLE_LEAF);
            let cost = selection * 10000;
            if (itemQuantity < cost) {
                cm.sendOk("枫叶不足");
                cm.dispose();
            } else if (!cm.canHold(BEI_DOU_SATELLITE_MANUAL, selection)) {
                cm.sendOk("背包空间不足");
                cm.dispose();
            } else {
                cm.gainItem(MAPLE_LEAF, -cost);
                successGain(selection);
            }
        } else if (firstSelection === 3) {
            const inputCode = cm.getText();
            if ("BeiDou_YYDS" === inputCode) {
                if (cm.canHold(BEI_DOU_SATELLITE_MANUAL, 1)) {
                    successGain(1);
                } else {
                    cm.sendOk("背包空间不足");
                    cm.dispose();
                }
            } else {
                cm.sendOk("兑换码错误");
                cm.dispose();
            }
        } else {
            cm.dispose();
        }
    } else {
        cm.dispose();
    }
}

function successGain(quantity) {
    cm.gainItem(BEI_DOU_SATELLITE_MANUAL, quantity);
    cm.sendOk("兑换成功");
    cm.dispose();
}