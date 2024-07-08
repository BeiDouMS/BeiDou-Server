/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

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
 Assistant Nancy
 -- By ---------------------------------------------------------------------------------------------
 Angel (get31720)
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Angel
 2.0 - Second Version by happydud3 & XotiCraze
 3.0 - Third Version by RonanLana (HeavenMS)
 ---------------------------------------------------------------------------------------------------
 **/

var status;
var eim;
var hasEngage;
var hasRing;

function start() {
    eim = cm.getEventInstance();
    if (eim == null) {
        cm.warp(680000000, 0);
        cm.dispose();
        return;
    }

    if (cm.getMapId() == 680000200) {
        if (eim.getIntProperty("weddingStage") == 0) {
            cm.sendNext("客人们正在这里聚集。请稍等一会儿，仪式很快就会开始。");
        } else {
            cm.warp(680000210, "sp");
            cm.sendNext("选择你的座位在这里，祝你观影愉快！");
        }

        cm.dispose();
    } else {
        if (cm.getPlayer().getId() != eim.getIntProperty("groomId") && cm.getPlayer().getId() != eim.getIntProperty("brideId")) {
            cm.sendNext("抱歉，现在只有结婚的夫妇才能和我交谈。");
            cm.dispose();
            return;
        }

        hasEngage = false;
        for (var i = 4031357; i <= 4031364; i++) {
            if (cm.haveItem(i)) {
                hasEngage = true;
                break;
            }
        }

        var rings = [1112806, 1112803, 1112807, 1112809];
        hasRing = false;
        for (i = 0; i < rings.length; i++) {
            if (cm.getPlayer().haveItemWithId(rings[i], true)) {
                hasRing = true;
            }
        }

        status = -1;
        action(1, 0, 0);
    }
}

function action(mode, type, selection) {
    if (mode == -1 || mode == 0) {
        cm.sendOk("再见。");
        cm.dispose();
        return;
    } else if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 0) {
        var hasGoldenLeaf = cm.haveItem(4000313);

        if (hasGoldenLeaf && hasEngage) {
            cm.sendOk("你还不能离开！你需要点击高阶祭司约翰并结婚，然后我才能让你离开。");
            cm.dispose();
        } else if (hasGoldenLeaf && hasRing) {
            var choice = Array("Go to the Afterparty", "What should I be doing");
            var msg = "What can I help you with?#b";
            for (i = 0; i < choice.length; i++) {
                msg += "\r\n#L" + i + "#" + choice[i] + "#l";
            }
            cm.sendSimple(msg);
        } else {
            cm.sendNext("你似乎没有金枫叶、订婚戒指或结婚戒指。你一定不属于这里，所以我会带你去阿莫利亚。");
            selection = 20; // Random.
        }
    } else if (status == 1) {
        var cmPartner;
        try {
            cmPartner = cm.getMap().getCharacterById(cm.getPlayer().getPartnerId()).getAbstractPlayerInteraction();
        } catch (err) {
            cmPartner = null;
        }

        switch (selection) {
            case 0:
                if (eim.getIntProperty("isPremium") == 1) {
                    eim.warpEventTeam(680000300);
                    cm.sendOk("享受吧！永远珍惜你的照片！");
                    if (cmPartner != null) {
                        cmPartner.npcTalk(cm.getNpc(), "Enjoy! Cherish your Photos Forever!");
                    }
                } else {    // skip the party-time (premium only)
                    eim.warpEventTeam(680000500);
                    cm.sendOk("恭喜新人！我会护送你们到出口。");
                    if (cmPartner != null) {
                        cmPartner.npcTalk(cm.getNpc(), "Congratulations for the newly-wed! I will escort you to the exit.");
                    }
                }

                cm.dispose();
                break;

            case 1:
                cm.sendOk("新郎新娘必须得到大主教约翰的祝福才能结婚。当你准备好了，你可以点击我去参加婚礼后的派对。");
                cm.dispose();
                break;

            default:
                cm.warp(680000000, 0);
                cm.dispose();
                break;
        }
    }
}