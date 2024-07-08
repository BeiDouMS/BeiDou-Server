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
 * @npc: Romeo
 * @map: Magatia - Zenumist - Hidden Room (261000011)
 * @func: Magatia PQ (Zenumist)
 */

var status = 0;
var em = null;

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

        if (cm.getMapId() != 261000011) {
            if (status == 0) {
                cm.sendYesNo("我们必须继续战斗，拯救朱丽叶，请保持你的速度。如果你感觉不太好，无法继续，你的同伴和我都会理解……那么，你打算撤退吗？");
            } else if (status == 1) {
                cm.warp(926100700, 0);
                cm.dispose();
            }
        } else {
            if (status == 0) {
                em = cm.getEventManager("MagatiaPQ_Z");
                if (em == null) {
                    cm.sendOk("玛加提亚组队任务（泽尼姆斯特）遇到了一个错误。");
                    cm.dispose();
                    return;
                } else if (cm.isUsingOldPqNpcStyle()) {
                    action(1, 0, 0);
                    return;
                }

                cm.sendSimple("#e#b<组队任务：罗密欧与朱丽叶>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n我心爱的朱丽叶被绑架了！虽然她是阿尔卡德诺家的人，但我不能坐视她因这场愚蠢的冲突而受苦。我需要你和你的同事们帮助我救她！拜托，帮帮我们！！请让你的#b队伍领袖#k和我谈谈。#b\r\n#L0#我想参加这个组队任务。\r\n#L1#我想" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "关闭" : "开启") + "组队搜索。\r\n#L2#我想了解更多细节。");
            } else if (status == 1) {
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
                            if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
                                cm.sendOk("另一个队伍已经进入了该频道的#r组队任务#k。请尝试其他频道，或者等待当前队伍完成。");
                            }
                        } else {
                            cm.sendOk("你目前无法开始这个组队任务，因为你的队伍可能不符合人数要求，有些队员可能不符合参与条件，或者他们不在这张地图上。如果你找不到队员，可以尝试使用组队搜索功能。");
                        }

                        cm.dispose();
                    }
                } else if (selection == 1) {
                    var psState = cm.getPlayer().toggleRecvPartySearchInvite();
                    cm.sendOk("你的组队搜索状态现在是：#b" + (psState ? "enabled" : "disabled") + "#k。想要改变状态时随时找我。");
                    cm.dispose();
                } else {
                    cm.sendOk("不久前，一位名叫尤利特的科学家因为他对阿尔卡德诺和泽尼玛斯的合成炼金术的研究而被这个城镇放逐。由于这种组合所带来的巨大力量，根据法律是禁止研究的。然而，他无视了这项法律，并且参与了这两项研究。结果，他被流放了。\r\n他现在在报复，已经带走了我心爱的人，下一个目标是我，因为我们是玛加提亚的重要人物，是这两个社会的继承者。但我不害怕。我们必须不惜一切代价把她夺回来！");
                    cm.dispose();
                }
            }
        }
    }
}