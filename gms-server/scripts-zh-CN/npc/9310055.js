var status = -1;



function start() {

    action(1, 0, 0);

}



function action(mode, type, selection) {

    if (mode == -1 || (status >= 0 && mode == 0)) {

        cm.dispose();

        return;

    }

    if (mode == 1) {

        status++;

    } else {

        status--;

    }



    if (status == 0) {

        cm.sendYesNo("嘿！我是热气球操纵员小胖。想从上海外滩搭乘热气球，前往#b#m701010320##k看看吗？");

    } else if (status == 1) {

        cm.sendNext("抓稳了，热气球马上起飞！");

    } else if (status == 2) {

        cm.warp(701010320, "h-top");

        cm.dispose();

    }

}


