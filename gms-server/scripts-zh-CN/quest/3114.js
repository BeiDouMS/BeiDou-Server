var status = -1;


function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 0) {
     if(qm.getQuestProgress(3114) == 42){
        qm.sendNext("完成了吗？谢谢您，冒险家。多亏了您的帮助，我可以安稳入睡了...");
        }else{
        qm.sendNext("你还没完成。");
        qm.dispose();
        }
    } else if (status == 1) {
        qm.forceCompleteQuest();
        qm.gainFame(20);
        qm.sendNext("再见，希望我们还能再见到面。");
        qm.dispose();
    }
}
