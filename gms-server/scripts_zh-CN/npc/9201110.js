/* @Author SharpAceX
*/

function start() {
    switch (cm.getPlayer().getMapId()) {
        case 610030500:
            cm.sendOk("作为每个盗贼都知道的，最好的攻击是你永远不会看到的那种。因此，为了最好地说明这一点，你将置身于一个只能通过加速术到达的平台和壁架的房间，还有你的匕首或爪子必须永久关闭的全视之眼。在所有全视之眼被消灭后，前往盗贼雕像并夺取原始之爪！祝你好运！");
            break;
        case 610030000:
            cm.sendOk("曾被誉为“暗影王子”的大师李龙拥有无与伦比的速度和力量，使用短距离匕首和长链爪。作为Bosshunters的兼职成员，他以无与伦比的能力融入夜晚本身而闻名。在与Crimson Balrog的战斗中，他的传奇故事逐渐传开，他移动如此迅速，以至于Balrog的攻击只能击中空气。李龙还为那些不如他自己幸运的人偶尔进行“回收”行动。");
            break;
        case 610030530:
            if (cm.isAllReactorState(6108004, 1)) {
                var eim = cm.getEventInstance();
                var stgStatus = eim.getIntProperty("glpq5_room");
                var jobNiche = cm.getPlayer().getJob().getJobNiche();

                if ((stgStatus >> jobNiche) % 2 == 0) {
                    if (cm.canHold(4001256, 1)) {
                        cm.gainItem(4001256, 1);
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
                cm.sendOk("现在去吧，用你的移动技能摧毁所有警惕的眼睛，同伴盗贼。完成后向我报告。");
            }
            break;
    }
    cm.dispose();
}