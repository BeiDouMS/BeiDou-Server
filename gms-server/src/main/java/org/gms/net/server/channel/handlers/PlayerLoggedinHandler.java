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

import org.gms.client.BuddyList;
import org.gms.client.BuddylistEntry;
import org.gms.client.Character;
import org.gms.client.CharacterNameAndId;
import org.gms.client.Client;
import org.gms.client.Disease;
import org.gms.client.Family;
import org.gms.client.FamilyEntry;
import org.gms.client.Mount;
import org.gms.client.SkillFactory;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.Pet;
import org.gms.client.keybind.KeyBinding;
import org.gms.config.GameConfig;
import org.gms.constants.game.GameConstants;
import org.gms.manager.ServerManager;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.net.server.PlayerBuffValueHolder;
import org.gms.net.server.Server;
import org.gms.net.server.channel.Channel;
import org.gms.net.server.channel.CharacterIdChannelPair;
import org.gms.net.server.coordinator.session.Hwid;
import org.gms.net.server.coordinator.session.SessionCoordinator;
import org.gms.net.server.coordinator.world.EventRecallCoordinator;
import org.gms.net.server.guild.Alliance;
import org.gms.net.server.guild.Guild;
import org.gms.net.server.guild.GuildPackets;
import org.gms.net.server.world.PartyCharacter;
import org.gms.net.server.world.PartyOperation;
import org.gms.net.server.world.World;
import org.gms.service.HpMpAlertService;
import org.gms.util.I18nUtil;
import org.gms.util.packets.WeddingPackets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.scripting.event.EventInstanceManager;
import org.gms.server.life.MobSkill;
import org.gms.service.NoteService;
import org.gms.util.DatabaseConnection;
import org.gms.util.PacketCreator;
import org.gms.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public final class PlayerLoggedinHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(PlayerLoggedinHandler.class);
    private static final Set<Integer> attemptingLoginAccounts = new HashSet<>();

    private final NoteService noteService;

    private static final HpMpAlertService hpMpAlertService = ServerManager.getApplicationContext().getBean(HpMpAlertService.class);

    public PlayerLoggedinHandler(NoteService noteService) {
        this.noteService = noteService;
    }

    private boolean tryAcquireAccount(int accId) {
        synchronized (attemptingLoginAccounts) {
            if (attemptingLoginAccounts.contains(accId)) {
                return false;
            }

            attemptingLoginAccounts.add(accId);
            return true;
        }
    }

    private void releaseAccount(int accId) {
        synchronized (attemptingLoginAccounts) {
            attemptingLoginAccounts.remove(accId);
        }
    }

    @Override
    public final boolean validateState(Client c) {
        return !c.isLoggedIn();
    }

    @Override
    public final void handlePacket(InPacket p, Client c) {  //角色进入频道函数入口
        final int cid = p.readInt(); // TODO: investigate if this is the "client id" supplied in PacketCreator#getServerIP()
        final Server server = Server.getInstance();


        if (!c.tryacquireClient()) {
            // thanks MedicOP for assisting on concurrency protection here
            c.sendPacket(PacketCreator.getAfterLoginError(10));
        }

        try {
            World wserv = server.getWorld(c.getWorld());
            if (wserv == null) {
                c.disconnect(true, false);
                return;
            }

            Channel cserv = wserv.getChannel(c.getChannel());
            if (cserv == null) {
                c.setChannel(1);
                cserv = wserv.getChannel(c.getChannel());

                if (cserv == null) {
                    c.disconnect(true, false);
                    return;
                }
            }

            Character player = wserv.getPlayerStorage().getCharacterById(cid);

            final Hwid hwid;
            if (player == null) {
                hwid = SessionCoordinator.getInstance().pickLoginSessionHwid(c);
                if (hwid == null) {
                    c.disconnect(true, false);
                    return;
                }
            } else {
                hwid = player.getClient().getHwid();
            }

            c.setHwid(hwid);

            if (!server.validateCharacteridInTransition(c, cid)) {
                c.disconnect(true, false);
                return;
            }

            boolean newcomer = false;
            if (player == null) {
                try {
                    player = Character.loadCharFromDB(cid, c, true);
                    newcomer = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (player == null) { //If you are still getting null here then please just uninstall the game >.>, we dont need you fucking with the logs
                    c.disconnect(true, false);
                    return;
                }
            }
            c.setPlayer(player);
            c.setAccID(player.getAccountId());

            boolean allowLogin = true;

                /*  is this check really necessary?
                if (state == Client.LOGIN_SERVER_TRANSITION || state == Client.LOGIN_NOTLOGGEDIN) {
                    List<String> charNames = c.loadCharacterNames(c.getWorld());
                    if(!newcomer) {
                        charNames.remove(player.getName());
                    }

                    for (String charName : charNames) {
                        if(wserv.getPlayerStorage().getCharacterByName(charName) != null) {
                            allowLogin = false;
                            break;
                        }
                    }
                }
                */

            int accId = c.getAccID();
            if (tryAcquireAccount(accId)) { // Sync this to prevent wrong login state for double loggedin handling
                try {
                    int state = c.getLoginState();
                    if (state != Client.LOGIN_SERVER_TRANSITION || !allowLogin) {
                        c.setPlayer(null);
                        c.setAccID(0);

                        if (state == Client.LOGIN_LOGGEDIN) {
                            c.disconnect(true, false);
                        } else {
                            c.sendPacket(PacketCreator.getAfterLoginError(7));
                        }

                        return;
                    }
                    c.updateLoginState(Client.LOGIN_LOGGEDIN);
                } finally {
                    releaseAccount(accId);
                }
            } else {
                c.setPlayer(null);
                c.setAccID(0);
                c.sendPacket(PacketCreator.getAfterLoginError(10));
                return;
            }

            if (!newcomer) {
                c.setLanguage(player.getClient().getLanguage());
                c.setCharacterSlots((byte) player.getClient().getCharacterSlots());
                player.newClient(c);
            }

            // 增加参数判断，避免给客户端发未知包导致异常
            if (GameConfig.getServerBoolean("use_server_auto_pot")) {
                // 同步HP MP提醒 考虑上面player.newClient(c)，如果在此之前发包，可能会造成错误
                player.broadcastAcquaintances(PacketCreator.updateHpMpAlert(hpMpAlertService.getHpAlert(player.getId()), hpMpAlertService.getMpAlert(player.getId())));
            }
            cserv.addPlayer(player);
            wserv.addPlayer(player);
            player.setEnteredChannelWorld();

            List<PlayerBuffValueHolder> buffs = server.getPlayerBuffStorage().getBuffsFromStorage(cid);
            if (buffs != null) {
                List<Pair<Long, PlayerBuffValueHolder>> timedBuffs = getLocalStartTimes(buffs);
                player.silentGiveBuffs(timedBuffs);
            }

            Map<Disease, Pair<Long, MobSkill>> diseases = server.getPlayerBuffStorage().getDiseasesFromStorage(cid);
            if (diseases != null) {
                player.silentApplyDiseases(diseases);
            }

            c.sendPacket(PacketCreator.getCharInfo(player));    //这里发送登录成功封包
            if (player.isHidden()) {
                if (!GameConfig.getServerBoolean("use_auto_hide_gm")) {
                    player.toggleHide(true);
                }
            } else {
                if (player.isGM() && GameConfig.getServerBoolean("use_auto_hide_gm")) {
                    player.toggleHide(true);    //设置GM角色隐身
                }
            }
            player.sendKeymap();
            player.sendQuickmap();
            player.sendMacros();

            // pot bindings being passed through other characters on the account detected thanks to Croosade dev team
            KeyBinding autohpPot = player.getKeymap().get(91);
            player.sendPacket(PacketCreator.sendAutoHpPot(autohpPot != null ? autohpPot.getAction() : 0));

            KeyBinding autompPot = player.getKeymap().get(92);
            player.sendPacket(PacketCreator.sendAutoMpPot(autompPot != null ? autompPot.getAction() : 0));

            player.getMap().addPlayer(player);
            player.visitMap(player.getMap());

            BuddyList bl = player.getBuddylist();
            int[] buddyIds = bl.getBuddyIds();
            wserv.loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : wserv.multiBuddyFind(player.getId(), buddyIds)) {
                BuddylistEntry ble = bl.get(onlineBuddy.getCharacterId());
                ble.setChannel(onlineBuddy.getChannel());
                bl.put(ble);
            }
            c.sendPacket(PacketCreator.updateBuddylist(bl.getBuddies()));

            c.sendPacket(PacketCreator.loadFamily(player));
            if (player.getFamilyId() > 0) {
                Family f = wserv.getFamily(player.getFamilyId());
                if (f != null) {
                    FamilyEntry familyEntry = f.getEntryByID(player.getId());
                    if (familyEntry != null) {
                        familyEntry.setCharacter(player);
                        player.setFamilyEntry(familyEntry);

                        c.sendPacket(PacketCreator.getFamilyInfo(familyEntry));
                        familyEntry.announceToSenior(PacketCreator.sendFamilyLoginNotice(player.getName(), true), true);
                    } else {
                        log.error(I18nUtil.getLogMessage("PlayerLoggedinHandler.error.message1"), player.getName(), f.getID());
                    }
                } else {
                    log.error(I18nUtil.getLogMessage("PlayerLoggedinHandler.error.message2"), player.getName(), player.getFamilyId());
                    c.sendPacket(PacketCreator.getFamilyInfo(null));
                }
            } else {
                c.sendPacket(PacketCreator.getFamilyInfo(null));
            }

            if (player.getGuildId() > 0) {
                Guild playerGuild = server.getGuild(player.getGuildId(), player.getWorld(), player);
                if (playerGuild == null) {
                    player.deleteGuild(player.getGuildId());
                    player.getMGC().setGuildId(0);
                    player.getMGC().setGuildRank(5);
                } else {
                    playerGuild.getMGC(player.getId()).setCharacter(player);
                    player.setMGC(playerGuild.getMGC(player.getId()));
                    server.setGuildMemberOnline(player, true, c.getChannel());
                    c.sendPacket(GuildPackets.showGuildInfo(player));
                    int allianceId = player.getGuild().getAllianceId();
                    if (allianceId > 0) {
                        Alliance newAlliance = server.getAlliance(allianceId);
                        if (newAlliance == null) {
                            newAlliance = Alliance.loadAlliance(allianceId);
                            if (newAlliance != null) {
                                server.addAlliance(allianceId, newAlliance);
                            } else {
                                player.getGuild().setAllianceId(0);
                            }
                        }
                        if (newAlliance != null) {
                            c.sendPacket(GuildPackets.updateAllianceInfo(newAlliance, c.getWorld()));
                            c.sendPacket(GuildPackets.allianceNotice(newAlliance.getId(), newAlliance.getNotice()));

                            if (newcomer) {
                                server.allianceMessage(allianceId, GuildPackets.allianceMemberOnline(player, true), player.getId(), -1);
                            }
                        }
                    }
                }
            }
            //展示服务信息
            noteService.show(player);
            //异常地图掉线信息提示
            c.getSysRescue().showMapChangeMessage();

            if (player.getParty() != null) {
                PartyCharacter pchar = player.getMPC();

                //Use this in case of enabling party HPbar HUD when logging in, however "you created a party" will appear on chat.
                //c.sendPacket(PacketCreator.partyCreated(pchar));

                pchar.setChannel(c.getChannel());
                pchar.setMapId(player.getMapId());
                pchar.setOnline(true);
                wserv.updateParty(player.getParty().getId(), PartyOperation.LOG_ONOFF, pchar);
                player.updatePartyMemberHP();
            }

            Inventory eqpInv = player.getInventory(InventoryType.EQUIPPED);
            eqpInv.lockInventory();
            try {
                for (Item it : eqpInv.list()) {
                    player.equippedItem((Equip) it);
                }
            } finally {
                eqpInv.unlockInventory();
            }

            c.sendPacket(PacketCreator.updateBuddylist(player.getBuddylist().getBuddies()));

            CharacterNameAndId pendingBuddyRequest = c.getPlayer().getBuddylist().pollPendingRequest();
            if (pendingBuddyRequest != null) {
                c.sendPacket(PacketCreator.requestBuddylistAdd(pendingBuddyRequest.getId(), c.getPlayer().getId(), pendingBuddyRequest.getName()));
            }

            c.sendPacket(PacketCreator.updateGender(player));
            player.checkMessenger();
            c.sendPacket(PacketCreator.enableReport());
            player.changeSkillLevel(SkillFactory.getSkill(10000000 * player.getJobType() + 12), (byte) (player.getLinkedLevel() / 10), 20, -1);
            player.checkBerserk(player.isHidden());

            if (newcomer) {
                for (Pet pet : player.getPets()) {
                    if (pet != null) {
                        wserv.registerPetHunger(player, player.getPetIndex(pet));
                    }
                }

                Mount mount = player.getMapleMount();   // thanks Ari for noticing a scenario where Silver Mane quest couldn't be started
                if (mount.getItemId() != 0) {
                    player.sendPacket(PacketCreator.updateMount(player.getId(), mount, false));
                }

                player.reloadQuestExpirations();

                    /*
                    if (!c.hasVotedAlready()){
                        player.sendPacket(PacketCreator.earnTitleMessage("You can vote now! Vote and earn a vote point!"));
                    }
                    */
                if (player.isGM()) {
                    Server.getInstance().broadcastGMMessage(c.getWorld(), PacketCreator.earnTitleMessage((player.gmLevel() < 6 ? "GM " : "Admin ") + player.getName() + " 登录了游戏"));
                }else {
                    if (GameConfig.getServerBoolean("use_login_notification")) {
                        String msg = I18nUtil.getMessage("Character.login.globalNotice", player.getName());
                        Server.getInstance().broadcastMessage(c.getWorld(), PacketCreator.serverNotice(3, c.getChannel(), msg));
                    }
                }
                if (diseases != null) {
                    for (Entry<Disease, Pair<Long, MobSkill>> e : diseases.entrySet()) {
                        final List<Pair<Disease, Integer>> debuff = Collections.singletonList(new Pair<>(e.getKey(), e.getValue().getRight().getX()));
                        c.sendPacket(PacketCreator.giveDebuff(debuff, e.getValue().getRight()));
                    }
                }
            } else {
                if (player.isRidingBattleship()) {
                    player.announceBattleshipHp();
                }
            }

            player.buffExpireTask();
            player.diseaseExpireTask();
            player.skillCooldownTask();
            player.expirationTask();
            player.questExpirationTask();
            if (GameConstants.hasSPTable(player.getJob()) && player.getJob().getId() != 2001) {
                player.createDragon();
            }

            player.commitExcludedItems();
            showDueyNotification(c, player);

            player.resetPlayerRates();
            if (GameConfig.getServerBoolean("use_add_rates_by_level")) {
                player.setPlayerRates();
            }

            player.setWorldRates();
            player.updateCouponRates();

            player.receivePartyMemberHP();

            if (player.getPartnerId() > 0) {
                int partnerId = player.getPartnerId();
                final Character partner = wserv.getPlayerStorage().getCharacterById(partnerId);

                if (partner != null && !partner.isAwayFromWorld()) {
                    player.sendPacket(WeddingPackets.OnNotifyWeddingPartnerTransfer(partnerId, partner.getMapId()));
                    partner.sendPacket(WeddingPackets.OnNotifyWeddingPartnerTransfer(player.getId(), player.getMapId()));
                }
            }

            if (newcomer) {
                EventInstanceManager eim = EventRecallCoordinator.getInstance().recallEventInstance(cid);
                if (eim != null) {
                    eim.registerPlayer(player);
                }
            }

            // Tell the client to use the custom scripts available for the NPCs provided, instead of the WZ entries.
            if (GameConfig.getServerBoolean("use_npcs_scriptable")) {

                // Create a copy to prevent always adding entries to the server's list.
                Map<Integer, String> npcsIds = GameConfig.getServerObject("npcs_scriptable", new HashMap<>());

                // Any npc be specified as the rebirth npc. Allow the npc to use custom scripts explicitly.
                if (GameConfig.getServerBoolean("use_rebirth_system")) {
                    npcsIds.put(GameConfig.getServerInt("rebirth_npc_id"), "Rebirth");
                }

                c.sendPacket(PacketCreator.setNPCScriptable(npcsIds));
            }

            if (newcomer) {
                player.setLoginTime(System.currentTimeMillis());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.releaseClient();
        }
    }

    private static void showDueyNotification(Client c, Character player) {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT Type FROM dueypackages WHERE ReceiverId = ? AND Checked = 1 ORDER BY Type DESC")) {
            ps.setInt(1, player.getId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement ps2 = con.prepareStatement("UPDATE dueypackages SET Checked = 0 WHERE ReceiverId = ?")) {
                        ps2.setInt(1, player.getId());
                        ps2.executeUpdate();

                        c.sendPacket(PacketCreator.sendDueyParcelNotification(rs.getInt("Type") == 1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static List<Pair<Long, PlayerBuffValueHolder>> getLocalStartTimes(List<PlayerBuffValueHolder> lpbvl) {
        List<Pair<Long, PlayerBuffValueHolder>> timedBuffs = new ArrayList<>();
        long curtime = currentServerTime();

        for (PlayerBuffValueHolder pb : lpbvl) {
            timedBuffs.add(new Pair<>(curtime - pb.usedTime, pb));
        }

        timedBuffs.sort((p1, p2) -> p1.getLeft().compareTo(p2.getLeft()));

        return timedBuffs;
    }
}
