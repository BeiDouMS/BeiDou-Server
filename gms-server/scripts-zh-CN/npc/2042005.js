/**
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Drago (MapleStorySA)
 2.0 - Second Version by Jayd - translated CPQ contents to English
 ---------------------------------------------------------------------------------------------------
 **/

var cpqMinLvl = 51;
var cpqMaxLvl = 70;
var cpqMinAmt = 2;
var cpqMaxAmt = 6;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            if (cm.getParty() == null) {
                status = 10;
                cm.sendOk("你需要先创建一个队伍，才能参加怪物嘉年华！");
            } else if (!cm.isLeader()) {
                status = 10;
                cm.sendOk("如果想开始战斗，请让#b队长#k来和我对话。");
            } else {
                var leaderMapid = cm.getMapId();
                var party = cm.getParty().getMembers();
                var inMap = cm.partyMembersInMap();
                var lvlOk = 0;
                var isOutMap = 0;
                for (var i = 0; i < party.size(); i++) {
                    if (party.get(i).getLevel() >= cpqMinLvl && party.get(i).getLevel() <= cpqMaxLvl) {
                        lvlOk++;

                        if (party.get(i).getPlayer().getMapId() != leaderMapid) {
                            isOutMap++;
                        }
                    }
                }

                if (party >= 1) {
                    status = 10;
                    cm.sendOk("你的队伍人数不足。队伍需要有#b" + cpqMinAmt + "#k - #r" + cpqMaxAmt + "#k名成员，而且所有成员都必须和你在同一张地图。");
                } else if (lvlOk != inMap) {
                    status = 10;
                    cm.sendOk("请确认队伍中的所有成员等级都在正确范围内（" + cpqMinLvl + "~" + cpqMaxLvl + "）！");
                } else if (isOutMap > 0) {
                    status = 10;
                    cm.sendOk("队伍中有成员不在当前地图！");
                } else {
                    if (!cm.sendCPQMapLists2()) {
                        cm.sendOk("所有怪物嘉年华场地目前都在使用中！请稍后再试。");
                        cm.dispose();
                    }
                }
            }
        } else if (status == 1) {
            if (cm.fieldTaken2(selection)) {
                if (cm.fieldLobbied2(selection)) {
                    cm.challengeParty2(selection);
                    cm.dispose();
                } else {
                    cm.sendOk("这个房间目前已经满员。");
                    cm.dispose();
                }
            } else {
                var party = cm.getParty().getMembers();
                const GameConfig = Java.type('org.gms.config.GameConfig');
                if ((selection === 0 || selection === 1) && party.size() < (GameConfig.getServerBoolean("use_enable_solo_expeditions") ? 1 : 2)) {
                    cm.sendOk("至少需要2名玩家才能参加战斗！");
                } else if ((selection === 2) && party.size() < (GameConfig.getServerBoolean("use_enable_solo_expeditions") ? 1 : 3)) {
                    cm.sendOk("至少需要3名玩家才能参加战斗！");
                } else {
                    cm.cpqLobby2(selection);
                }
                cm.dispose();
            }
        } else if (status == 11) {
            cm.dispose();
        }
    }
}