var status = 0;
var menu;
var payment = false;
var atHerbTown = false;
var locationCloseToOrbis =[13,18];

function start() {
    if (cm.getPlayer().getMap().getId() == 251000100) {
        atHerbTown = true;
    }

    if (cm.haveItem(4031242)) {
        if (atHerbTown) {
            menu = "#L0##b我想用 #t4031242##k 移动到 #b#m230030200##k.#l\r\n#L1#去 #b#m230000000##k 需支付 #b10000金币#k.#l";
        } else {
            menu = "#L0##b我想用 #t4031242##k 移动到 #b#m230030200##k.#l\r\n#L1#去 #b#m251000000##k 需支付 #b10000金币#k.#l\r\n#L2#前往 #b#m230010000##k 需支付#b1000金币#k.#l";
        }
    } else {
        if (atHerbTown) {
            menu = "#L0#前往 #b#m230030200##k 需支付#b1000金币#k.#l\r\n#L1#前往 #b#m230000000##k 需支付#b10000金币#k.#l";
        } else {
            menu = "#L0#前往 #b#m230030200##k 需支付#b1000金币#k.#l\r\n#L1#前往 #b#m251000000##k 需支付#b10000金币#k.#l\r\n#L2#前往 #b#m230010000##k 需支付#b1000金币#k.#l";
        }
        payment = true;
    }
    cm.sendSimple("海洋之间都是相互连接的。无法步行到达的地方可以很容易地通过海路到达。今天和我们一起乘坐 #b海豚出租车#k，怎么样？\r\n"+menu);
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
        else if (selection == 2) {
            if (cm.getPlayer().getMeso() < 1000) {
                cm.sendOk("我认为你没有足够的钱……");
                cm.dispose();
                return;
            } else {
                cm.gainMeso(-1000);
                //传送到通天塔入口最近的2个点随机一个其实就是在map.wz的div的13和18里面选
                cm.warp(230010000,locationCloseToOrbis[Math.floor(Math.random() * locationCloseToOrbis.length)]);
            }
        }
        cm.dispose();
    }
}
