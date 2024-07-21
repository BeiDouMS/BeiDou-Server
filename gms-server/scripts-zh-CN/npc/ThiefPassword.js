var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 0)) {
        cm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }


    if (status == 0) {
        cm.sendGetText("一个可疑的声音打破了沉默。 #b暗号#k ！");
    } else if (status == 1) {
        if (cm.getText() == "芝麻开门") {
            if (cm.isQuestCompleted(3925)) {
                cm.warp(260010402, 1);
            } else {
                cm.playerMessage(5, "尽管你说出了正确的答案，但门依然没有动静！");
            }

            cm.dispose();
        } else {
            cm.sendOk("#r错误！");
        }
    } else if (status == 2) {
        cm.dispose();
    }
}