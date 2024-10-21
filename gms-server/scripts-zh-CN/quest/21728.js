var status = -1;

function end(mode, type) {
    if (mode == -1) { // END CHAT
        qm.dispose();
        return;
    }

    status++;
    switch (status) {
        case 0:
            qm.sendSimple("你到火独眼兽洞穴里去看过了吗？那个孩子真的住在火独眼兽洞穴里吗？\r\n#b#L0#(讲述在人偶师的洞穴入口附近转了一圈又回来的事情。)#l\n#k");
            break;
        case 1:
            if (type == 4 && mode == 0) { // END CHAT
                qm.dispose();
                break;
            }
            qm.sendOk("看来他真的住在那里啊……居然还有暗号……是谁在那里设置了暗号呢？\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 200 exp");
            break;
        case 2:
            qm.forceCompleteQuest();
            qm.gainExp(200);
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}