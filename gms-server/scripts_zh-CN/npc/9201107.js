/* @Author SharpAceX
*/

function start() {
    if (cm.getPlayer().getMapId() == 610030500) {
        cm.sendOk("难以置信的力量和能力，任何人都可以实现。但是让一个战士特别的是他们的铁一般意志。无论面对多大的困难，真正的战士都会坚持到胜利确保为止。因此，战士之厅是一条残酷的道路，房间本身和其中的超强怪物都在与你对抗。利用你的技能摆脱影响，打败其中的怪物，到达战士雕像并夺取圣剑。祝你好运！");
        cm.dispose();
    } else if (cm.getPlayer().getMap().getId() == 610030000) {
        cm.sendOk("一支传奇英雄家族，德弗里西恩家族是风暴法师的创始人。这个家族很特别，因为每个儿子或女儿都继承了他们祖先的全部战斗技巧。这种能力被证明非常有用；因为它几乎可以无限制地运用战略、即兴和战术来打败所有的敌人。一个真正的代代相传的家族。");
        cm.dispose();
    } else if (cm.getPlayer().getMapId() == 610030510) {
        if (cm.getPlayer().getMap().countMonsters() == 0) {
            var eim = cm.getEventInstance();
            var stgStatus = eim.getIntProperty("glpq5_room");
            var jobNiche = cm.getPlayer().getJob().getJobNiche();

            if ((stgStatus >> jobNiche) % 2 == 0) {
                if (cm.canHold(4001259, 1)) {
                    cm.gainItem(4001259, 1);
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
"Eliminate all Crimson Guardians."
        }
        cm.dispose();
    }
}