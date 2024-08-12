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
package org.gms.scripting.map;

import org.gms.client.Client;
import org.gms.client.QuestStatus;
import org.gms.constants.game.DelayedQuestUpdate;
import org.gms.constants.id.MapId;
import org.gms.scripting.AbstractPlayerInteraction;
import org.gms.server.quest.Quest;
import org.gms.util.PacketCreator;

public class MapScriptMethods extends AbstractPlayerInteraction {

    private final String rewardstring = " 勋章挑战已完成！请找勋章老人领取你的勋章。";

    public MapScriptMethods(Client c) {
        super(c);
    }

    public void displayCygnusIntro() {
        switch (c.getPlayer().getMapId()) {
            case MapId.CYGNUS_INTRO_LEAD -> {
                lockUI();
                c.sendPacket(PacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene0"));
            }
            case MapId.CYGNUS_INTRO_WARRIOR -> c.sendPacket(PacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene1"));
            case MapId.CYGNUS_INTRO_BOWMAN -> c.sendPacket(PacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene2"));
            case MapId.CYGNUS_INTRO_MAGE -> c.sendPacket(PacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene3"));
            case MapId.CYGNUS_INTRO_PIRATE -> c.sendPacket(PacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene4"));
            case MapId.CYGNUS_INTRO_THIEF -> c.sendPacket(PacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene5"));
            case MapId.CYGNUS_INTRO_CONCLUSION -> {
                lockUI();
                c.sendPacket(PacketCreator.showIntro("Effect/Direction.img/cygnusJobTutorial/Scene6"));
            }
        }
    }

    public void displayAranIntro() {
        switch (c.getPlayer().getMapId()) {
            case MapId.ARAN_TUTO_1 -> {
                lockUI();
                c.sendPacket(PacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene0"));
            }
            case MapId.ARAN_TUTO_2 -> c.sendPacket(PacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene1" + c.getPlayer().getGender()));
            case MapId.ARAN_TUTO_3 -> c.sendPacket(PacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene2" + c.getPlayer().getGender()));
            case MapId.ARAN_TUTO_4 -> c.sendPacket(PacketCreator.showIntro("Effect/Direction1.img/aranTutorial/Scene3"));
            case MapId.ARAN_POLEARM -> {
                lockUI();
                c.sendPacket(PacketCreator.showIntro("Effect/Direction1.img/aranTutorial/HandedPoleArm" + c.getPlayer().getGender()));
            }
        }
    }

    public void startExplorerExperience() {
        switch (c.getPlayer().getMapId()) {
        case 1020100: //Swordman
            c.sendPacket(PacketCreator.showIntro("Effect/Direction3.img/swordman/Scene" + c.getPlayer().getGender()));
            break;
        case 1020200: //Magician
            c.sendPacket(PacketCreator.showIntro("Effect/Direction3.img/magician/Scene" + c.getPlayer().getGender()));
            break;
        case 1020300: //Archer
            c.sendPacket(PacketCreator.showIntro("Effect/Direction3.img/archer/Scene" + c.getPlayer().getGender()));
            break;
        case 1020400: //Rogue
            c.sendPacket(PacketCreator.showIntro("Effect/Direction3.img/rogue/Scene" + c.getPlayer().getGender()));
            break;
        case 1020500: //Pirate
            c.sendPacket(PacketCreator.showIntro("Effect/Direction3.img/pirate/Scene" + c.getPlayer().getGender()));
            break;
        }
    }

    public void goAdventure() {
        lockUI();
        c.sendPacket(PacketCreator.showIntro("Effect/Direction3.img/goAdventure/Scene" + c.getPlayer().getGender()));
    }

    public void goLith() {
        lockUI();
        c.sendPacket(PacketCreator.showIntro("Effect/Direction3.img/goLith/Scene" + c.getPlayer().getGender()));
    }

    public void explorerQuest(short questid, String questName) {
        Quest quest = Quest.getInstance(questid);
        if (isQuestCompleted(questid)) {
            return;
        }
        
        if (!isQuestStarted(questid)) {
            if (!quest.forceStart(getPlayer(), 9000066)) {
                return;
            }
        }
        QuestStatus qs = getPlayer().getQuest(quest);
        if (!qs.addMedalMap(getPlayer().getMapId())) {
            return;
        }
        String status = Integer.toString(qs.getMedalProgress());
        String infoex = qs.getInfoEx(0);

        // explorer quests all have an infoex/infonumber requirement that points to another quest
        // THAT quest's progress needs to be updated for Quest.canComplete() to return true
        getPlayer().setQuestProgress(quest.getId(), (int)quest.getInfoNumber(qs.getStatus()), status);

        StringBuilder smp = new StringBuilder();
        StringBuilder etm = new StringBuilder();
        if (status.equals(infoex)) {
            etm.append("获得 ").append(questName).append(" 勋章！");
            smp.append("你获得了 <").append(questName).append(">").append(rewardstring);
            getPlayer().sendPacket(PacketCreator.getShowQuestCompletion(quest.getId()));
        } else {
            getPlayer().sendPacket(PacketCreator.earnTitleMessage(status + "/" + infoex + " 区域已探索"));
            etm.append("正在挑战 ").append(questName).append(" 勋章");
            smp.append("你正在挑战 ").append(questName).append(" 勋章。 ").append(status).append("/").append(infoex);
        }
        getPlayer().sendPacket(PacketCreator.earnTitleMessage(etm.toString()));
        showInfoText(smp.toString());
    }

    public void touchTheSky() { //29004
        Quest quest = Quest.getInstance(29004);
        if (!isQuestStarted(29004)) {
            if (!quest.forceStart(getPlayer(), 9000066)) {
                return;
            }
        }
        QuestStatus qs = getPlayer().getQuest(quest);
        if (!qs.addMedalMap(getPlayer().getMapId())) {
            return;
        }
        String status = Integer.toString(qs.getMedalProgress());
        getPlayer().setQuestProgress(quest.getId(), (int)quest.getInfoNumber(qs.getStatus()), status);
        getPlayer().sendPacket(PacketCreator.earnTitleMessage(status + "/5 已完成"));
        getPlayer().sendPacket(PacketCreator.earnTitleMessage("站在巅峰的人 勋章挑战正在进行中"));
        if (Integer.toString(qs.getMedalProgress()).equals(qs.getInfoEx(0))) {
            showInfoText("站在巅峰的人" + rewardstring);
            getPlayer().sendPacket(PacketCreator.getShowQuestCompletion(quest.getId()));
        } else {
            showInfoText("站在巅峰的人 勋章挑战正在进行中。 " + status + "/5 已完成");
        }
    }
}
