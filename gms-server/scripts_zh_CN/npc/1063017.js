/* Monstrous Looking Statue
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
            cm.sendYesNo("前方等待着大师本人。你准备好面对他了吗？");
        } else {
            if (cm.getClient().getChannelServer().getMapFactory().getMap(925020010).getCharacters().size() > 0) {
                cm.sendOk("有人已经在挑战大师了。请稍后再试。");
            } else {
                const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
                const Point = Java.type('java.awt.Point');
                cm.getWarpMap(910510202).spawnMonsterOnGroundBelow(LifeFactory.getMonster(9300346), new Point(95, 200));
                cm.warp(910510202, 0);
            }

            cm.dispose();
        }
    }
}