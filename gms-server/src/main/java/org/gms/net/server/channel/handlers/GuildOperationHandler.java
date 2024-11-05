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
import org.gms.config.GameConfig;
import org.gms.constants.game.GameConstants;
import org.gms.constants.id.MapId;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.net.server.Server;
import org.gms.net.server.coordinator.matchchecker.MatchCheckerListenerFactory.MatchCheckerType;
import org.gms.net.server.guild.Alliance;
import org.gms.net.server.guild.Guild;
import org.gms.net.server.guild.GuildPackets;
import org.gms.net.server.guild.GuildResponse;
import org.gms.net.server.world.Party;
import org.gms.net.server.world.World;
import org.gms.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.util.PacketCreator;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class GuildOperationHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(GuildOperationHandler.class);

    private boolean isGuildNameAcceptable(String name) {
        if (name.length() < 3 || name.length() > 12) {
            return false;
        }
        // 修改成不包含特殊字符，支持中文
        return !Pattern.compile("[^\u4e00-\u9fa5a-zA-Z0-9_]").matcher(name).find();
    }

    @Override
    public void handlePacket(InPacket p, Client c) {
        Character mc = c.getPlayer();
        byte type = p.readByte();
        int allianceId = -1;
        switch (type) {
            case 0x00:
                //c.sendPacket(PacketCreator.showGuildInfo(mc));
                break;
            case 0x02:
                if (mc.getGuildId() > 0) {
                    mc.dropMessage(1, I18nUtil.getMessage("GuildOperationHandler.handlePacket.message1"));
                    return;
                }
                if (mc.getMeso() < GameConfig.getServerInt("create_guild_cost")) {
                    mc.dropMessage(1, I18nUtil.getMessage("GuildOperationHandler.handlePacket.message2"));
                    //mc.dropMessage(1, "You do not have " + GameConstants.numberWithCommas(GameConfig.getServerInt("create_guild_cost")) + " mesos to create a Guild.");
                    return;
                }
                String guildName = p.readString();
                if (!isGuildNameAcceptable(guildName)) {
                    mc.dropMessage(1, I18nUtil.getMessage("GuildOperationHandler.handlePacket.message3"));
                    return;
                }

                Set<Character> eligibleMembers = new HashSet<>(Guild.getEligiblePlayersForGuild(mc));
                if (eligibleMembers.size() < GameConfig.getServerInt("create_guild_min_partners")) {
                    if (mc.getMap().getAllPlayers().size() < GameConfig.getServerInt("create_guild_min_partners")) {
                        // thanks NovaStory for noticing message in need of smoother info
                        mc.dropMessage(1, I18nUtil.getMessage("GuildOperationHandler.handlePacket.message4"));
                    } else {
                        // players may be unaware of not belonging on a party in order to become eligible, thanks Hair (Legalize) for pointing this out
                        mc.dropMessage(1, I18nUtil.getMessage("GuildOperationHandler.handlePacket.message5"));
                    }

                    return;
                }

                if (!Party.createParty(mc, true)) {
                    mc.dropMessage(1, I18nUtil.getMessage("GuildOperationHandler.handlePacket.message6"));
                    return;
                }

                Set<Integer> eligibleCids = new HashSet<>();
                for (Character chr : eligibleMembers) {
                    eligibleCids.add(chr.getId());
                }

                c.getWorldServer().getMatchCheckerCoordinator().createMatchConfirmation(MatchCheckerType.GUILD_CREATION, c.getWorld(), mc.getId(), eligibleCids, guildName);
                break;
            case 0x05:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() > 2) {
                    return;
                }

                String targetName = p.readString();
                GuildResponse mgr = Guild.sendInvitation(c, targetName);
                if (mgr != null) {
                    c.sendPacket(mgr.getPacket(targetName));
                } else {
                } // already sent invitation, do nothing

                break;
            case 0x06:
                if (mc.getGuildId() > 0) {
                    log.warn("[Hack] Chr {} attempted to join a guild when s/he is already in one.", mc.getName());
                    return;
                }
                int gid = p.readInt();
                int cid = p.readInt();
                if (cid != mc.getId()) {
                    log.warn("[Hack] Chr {} attempted to join a guild with a different chrId", mc.getName());
                    return;
                }

                if (!Guild.answerInvitation(cid, mc.getName(), gid, true)) {
                    return;
                }

                mc.getMGC().setGuildId(gid); // joins the guild
                mc.getMGC().setGuildRank(5); // start at lowest rank
                mc.getMGC().setAllianceRank(5);

                int s = Server.getInstance().addGuildMember(mc.getMGC(), mc);
                if (s == 0) {
                    mc.dropMessage(1, I18nUtil.getMessage("GuildOperationHandler.handlePacket.message7"));
                    mc.getMGC().setGuildId(0);
                    return;
                }

                c.sendPacket(GuildPackets.showGuildInfo(mc));

                allianceId = mc.getGuild().getAllianceId();
                if (allianceId > 0) {
                    Server.getInstance().getAlliance(allianceId).updateAlliancePackets(mc);
                }

                mc.saveGuildStatus(); // update database
                mc.getMap().broadcastPacket(mc, GuildPackets.guildNameChanged(mc.getId(), mc.getGuild().getName())); // thanks Vcoc for pointing out an issue with updating guild tooltip to players in the map
                mc.getMap().broadcastPacket(mc, GuildPackets.guildMarkChanged(mc.getId(), mc.getGuild()));
                break;
            case 0x07:
                cid = p.readInt();
                String name = p.readString();
                if (cid != mc.getId() || !name.equals(mc.getName()) || mc.getGuildId() <= 0) {
                    log.warn("[Hack] Chr {} tried to quit guild under the name {} and current guild id of {}", mc.getName(), name, mc.getGuildId());
                    return;
                }

                allianceId = mc.getGuild().getAllianceId();

                c.sendPacket(GuildPackets.updateGP(mc.getGuildId(), 0));
                Server.getInstance().leaveGuild(mc.getMGC());

                c.sendPacket(GuildPackets.showGuildInfo(null));
                if (allianceId > 0) {
                    Server.getInstance().getAlliance(allianceId).updateAlliancePackets(mc);
                }

                mc.getMGC().setGuildId(0);
                mc.getMGC().setGuildRank(5);
                mc.saveGuildStatus();
                mc.getMap().broadcastPacket(mc, GuildPackets.guildNameChanged(mc.getId(), ""));
                break;
            case 0x08:
                allianceId = mc.getGuild().getAllianceId();

                cid = p.readInt();
                name = p.readString();
                if (mc.getGuildRank() > 2 || mc.getGuildId() <= 0) {
                    log.warn("[Hack] Chr {} is trying to expel without rank 1 or 2", mc.getName());
                    return;
                }

                Server.getInstance().expelMember(mc.getMGC(), name, cid);
                if (allianceId > 0) {
                    Server.getInstance().getAlliance(allianceId).updateAlliancePackets(mc);
                }
                break;
            case 0x0d:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() != 1) {
                    log.warn("[Hack] Chr {} tried to change guild rank titles when s/he does not have permission", mc.getName());
                    return;
                }
                String[] ranks = new String[5];
                for (int i = 0; i < 5; i++) {
                    ranks[i] = p.readString();
                }

                Server.getInstance().changeRankTitle(mc.getGuildId(), ranks);
                break;
            case 0x0e:
                cid = p.readInt();
                byte newRank = p.readByte();
                if (mc.getGuildRank() > 2 || (newRank <= 2 && mc.getGuildRank() != 1) || mc.getGuildId() <= 0) {
                    log.warn("[Hack] Chr {} is trying to change rank outside of his/her permissions.", mc.getName());
                    return;
                }
                if (newRank <= 1 || newRank > 5) {
                    return;
                }
                Server.getInstance().changeRank(mc.getGuildId(), cid, newRank);
                break;
            case 0x0f:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() != 1 || mc.getMapId() != MapId.GUILD_HQ) {
                    log.warn("[Hack] Chr {} tried to change guild emblem without being the guild leader", mc.getName());
                    return;
                }
                if (mc.getMeso() < GameConfig.getServerInt("change_emblem_cost")) {
                    c.sendPacket(PacketCreator.serverNotice(1, "You do not have " + GameConstants.numberWithCommas(GameConfig.getServerInt("change_emblem_cost")) + " mesos to change the Guild emblem."));
                    return;
                }
                short bg = p.readShort();
                byte bgcolor = p.readByte();
                short logo = p.readShort();
                byte logocolor = p.readByte();
                Server.getInstance().setGuildEmblem(mc.getGuildId(), bg, bgcolor, logo, logocolor);

                if (mc.getGuild() != null && mc.getGuild().getAllianceId() > 0) {
                    Alliance alliance = mc.getAlliance();
                    Server.getInstance().allianceMessage(alliance.getId(), GuildPackets.getGuildAlliances(alliance, c.getWorld()), -1, -1);
                }

                mc.gainMeso(-GameConfig.getServerInt("change_emblem_cost"), true, false, true);
                mc.getGuild().broadcastNameChanged();
                mc.getGuild().broadcastEmblemChanged();
                break;
            case 0x10:
                if (mc.getGuildId() <= 0 || mc.getGuildRank() > 2) {
                    if (mc.getGuildId() <= 0) {
                        log.warn("[Hack] Chr {} tried to change guild notice while not in a guild", mc.getName());
                    }
                    return;
                }
                String notice = p.readString();
                if (notice.length() > 100) {
                    return;
                }
                Server.getInstance().setGuildNotice(mc.getGuildId(), notice);
                break;
            case 0x1E:
                p.readInt();
                World wserv = c.getWorldServer();

                if (mc.getParty() != null) {
                    wserv.getMatchCheckerCoordinator().dismissMatchConfirmation(mc.getId());
                    return;
                }

                int leaderid = wserv.getMatchCheckerCoordinator().getMatchConfirmationLeaderid(mc.getId());
                if (leaderid != -1) {
                    boolean result = p.readByte() != 0;
                    if (result && wserv.getMatchCheckerCoordinator().isMatchConfirmationActive(mc.getId())) {
                        Character leader = wserv.getPlayerStorage().getCharacterById(leaderid);
                        if (leader != null) {
                            int partyid = leader.getPartyId();
                            if (partyid != -1) {
                                Party.joinParty(mc, partyid, true);    // GMS gimmick "party to form guild" recalled thanks to Vcoc
                            }
                        }
                    }

                    wserv.getMatchCheckerCoordinator().answerMatchConfirmation(mc.getId(), result);
                }

                break;
            default:
                log.warn("Unhandled GUILD_OPERATION packet: {}", p);
        }
    }
}
