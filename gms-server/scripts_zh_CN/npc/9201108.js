/* @Author SharpAceX
*/

function start() {
    if (cm.getPlayer().getMapId() == 610030500) {
        cm.sendOk("一位被称为大师守护者的传奇生物在等待着你。它曾是一只深红守护者，里德利曾对其进行实验，导致它对魔法攻击、长矛、狼牙棒等几乎所有攻击都具有高度抵抗力，唯独不能抵挡威力异常强大的箭矢。弓箭手们！作为弓箭的无可争议的大师，你们必须使用最强大的攻击——从扫射到飓风到穿刺箭——来摧毁这个强大的生物，达到弓箭手雕像，夺取祖传弓！祝你好运！");
        cm.dispose();
    } else if (cm.getPlayer().getMap().getId() == 610030000) {
        cm.sendOk("Lockewood是为数不多的已知的神圣弓箭手之一，他是要塞中最著名的英雄之一。特别值得一提的是他定制的白色和金色战斗箭矢，据说是由一位强大的女神祝福过。他的瞄准能力在长距离上非常准确。因为他的“创世箭”和“末日凤凰”，他备受敬畏和尊重，曾经一次从英雄谷击倒了六只泰坦。");
        cm.dispose();
    } else if (cm.getPlayer().getMapId() == 610030540) {
        if (cm.getPlayer().getMap().countMonsters() == 0) {
            var eim = cm.getEventInstance();
            var stgStatus = eim.getIntProperty("glpq5_room");
            var jobNiche = cm.getPlayer().getJob().getJobNiche();

            if ((stgStatus >> jobNiche) % 2 == 0) {
                if (cm.canHold(4001258, 1)) {
                    cm.gainItem(4001258, 1);
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
"Eliminate all Master Guardians."
        }
        cm.dispose();
    }
}