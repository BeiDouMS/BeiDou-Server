/* @Author SharpAceX
*/

function start() {
    if (cm.getPlayer().getMapId() == 610030500) {
        cm.sendOk("作为一名强大的精英法师，里德利知道智慧的价值，这是巫师的标志品质。因此，法师之间是一个扭曲的阴谋迷宫——传送技能是你唯一能在里面使用的技能，魔法爪是唯一能打破雕像的技能。你还必须在其中杀死许多怪物。解决迷宫并打败其中所有的敌人后，推断出哪个法师雕像隐藏了第一魔法之杖，然后打破它来获取！祝你好运！");
        cm.dispose();
    } else if (cm.getPlayer().getMap().getId() == 610030000) {
        cm.sendOk("一个永远被铭记的名字，拉斐尔是一个非常有技巧的巫师，也是心灵魔法、念力和心灵感应的首席大师。除此之外，他还是掌握所有元素的“精英法师”之一。他最后被看到是在寻找“元素神殿”，以扭转克拉克军队的入侵潮流……");
        cm.dispose();
    } else if (cm.getPlayer().getMapId() == 610030521) {
        if (cm.getPlayer().getMap().countMonsters() == 0) {
            var eim = cm.getEventInstance();
            var stgStatus = eim.getIntProperty("glpq5_room");
            var jobNiche = cm.getPlayer().getJob().getJobNiche();

            if ((stgStatus >> jobNiche) % 2 == 0) {
                if (cm.canHold(4001257, 1)) {
                    cm.gainItem(4001257, 1);
                    cm.sendOk("干得好。");

                    stgStatus += (1 << jobNiche);
                    eim.setIntProperty("glpq5_room", stgStatus);
                } else {
                    cm.sendOk("先在你的杂项物品栏腾出空间。");
                }
            } else {
                cm.sendOk("这个房间里的武器已经被取走了。");
            }
        } else {
            cm.sendOk("消灭所有怪物。");
        }
        cm.dispose();
    } /* else if (cm.getPlayer().getMapId() == 610030522) {
                if (cm.getPlayer().getMap().countMonsters() == 0) {
                        cm.warp(610030522,0);
                } else {
                        cm.sendOk("消灭所有怪物。");
                }
                cm.dispose();
        }
        */
}