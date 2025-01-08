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
package org.gms.net.server.channel.handlers;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.constants.id.MapId;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.scripting.quest.QuestScriptManager;
import org.gms.server.life.NPC;
import org.gms.server.quest.Quest;
import org.gms.util.I18nUtil;

import java.awt.*;

/**
 * @author Matze
 */
public final class QuestActionHandler extends AbstractPacketHandler {

    // isNpcNearby thanks to GabrielSin
    private static boolean isNpcNearby(InPacket p, Character player, Quest quest, int npcId) {
        Point playerP;
        Point pos = player.getPosition();

        if (p.available() >= 4) {
            playerP = new Point(p.readShort(), p.readShort());
            if (playerP.distance(pos) > 1000) {     // thanks Darter (YungMoozi) for reporting unchecked player position
                playerP = pos;
            }
        } else {
            playerP = pos;
        }

        if (!quest.isAutoStart() && !quest.isAutoComplete()) {
            NPC npc = player.getMap().getNPCById(npcId);
            if (npc == null) {
                return false;
            }

            Point npcP = npc.getPosition();
            if (Math.abs(npcP.getX() - playerP.getX()) > 1200 || Math.abs(npcP.getY() - playerP.getY()) > 800) {
                player.dropMessage(5, I18nUtil.getMessage("QuestActionHandler.isNpcNearby.message1"));
                return false;
            }
        }

        return true;
    }

    @Override
    public final void handlePacket(InPacket p, Client c) {
        byte action = p.readByte();
        short questid = p.readShort();
        Character player = c.getPlayer();
        Quest quest = Quest.getInstance(questid);
        if (player.getMapId() == MapId.JAIL) {   //监狱地图不可使用任务脚本
            player.dropMessage(1,I18nUtil.getMessage("ActionHandler.map.message1"));
            return;
        }
        switch (action) {
            case 0: // Restore lost item, Credits Darter ( Rajan )
                p.readInt();
                int itemid = p.readInt();
                quest.restoreLostItem(player, itemid);
                break;
            case 1: { // Start Quest
                int npc = p.readInt();
                if (!isNpcNearby(p, player, quest, npc)) {
                    return;
                }
                if (quest.canStart(player, npc)) {
                    boolean success = QuestScriptManager.getInstance().checkFunctionExists(c, questid, npc, "start");
                    boolean hasScriptRequirement = quest.hasScriptRequirement(false);
                    if (hasScriptRequirement && success) {
                        QuestScriptManager.getInstance().start(c, questid, npc);
                    } else {
                        quest.start(player, npc);
                    }
                }
                break;
            }
            case 2: { // Complete Quest
                int npc = p.readInt();
                if (!isNpcNearby(p, player, quest, npc)) {
                    return;
                }
                if (quest.canComplete(player, npc)) {
                    boolean success = QuestScriptManager.getInstance().checkFunctionExists(c, questid, npc, "end");
                    boolean hasScriptRequirement = quest.hasScriptRequirement(true);
                    if (hasScriptRequirement && success) {
                        QuestScriptManager.getInstance().end(c, questid, npc);
                    } else {
                        if (p.available() >= 2) {
                            int selection = p.readShort();
                            quest.complete(player, npc, selection);
                        } else {
                            quest.complete(player, npc);
                        }
                    }
                }
                break;
            }
            case 3: // forfeit quest
                quest.forfeit(player);
                break;
            case 4: { // scripted start quest
                int npc = p.readInt();
                if (!isNpcNearby(p, player, quest, npc)) {
                    return;
                }
                if (quest.canStart(player, npc)) {
                    QuestScriptManager.getInstance().start(c, questid, npc);
                }
                break;
            }
            case 5: { // scripted end quests
                int npc = p.readInt();
                if (!isNpcNearby(p, player, quest, npc)) {
                    return;
                }
                if (quest.canComplete(player, npc)) {
                    QuestScriptManager.getInstance().end(c, questid, npc);
                }
                break;
            }
        }
    }
}
