var status = -1;

function start(mode, type, selection) {
    if (mode == 0 && type == 0) {
        status--;
    } else if (mode == -1) {
        qm.dispose();
        return;
    } else {
        status++;
    }
    if (status == 0) {
        qm.sendNext("吱吱吱吱吱！吱！吱！");
    } else if (status == 1) {
        qm.sendNextPrev("肚子吃饱了，但精神还是不清醒……到底是怎么回事？一睁开眼睛看到的是猴子，不知道这是什么地方……船怎么样了呢？你知道怎么回事吗？", 2);
    } else if (status == 2) {
        qm.sendAcceptDecline("吱吱，吱吱！ (猴子点着头。它真的知道情况吗？仔细问问猴子吧！)");
    } else if (status == 3) {
        if (mode == 0) { //decline
            qm.sendNext("吱吱，吱吱！(猴子看起来很不满意。)");
        } else {
            qm.forceStartQuest();
            qm.dispose();
        }
    } else if (status == 4) {
        qm.dispose();
    }
}
	
