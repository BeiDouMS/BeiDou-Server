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
            cm.sendOk("好吧，希望下次还能再聊。");
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
                cm.sendNext("希望你在怪物嘉年华中玩得开心！");
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
                        cm.sendOk("很遗憾，虽然你表现非常出色，但这场战斗还是平局或失败了。下次胜利一定会属于你！ \r\n\r\n#b你的结果：" + shiu);
                        rnk = 10;
                    } else if (cm.getPlayer().getFestivalPoints() >= 100) {
                        shiu += "#rB#k";
                        rnk = 20;
                        cm.sendOk("很遗憾，虽然你展现了极限般的表现，但这场战斗还是平局或失败了。只差一点点，胜利就会属于你！ \r\n\r\n#b你的结果：" + shiu);
                    } else if (cm.getPlayer().getFestivalPoints() >= 50) {
                        shiu += "#rC#k";
                        rnk = 30;
                        cm.sendOk("很遗憾，这场战斗最终平局或失败了。胜利属于不断努力的人。我看到了你的努力，所以胜利离你并不遥远。继续加油！\r\n\r\n#b你的结果：" + shiu);
                    } else {
                        shiu += "#rD#k";
                        rnk = 40;
                        cm.sendOk("很遗憾，这场战斗最终平局或失败了，你的表现也确实反映了这一点。下次我期待你有更好的表现。 \r\n\r\n#b你的结果：" + shiu);
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
                        cm.sendOk("恭喜你获得胜利！！！多么精彩的表现！对方队伍完全无计可施！希望下次也能保持这样的好表现！ \r\n\r\n#b你的结果：" + shi);
                    } else if (cm.getPlayer().getFestivalPoints() >= 100) {
                        shi += "#rB#k";
                        rnk = 2;
                        cm.sendOk("恭喜你获得胜利！太棒了！你在对抗对方队伍时表现得很好！只要再努力一点，下次一定能拿到A！ \r\n\r\n#b你的结果：" + shi);
                    } else if (cm.getPlayer().getFestivalPoints() >= 50) {
                        shi += "#rC#k";
                        rnk = 3;
                        cm.sendOk("恭喜你获得胜利。你确实做了一些贡献，不过还称不上漂亮的胜利。下次我期待你有更好的表现。 \r\n\r\n#b你的结果：" + shi);
                    } else {
                        shi += "#rD#k";
                        rnk = 4;
                        cm.sendOk("恭喜你获得胜利，虽然你的表现还不太配得上这场胜利。下次参加怪物嘉年华时，请更积极一些！ \r\n\r\n#b你的结果：" + shi);
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