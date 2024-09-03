/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/*
 *
 *@author Ronan
 */

var status = 0;
var expedition;
var expedMembers;
var player;
var em;
var exped = MapleExpeditionType.BALROG_NORMAL;
var expedName = "Balrog";
var expedBoss = "蝙蝠怪";
var expedMap = "蝙蝠怪的墓地";

var list = "有什么事要做？##b\r\n\r\n#L1#确认目前的远征队成员。#l\r\n#L2#挑战蝙蝠怪 - 入场#l\r\n#L3#取消远征队登记。#l";

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {

    player = cm.getPlayer();
    expedition = cm.getExpedition(exped);
    em = cm.getEventManager("BalrogBattle");

    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }

        if (status == 0) {
            if (player.getLevel() < exped.getMinLevel() || player.getLevel() > exped.getMaxLevel()) { //Don't fit requirement, thanks Conrad
                cm.sendOk("你尚不满足挑战" + expedBoss + "的资格。");
                cm.dispose();
            } else if (expedition == null) { //Start an expedition
                cm.sendSimple("#e#b<远征队：" + expedName + ">\r\n#k#n" + em.getProperty("party") + "\r\n\r\n你要组成一支远征队讨伐#r" + expedBoss + "#k吗?\r\n#b#L1#入场。#l\r\n\#L2#还不是时候。#l\r\n\#L3#我想多了解一点关于这次远征的事情。#l");
                status = 1;
            } else if (expedition.isLeader(player)) { //If you're the leader, manage the exped
                if (expedition.isInProgress()) {
                    cm.sendOk("已开始登记将要参与的远征队成员。");
                    cm.dispose();
                } else {
                    cm.sendSimple(list);
                    status = 2;
                }
            } else if (expedition.isRegistering()) { //If the expedition is registering
                if (expedition.contains(player)) { //If you're in it but it hasn't started, be patient
                    cm.sendOk("你已经登记为远征队成员。请等待 #r" + expedition.getLeader().getName() + "#k 开启远征。");
                    cm.dispose();
                } else { //If you aren't in it, you're going to get added
                    cm.sendOk(expedition.addMember(cm.getPlayer()));
                    cm.dispose();
                }
            } else if (expedition.isInProgress()) { //Only if the expedition is in progress
                if (expedition.contains(player)) { //If you're registered, warp you in
                    var eim = em.getInstance(expedName + player.getClient().getChannel());
                    if(eim.getIntProperty("canJoin") == 1) {
                        eim.registerPlayer(player);
                    } else {
                        cm.sendOk("你的远征队正在挑战" + expedBoss + "。愿他们勇敢的灵魂得偿所愿。");
                    }

                    cm.dispose();
                } else { //If you're not in by now, tough luck
                    cm.sendOk("其它远征队正在挑战 " + expedBoss + "。愿他们勇敢的灵魂得偿所愿。");
                    cm.dispose();
                }
            }
        } else if (status == 1) {
            if (selection == 1) {
                expedition = cm.getExpedition(exped);
                if(expedition != null) {
                    cm.sendOk("已有玩家登记成为远征队长，请作为远征队员加入。");
                    cm.dispose();
                    return;
                }

                var res = cm.createExpedition(exped);
                if (res == 0) {
                    cm.sendOk("#r" + expedBoss + " 远征队#k 已创建。\r\n\r\n目前可以与我对话确认队员名单，或开启挑战。");
                } else if (res > 0) {
                    cm.sendOk("抱歉，你的挑战次数已达上限。");
                } else {
                    cm.sendOk("发生了未知错误，请稍后重试。");
                }

                cm.dispose();
                return;
            } else if (selection == 2) {
                cm.sendOk("确实，并非每个人都想要挑战 " + expedBoss + "。");
                cm.dispose();
                return;
            } else {
                cm.sendSimple("你好，我是 #b#n无影#n#k, 正在守护着这里。这座寺庙目前被蝙蝠魔的军队包围了。我们目前仍不知道是谁下达了围困这里的命令。" +
                    "最近 #e#b阿尔泰骑士团#n#k 一直在派遣成员，但所有救兵都一去不返。" +
                    "那么，冒险家。你想要尝试战胜这不可名状的恐惧吗？\r\n  #L1#阿尔泰骑士团？");

                status = 10;
            }
        } else if (status == 2) {
            if (selection == 1) {
                if (expedition == null) {
                    cm.sendOk("远征队人数已达上限。");
                    cm.dispose();
                    return;
                }
                expedMembers = expedition.getMemberList();
                var size = expedMembers.size();
                if (size == 1) {
                    cm.sendOk("目前尚无其他人加入远征队。");
                    cm.dispose();
                    return;
                }
                var text = "以下成员加入了远征队，可以点击名字将其移除：\r\n";
                text += "\r\n\t\t1." + expedition.getLeader().getName();
                for (var i = 1; i < size; i++) {
                    text += "\r\n#b#L" + (i + 1) + "#" + (i + 1) + ". " + expedMembers.get(i).getValue() + "#l\n";
                }
                cm.sendSimple(text);
                status = 6;
            } else if (selection == 2) {
                var min = exped.getMinSize();
                var size = expedition.getMemberList().size();
                if (size < min) {
                    cm.sendOk("需要至少 " + min + " 名角色加入远征队方可开启。");
                    cm.dispose();
                    return;
                }

                cm.sendOk("挑战即将开始，远征队将被传送进入#b" + expedMap + "#k。");
                status = 4;
            } else if (selection == 3) {
                player.getMap().broadcastMessage(MaplePacketCreator.serverNotice(6, expedition.getLeader().getName() + " 结束了远征。"));
                cm.endExpedition(expedition);
                cm.sendOk("远征结束，有时脱逃也是保存实力的上策。");
                cm.dispose();
                return;
            }
        } else if (status == 4) {
            if (em == null) {
                cm.sendOk("活动无法初始化，请截图向GM报告。");
                cm.dispose();
                return;
            }

            em.setProperty("leader", player.getName());
            em.setProperty("channel", player.getClient().getChannel());
            if(!em.startInstance(expedition)) {
                cm.sendOk("另一支远征队正在挑战 " + expedBoss + "。");
                cm.dispose();
                return;
            }

            cm.dispose();
            return;
        } else if (status == 6) {
            if (selection > 0) {
                var banned = expedMembers.get(selection - 1);
                expedition.ban(banned);
                cm.sendOk("你将 " + banned.getValue() + " 驱逐出了远征队。");
                cm.dispose();
            } else {
                cm.sendSimple(list);
                status = 2;
            }
        } else if (status == 10) {
            cm.sendOk("阿尔泰骑士团是一群精英战士，他们在监督着世界的运转。这个教团成立于40年前，防备着那时被封印的黑魔法师有朝一日卷土重来。");
            cm.dispose();
        }
    }
}