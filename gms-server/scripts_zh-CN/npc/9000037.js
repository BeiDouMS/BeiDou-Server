/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * @author: Ronan
 * @npc: Agent Meow
 * @map: 970030000 - Hidden Street - Exclusive Training Center
 * @func: Boss Rush PQ
 */

var status = 0;
var state;
var em = null;

function onRestingSpot() {
    return cm.getMapId() >= 970030001 && cm.getMapId() <= 970030010;
}

function isFinalBossDone() {
    return cm.getMapId() >= 970032700 && cm.getMapId() < 970032800 && cm.getMap().getMonsters().isEmpty();
}

function detectTeamLobby(team) {
    var midLevel = 0;

    for (var i = 0; i < team.size(); i++) {
        var player = team.get(i);
        midLevel += player.getLevel();
    }
    midLevel = Math.floor(midLevel / team.size());

    var lobby;  // teams low level can be allocated at higher leveled lobbys
    if (midLevel <= 20) {
        lobby = 0;
    } else if (midLevel <= 40) {
        lobby = 1;
    } else if (midLevel <= 60) {
        lobby = 2;
    } else if (midLevel <= 80) {
        lobby = 3;
    } else if (midLevel <= 90) {
        lobby = 4;
    } else if (midLevel <= 100) {
        lobby = 5;
    } else if (midLevel <= 110) {
        lobby = 6;
    } else {
        lobby = 7;
    }

    return lobby;
}

function start() {
    status = -1;
    state = (cm.getMapId() >= 970030001 && cm.getMapId() <= 970042711) ? (!onRestingSpot() ? (isFinalBossDone() ? 3 : 1) : 2) : 0;
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
            if (state == 3) {
                if (cm.getEventInstance().getProperty("clear") == null) {
                    cm.getEventInstance().clearPQ();
                    cm.getEventInstance().setProperty("clear", "true");
                }

                if (cm.isEventLeader()) {
                    cm.sendOk("你的队伍完成了如此惊人的壮举，走到了这一步，#b你已经打败了所有的boss#k，恭喜！现在我将给你们奖励，因为你们即将被传送出去…");
                } else {
                    cm.sendOk("在这个副本中#b打败所有的boss#k，恭喜你！现在你将获得与你在这里表现相匹配的奖品，我会将你传送出去。");
                }
            } else if (state == 2) {
                if (cm.isEventLeader()) {
                    if (cm.getPlayer().getEventInstance().isEventTeamTogether()) {
                        cm.sendYesNo("你的队伍准备好继续前进到下一阶段了吗？如果你认为已经完成了，就走过传送门，现在是时候了。现在，你们真的想要继续吗？");
                    } else {
                        cm.sendOk("请等待您的队伍重新集合后再继续。");
                        cm.dispose();

                    }
                } else {
                    cm.sendOk("等待你的队长发出信号让我继续。如果你感觉不太好，想要退出，走过传送门，你将被传送出去，并且你会因为走到这一步而获得奖品。");
                    cm.dispose();

                }
            } else if (state == 1) {
                cm.sendYesNo("你希望放弃这个副本吗？");
            } else {
                em = cm.getEventManager("BossRushPQ");
                if (em == null) {
                    cm.sendOk("Boss Rush PQ遇到了一个错误。");
                    cm.dispose();
                    return;
                } else if (cm.isUsingOldPqNpcStyle()) {
                    action(1, 0, 0);
                    return;
                }

                cm.sendSimple("#e#b<组队任务：首领突袭>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n你想要和队友合作完成远征任务，还是勇敢到足以独自完成？让你的#b队伍领袖#k与我交谈或者自己创建一个队伍。#b\r\n#L0#我想参加组队任务。\r\n#L1#我想" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "禁用" : "启用") + "组队搜索。\r\n#L2#我想了解更多详情。");
            }
        } else if (status == 1) {
            if (state == 3) {
                if (!cm.getPlayer().getEventInstance().giveEventReward(cm.getPlayer(), 6)) {
                    cm.sendOk("请提前在你的背包所有标签中安排一个空位。");
                    cm.dispose();
                    return;
                }

                cm.warp(970030000);
                cm.dispose();
            } else if (state == 2) {
                var restSpot = ((cm.getMapId() - 1) % 5) + 1;
                cm.getPlayer().getEventInstance().restartEventTimer(restSpot * 4 * 60000);  // adds (restspot number * 4) minutes
                cm.getPlayer().getEventInstance().warpEventTeam(970030100 + cm.getEventInstance().getIntProperty("lobby") + (500 * restSpot));

                cm.dispose();
            } else if (state == 1) {
                cm.warp(970030000);
                cm.dispose();
            } else {
                if (selection == 0) {
                    if (cm.getParty() == null) {
                        cm.sendOk("只有当你加入一个队伍时，你才能参加派对任务。");
                        cm.dispose();
                    } else if (!cm.isLeader()) {
                        cm.sendOk("你的队长必须与我交谈才能开始这个组队任务。");
                        cm.dispose();
                    } else {
                        var eli = em.getEligibleParty(cm.getParty());
                        if (eli.size() > 0) {
                            var lobby = detectTeamLobby(eli), i;
                            for (i = lobby; i < 8; i++) {
                                if (em.startInstance(i, cm.getParty(), cm.getPlayer().getMap(), 1)) {
                                    break;
                                }
                            }

                            if (i == 8) {
                                cm.sendOk("另一个队伍已经进入了该频道的#r组队任务#k。请尝试其他频道，或者等待当前队伍完成。");
                            }
                        } else {
                            cm.sendOk("你目前无法开始这个组队任务，因为你的队伍可能不符合人数要求，有些队员可能不符合尝试条件，或者他们不在这张地图上。如果你找不到队员，可以尝试使用组队搜索功能。");
                        }

                        cm.dispose();
                    }
                } else if (selection == 1) {
                    var psState = cm.getPlayer().toggleRecvPartySearchInvite();
                    cm.sendOk("你的组队搜索状态现在是：#b" + (psState ? "enabled" : "disabled") + "#k。想要改变状态时随时找我。");
                    cm.dispose();
                } else {
                    cm.sendOk("#e#b<组队任务：Boss Rush>#k#n\r\n来自世界各地的勇敢冒险者来到这里，测试他们在战斗中的技能和能力，面对来自冒险岛的更强大的boss。与其他冒险者联手或者独自承担所有的压力并获得所有的荣耀，这取决于你。奖励根据冒险者们的进展程度给予，而额外奖品可能会随机给予队伍中的一名成员，所有这些都在远征结束时进行分配。\r\n\r\n这个任务还支持#b多个大厅，以匹配不同团队等级的玩家#k：如果你想更快地为你的团队设置boss rush，可以与等级较低的玩家组队。");
                    cm.dispose();
                }
            }
        }
    }
}