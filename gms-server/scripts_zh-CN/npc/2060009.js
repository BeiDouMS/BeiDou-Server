var status = 0;
var menu;
var payment = false;
var atHerbTown = false;

function start() {
    if (cm.getPlayer().getMap().getId() == 251000100) {
        atHerbTown = true;
    }

    if (cm.haveItem(4031242)) {
        if (atHerbTown) {
            menu = "#L0##bI will use #t4031242##k to move to #b#m230030200##k.#l\r\n#L1#Go to #b#m230000000##k after paying #b10000mesos#k.#l";
        } else {
            menu = "#L0##bI will use #t4031242##k to move to #b#m230030200##k.#l\r\n#L1#Go to #b#m251000000##k after paying #b10000mesos#k.#l";
        }
    } else {
        if (atHerbTown) {
            menu = "#L0#Go to #b#m230030200##k after paying #b1000mesos#k.#l\r\n#L1#Go to #b#m230000000##k after paying #b10000mesos#k.#l";
        } else {
            menu = "#L0#Go to #b#m230030200##k after paying #b1000mesos#k.#l\r\n#L1#Go to #b#m251000000##k after paying #b10000mesos#k.#l";
        }
        payment = true;
    }
    cm.sendSimple("海洋之间都是相互连接的。无法步行到达的地方可以很容易地通过海路到达。今天和我们一起乘坐 #b海豚出租车#k，怎么样？");
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        if (selection == 0) {
            if (payment) {
                if (cm.getPlayer().getMeso() < 1000) {
                    cm.sendOk("我认为你没有足够的钱……");
                    cm.dispose();
                } else {
                    cm.gainMeso(-1000);
                }
            } else {
                cm.gainItem(4031242, -1);
            }
            cm.warp(230030200, 2);
            cm.dispose();
            return;
        } else if (selection == 1) {
            if (cm.getPlayer().getMeso() < 10000) {
                cm.sendOk("我认为你没有足够的钱……");
                cm.dispose();
                return;
            } else {
                cm.gainMeso(-10000);
                cm.warp(atHerbTown ? 230000000 : 251000100);
            }
        }
        cm.dispose();
    }
}