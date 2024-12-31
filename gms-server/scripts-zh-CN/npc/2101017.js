/*2101017.js
 *Cesar
 *@author Jvlaple
 */

var status = 0;
var toBan = -1;
var choice;
var arena;
var arenaName;
var type;
var map;
const ExpeditionType = Java.type('org.gms.server.expeditions.ExpeditionType');
var exped;
var expedicao;
var expedMembers;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {

    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }

        const GameConstants = Java.type('org.gms.constants.game.GameConstants');
        if (cm.getPlayer().getMapId() == 980010100 || cm.getPlayer().getMapId() == 980010200 || cm.getPlayer().getMapId() == 980010300) {
            if (cm.getPlayer().getMapId() == 980010100) {
                exped = ExpeditionType.ARIANT;
                expedicao = cm.getExpedition(exped);

            } else if (cm.getPlayer().getMapId() == 980010200) {
                exped = ExpeditionType.ARIANT1;
                expedicao = cm.getExpedition(exped);
            } else {
                exped = ExpeditionType.ARIANT2;
                expedicao = cm.getExpedition(exped);
            }

            if (expedicao == null) {
                cm.dispose();
                return;
            }

            expedMembers = expedicao.getMemberList();
            if (status == 0) {
                if (cm.isLeaderExpedition(exped)) {
                    cm.sendSimple("你想做什么？#b\r\n#L1#查看当前成员#l\r\n#L2#禁止成员#l\r\n#L3#开始战斗#l\r\n#L4#离开竞技场#l");
                    status = 1;
                } else {
                    var toSend = "Current members inside this arena:\r\n#b";
                    toSend += cm.getExpeditionMemberNames(exped);
                    cm.sendOk(toSend);
                    cm.dispose();
                }
            } else if (status == 1) {
                if (selection == 1) {
                    var toSend = "Current members inside this arena:\r\n#b";
                    toSend += cm.getExpeditionMemberNames(exped);
                    cm.sendOk(toSend);
                    cm.dispose();
                } else if (selection == 2) {
                    var size = expedMembers.size();
                    if (size == 1) {
                        cm.sendOk("你是探险队中唯一的成员。");
                        cm.dispose();
                        return;
                    }
                    var text = "以下成员组成了你的探险队（点击成员名字可以将其踢出探险队）：\r\n";
                    text += "\r\n\t\t1." + expedicao.getLeader().getName();
                    for (var i = 1; i < size; i++) {
                        text += "\r\n#b#L" + (i + 1) + "#" + (i + 1) + ". " + expedMembers.get(i).getValue() + "#l\n";
                    }
                    cm.sendSimple(text);
                    status = 6;
                } else if (selection == 3) {
                    if (expedicao.getMembers().size() < 1) {
                        cm.sendOk("需要一个玩家来开始战斗。");
                        cm.dispose();
                    } else {
                        if (cm.getParty() != null) {
                            cm.sendOk("你不能以团队形式进入战斗。");
                            cm.dispose();
                            return;
                        }

                        var errorMsg = cm.startAriantBattle(exped, cm.getPlayer().getMapId());
                        if (errorMsg != "") {
                            cm.sendOk(errorMsg);
                        }

                        cm.dispose();
                    }
                } else if (selection == 4) {
                    cm.mapMessage(5, "The Leader of the arena left.");
                    expedicao.warpExpeditionTeam(980010000);
                    cm.endExpedition(expedicao);
                    cm.dispose();
                }
            } else if (status == 6) {
                if (selection > 0) {
                    var banned = expedMembers.get(selection - 1);
                    expedicao.ban(banned);
                    cm.sendOk("你已经从远征中禁止了 " + banned.getValue() + "。");
                    cm.dispose();
                } else {
                    cm.sendSimple(list);
                    status = 2;
                }
            }
        } else if (GameConstants.isAriantColiseumArena(cm.getPlayer().getMapId())) {
            if (cm.getPlayer().getMapId() == 980010101) {
                exped = ExpeditionType.ARIANT;
                expedicao = cm.getExpedition(exped);
            } else if (cm.getPlayer().getMapId() == 980010201) {
                exped = ExpeditionType.ARIANT1;
                expedicao = cm.getExpedition(exped);
            } else {
                exped = ExpeditionType.ARIANT2;
                expedicao = cm.getExpedition(exped);
            }
            if (status == 0) {
                var gotTheBombs = expedicao.getProperty("gotBomb" + cm.getChar().getId());
                if (gotTheBombs != null) {
                    cm.sendOk("我已经给了你炸弹，请立刻击败 #b天蝎座#k！");
                    cm.dispose();
                } else if (cm.canHoldAll([2270002, 2100067], [50, 5])) {
                    cm.sendOk("我已经给了你(5) #b#e炸弹#k#n和(50) #b#e元素岩石#k#n。\r\n使用元素岩石来捕捉蝎子，以获取#r#e灵魂宝石#k#n！");
                    expedicao.setProperty("gotBomb" + cm.getChar().getId(), "1");
                    cm.gainItem(2270002, 50);
                    cm.gainItem(2100067, 5);
                    cm.dispose();
                } else {
                    cm.sendOk("你的背包好像已经满了。");
                    cm.dispose();
                }
            }
        } else {
            cm.sendOk("嗨，你听说过阿里安特角斗场战斗竞技场吗？这是一个供20级到30级玩家参与的竞争活动！");
            cm.dispose();
        }
    }
}