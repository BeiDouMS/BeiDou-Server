/**
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Drago (MapleStorySA)
 2.0 - Second Version by Jayd - translated CPQ contents to English
 ---------------------------------------------------------------------------------------------------
 **/

var status = 0;
var rnk = -1;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 0 && mode == 0) {
            cm.sendOk("好的，希望下次我们能聊天。");
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (cm.getPlayer().getMapId() == 980030010) {
            if (status == 0) {
                cm.sendNext("希望你在怪物嘉年华上玩得开心！");
            } else if (status > 0) {
                cm.warp(980030000, 0);
                cm.dispose();
            }
        } else if (cm.getChar().getMap().isCPQLoserMap()) {
            if (status == 0) {
                if (cm.getChar().getParty() != null) {
                    var shiu = "";
                    if (cm.getPlayer().getFestivalPoints() >= 300) {
                        shiu += "#rA#k";
                        cm.sendOk("很遗憾，尽管你表现出色，但你要么平局要么失败了这场战斗。下次胜利就属于你了！\r\n\r\n#b你的结果：" + shiu);
                        rnk = 10;
                    } else if (cm.getPlayer().getFestivalPoints() >= 100) {
                        shiu += "#rB#k";
                        rnk = 20;
                        cm.sendOk("很遗憾，即使你表现出色，你要么平局要么失败了这场战斗。只差一点点，胜利就可能属于你了！\r\n\r\n#b你的结果：" + shiu);
                    } else if (cm.getPlayer().getFestivalPoints() >= 50) {
                        shiu += "#rC#k";
                        rnk = 30;
                        cm.sendOk("很遗憾，你要么平局要么失败了。胜利属于那些努力奋斗的人。我看到了你的努力，所以胜利离你并不遥远。继续努力吧！\r\n#b你的结果：" + shiu);
                    } else {
                        shiu += "#rD#k";
                        rnk = 40;
                        cm.sendOk("很遗憾，你要么打成了平局，要么输掉了战斗，你的表现清楚地反映了这一点。我希望你下次能做得更好。\r\n\r\n#b你的结果：" + shiu);
                    }
                } else {
                    cm.warp(980030000, 0);
                    cm.dispose();
                }
            } else if (status == 1) {
                switch (rnk) {
                    case 10:
                        cm.warp(980030000, 0);
                        cm.gainExp(35000);
                        cm.dispose();
                        break;
                    case 20:
                        cm.warp(980030000, 0);
                        cm.gainExp(25000);
                        cm.dispose();
                        break;
                    case 30:
                        cm.warp(980030000, 0);
                        cm.gainExp(12500);
                        cm.dispose();
                        break;
                    case 40:
                        cm.warp(980030000, 0);
                        cm.gainExp(3500);
                        cm.dispose();
                        break;
                    default:
                        cm.warp(980030000, 0);
                        cm.dispose();
                        break;
                }
            }
        } else if (cm.getChar().getMap().isCPQWinnerMap()) {
            if (status == 0) {
                if (cm.getChar().getParty() != null) {
                    var shi = "";
                    if (cm.getPlayer().getFestivalPoints() >= 300) {
                        shi += "#rA#k";
                        rnk = 1;
                        cm.sendOk("恭喜你的胜利！表现太棒了！对方队伍毫无还手之力！希望下次也能有同样出色的表现！\r\n\r\n#b你的成绩：" + shi);
                    } else if (cm.getPlayer().getFestivalPoints() >= 100) {
                        shi += "#rB#k";
                        rnk = 2;
                        cm.sendOk("恭喜你的胜利！太棒了！你对抗对方团队做得很好！再坚持一会儿，下次你肯定能拿到A！\r\n\r\n#b你的成绩：" + shi);
                    } else if (cm.getPlayer().getFestivalPoints() >= 50) {
                        shi += "#rC#k";
                        rnk = 3;
                        cm.sendOk("恭喜你的胜利。你做了一些事情，但这不能算是一个好的胜利。我期待你下次能做得更好。\r\n#b你的结果：" + shi);
                    } else {
                        shi += "#rD#k";
                        rnk = 4;
                        cm.sendOk("恭喜你的胜利，尽管你的表现并没有完全体现出来。在下一次怪物嘉年华中更加积极参与吧！\r\n\r\n#b你的结果：" + shi);
                    }
                } else {
                    cm.warp(980030000, 0);
                    cm.dispose();
                }
            } else if (status == 1) {
                switch (rnk) {
                    case 1:
                        cm.warp(980030000, 0);
                        cm.gainExp(875000);
                        cm.dispose();
                        break;
                    case 2:
                        cm.warp(980030000, 0);
                        cm.gainExp(700000);
                        cm.dispose();
                        break;
                    case 3:
                        cm.warp(980030000, 0);
                        cm.gainExp(555000);
                        cm.dispose();
                        break;
                    case 4:
                        cm.warp(980030000, 0);
                        cm.gainExp(100000);
                        cm.dispose();
                        break;
                    default:
                        cm.warp(980030000, 0);
                        cm.dispose();
                        break;
                }
            }
        }
    }
}  