/* Strange Looking Statue
	Puppeteer's Secret Passage (910510100)
	Puppeteer JQ.
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            cm.sendYesNo("你会退出这个试炼吗？");
        } else {
            cm.warp(105040201, 2);
            cm.dispose();
        }
    }
}