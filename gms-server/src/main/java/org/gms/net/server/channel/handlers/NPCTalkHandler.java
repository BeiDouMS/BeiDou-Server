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

import org.gms.client.Client;
import org.gms.client.processor.npc.DueyProcessor;
import org.gms.config.GameConfig;
import org.gms.constants.id.MapId;
import org.gms.constants.id.NpcId;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.scripting.npc.NPCScriptManager;
import org.gms.server.life.NPC;
import org.gms.server.life.PlayerNPC;
import org.gms.server.maps.MapObject;
import org.gms.util.PacketCreator;

public final class NPCTalkHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(NPCTalkHandler.class);

    @Override
    public void handlePacket(InPacket p, Client c) {
        if (c.getPlayer().getMapId() == MapId.JAIL) {   //监狱地图不可使用脚本
            c.getPlayer().dropMessage(1,I18nUtil.getMessage("ActionHandler.map.message1"));
            c.sendPacket(PacketCreator.enableActions());
            return;
        } else if (!c.getPlayer().isAlive()) {
            c.sendPacket(PacketCreator.enableActions());
            return;
        }

        if (currentServerTime() - c.getPlayer().getNpcCooldown() < GameConfig.getServerInt("block_npc_race_condition")) {
            c.sendPacket(PacketCreator.enableActions());
            return;
        }

        int oid = p.readInt();
        MapObject obj = c.getPlayer().getMap().getMapObject(oid);
        if (obj instanceof NPC npc) {
            if (GameConfig.getServerBoolean("use_debug") && c.getPlayer().isGM()) {
                c.getPlayer().dropMessage(5, I18nUtil.getMessage("NPCTalkHandler.handlePacket.message1") + npc.getId());
            }

            if (npc.getId() == NpcId.DUEY) {
                DueyProcessor.dueySendTalk(c, false);
            } else {
                if (c.getCM() != null || c.getQM() != null) {
                    c.sendPacket(PacketCreator.enableActions());
                    return;
                }

                // Custom handling to reduce the amount of scripts needed.
                if (npc.getId() >= NpcId.GACHAPON_MIN && npc.getId() <= NpcId.GACHAPON_MAX) {
                    NPCScriptManager.getInstance().start(c, npc.getId(), "gachapon", null);
                } else if (npc.getName().endsWith("Maple TV")) {
                    NPCScriptManager.getInstance().start(c, npc.getId(), "mapleTV", null);
                } else if (GameConfig.getServerBoolean("use_rebirth_system") && npc.getId() == GameConfig.getServerInt("rebirth_npc_id")) {
                    NPCScriptManager.getInstance().start(c, npc.getId(), "rebirth", null);
                } else {
                    boolean hasNpcScript = NPCScriptManager.getInstance().start(c, npc.getId(), oid, null);
                    if (!hasNpcScript) {
                        if (!npc.hasShop()) {
                            log.warn("NPC {} ({}) is not coded", npc.getName(), npc.getId());
                            return;
                        } else if (c.getPlayer().getShop() != null) {
                            c.sendPacket(PacketCreator.enableActions());
                            return;
                        }

                        npc.sendShop(c);
                    }
                }
            }
        } else if (obj instanceof PlayerNPC pnpc) {
            NPCScriptManager nsm = NPCScriptManager.getInstance();

            if (pnpc.getScriptId() < NpcId.CUSTOM_DEV && !nsm.isNpcScriptAvailable(c, "" + pnpc.getScriptId())) {
                nsm.start(c, pnpc.getScriptId(), "rank_user", null);
            } else {
                nsm.start(c, pnpc.getScriptId(), null);
            }
        }
    }
}