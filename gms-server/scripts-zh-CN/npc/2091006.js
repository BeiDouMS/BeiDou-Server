var status = 0;
function start() {
    status = -1;
    action(1, 0, 0);
}
function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendSimple("#e< 注意 >#n\r\n冒险者，你勇气挑战武陵道场吗？ \r\n\r\n#b#L0#挑战武陵道场。#l\r\n#L1#更详细地阅读通知。#l");
        } else if (status == 1) {
            if(selection === 0){
                cm.sendYesNo("你现在就想去武陵道场吗？");
            } else if(selection === 1){
                cm.sendNext("#e< 注意：接受挑战！ >#n\r\n我的名字是慕容，慕龙道场的主人。自古以来，我一直在慕龙修炼，直到我的技能达到了巅峰。从今天开始，我将接受所有对慕龙道场的申请者。慕龙道场的权利将只赋予最强大的人。\r\n如果有人希望向我学习，随时来挑战吧！如果有人希望挑战我，也欢迎。我会让你充分意识到自己的弱点。");
                status = -1;
            } else {
                cm.dispose();
            }
        } else if (status == 2) {
            cm.getPlayer().saveLocation("MIRROR");
            cm.warp(925020000, 4);
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}
