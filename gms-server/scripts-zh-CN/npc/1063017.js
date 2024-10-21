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

        switch (status) {
            case 0:
                cm.sendYesNo("欢迎回来，弗朗西斯主人。要进入主人的洞窟吗？");
                break;
            case 1:
                if (cm.getClient().getChannelServer().getMapFactory().getMap(925020010).getCharacters().size() > 0) {
                    cm.sendOk("有人在里面了。请稍后再进入。");
                    cm.dispose();
                    break;
                }
                cm.sendOk("请慢走，弗朗西斯主人。");
                break;
            case 2:
                if (cm.getClient().getChannelServer().getMapFactory().getMap(925020010).getCharacters().size() > 0) {
                    cm.sendOk("有人在里面了。请稍后再进入。");
                } else {
                    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
                    const Point = Java.type('java.awt.Point');
                    cm.getWarpMap(910510202).spawnMonsterOnGroundBelow(LifeFactory.getMonster(9300346), new Point(95, 200));
                    cm.warp(910510202, 0);
                }
            default:
                cm.dispose();
                break;
        }
    }
}