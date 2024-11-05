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
package org.gms.server.life;

import lombok.Getter;
import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.inventory.InventoryType;
import org.gms.config.GameConfig;
import org.gms.constants.game.GameConstants;
import org.gms.constants.id.NpcId;
import org.gms.dao.entity.PlayernpcsDO;
import org.gms.dao.entity.PlayernpcsEquipDO;
import org.gms.manager.ServerManager;
import org.gms.net.server.Server;
import org.gms.net.server.channel.Channel;
import org.gms.net.server.world.World;
import org.gms.service.NpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.life.positioner.PlayerNPCPodium;
import org.gms.server.life.positioner.PlayerNPCPositioner;
import org.gms.server.maps.AbstractMapObject;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.server.maps.MapleMap;
import org.gms.util.DatabaseConnection;
import org.gms.util.PacketCreator;
import org.gms.util.Pair;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author XoticStory
 * @author Ronan
 */
// TODO: remove dependency on custom Npc.wz. All NPCs with id 9901910 and above are custom additions for player npcs.
// In summary: NPCs 9901910-9906599 and 9977777 are custom additions to HeavenMS that should be removed.
public class PlayerNPC extends AbstractMapObject {
    private static final Logger log = LoggerFactory.getLogger(PlayerNPC.class);
    private static final Map<Byte, List<Integer>> availablePlayerNpcScriptIds = new HashMap<>();
    private static final AtomicInteger runningOverallRank = new AtomicInteger();
    private static final List<AtomicInteger> runningWorldRank = new ArrayList<>();
    private static final Map<Pair<Integer, Integer>, AtomicInteger> runningWorldJobRank = new HashMap<>();
    private static final NpcService npcService = ServerManager.getApplicationContext().getBean(NpcService.class);

    @Getter
    private Map<Short, Integer> equips = new HashMap<>();
    @Getter
    private int scriptId;
    @Getter
    private int face;
    @Getter
    private int hair;
    @Getter
    private int gender;
    @Getter
    private int job;
    @Getter
    private byte skin;
    @Getter
    private String name = "";
    @Getter
    private int dir;
    @Getter
    private int FH;
    @Getter
    private int RX0;
    @Getter
    private int RX1;
    @Getter
    private int CY;
    private int worldRank, overallRank, worldJobRank, overallJobRank;

    public PlayerNPC(String name, int scriptId, int face, int hair, int gender, byte skin, Map<Short, Integer> equips, int dir, int FH, int RX0, int RX1, int CX, int CY, int oid) {
        this.equips = equips;
        this.scriptId = scriptId;
        this.face = face;
        this.hair = hair;
        this.gender = gender;
        this.skin = skin;
        this.name = name;
        this.dir = dir;
        this.FH = FH;
        this.RX0 = RX0;
        this.RX1 = RX1;
        this.CY = CY;
        this.job = 7777;    // supposed to be developer

        setPosition(new Point(CX, CY));
        setObjectId(oid);
    }

    public PlayerNPC(PlayernpcsDO npcDO, List<PlayernpcsEquipDO> equipDOList) {
        CY = Optional.ofNullable(npcDO.getCy()).orElse(0);
        name = Optional.ofNullable(npcDO.getName()).orElse("");
        hair = Optional.ofNullable(npcDO.getHair()).orElse(0);
        face = Optional.ofNullable(npcDO.getFace()).orElse(0);
        skin = Optional.ofNullable(npcDO.getSkin()).map(Integer::byteValue).orElse((byte) 0);
        gender = Optional.ofNullable(npcDO.getGender()).orElse(0);
        dir = Optional.ofNullable(npcDO.getDir()).orElse(0);
        FH = Optional.ofNullable(npcDO.getFh()).orElse(0);
        RX0 = Optional.ofNullable(npcDO.getRx0()).orElse(0);
        RX1 = Optional.ofNullable(npcDO.getRx1()).orElse(0);
        scriptId = Optional.ofNullable(npcDO.getScriptid()).orElse(0);
        worldRank = Optional.ofNullable(npcDO.getWorldrank()).orElse(0);
        overallRank = Optional.ofNullable(npcDO.getOverallrank()).orElse(0);
        worldJobRank = Optional.ofNullable(npcDO.getWorldjobrank()).orElse(0);
        overallJobRank = GameConstants.getOverallJobRankByScriptId(scriptId);
        job = Optional.ofNullable(npcDO.getJob()).orElse(0);
        setPosition(new Point(Optional.ofNullable(npcDO.getX()).orElse(0), CY));
        int id = Optional.ofNullable(npcDO.getId()).orElse(0);
        setObjectId(id);
        equipDOList.forEach(equipDO -> equips.put(Optional.ofNullable(equipDO.getEquippos()).orElse((short) 0), equipDO.getEquipid()));
    }

    public static void loadRunningRankData(int worlds) {
        List<PlayernpcsDO> playernpcsDOList = npcService.getPlayerNpcDOs(new PlayernpcsDO());
        runningOverallRank.set(playernpcsDOList.size() + 1);

        for (int i = 0; i < worlds; i++) {
            runningWorldRank.add(new AtomicInteger(1));
        }

        playernpcsDOList.forEach(playernpcsDO -> {
            if (playernpcsDO.getWorldrank() > runningWorldRank.get(playernpcsDO.getWorld()).get()) {
                runningWorldRank.get(playernpcsDO.getWorld()).set(playernpcsDO.getWorldrank());
            }
            Pair<Integer, Integer> worldJobPair = new Pair<>(playernpcsDO.getWorld(), playernpcsDO.getJob());
            AtomicInteger worldJobRank = runningWorldJobRank.get(worldJobPair);
            if (worldJobRank == null) {
                worldJobRank = new AtomicInteger(1);
            }
            if (playernpcsDO.getWorldjobrank() > worldJobRank.get()) {
                runningWorldJobRank.put(worldJobPair, worldJobRank);
            }
        });
    }

    public int getWorldRank() {
        return worldRank;
    }

    public int getOverallRank() {
        return overallRank;
    }

    public int getWorldJobRank() {
        return worldJobRank;
    }

    public int getOverallJobRank() {
        return overallJobRank;
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.PLAYER_NPC;
    }

    @Override
    public void sendSpawnData(Client client) {
        client.sendPacket(PacketCreator.spawnPlayerNPC(this));
        client.sendPacket(PacketCreator.getPlayerNPC(this));
    }

    @Override
    public void sendDestroyData(Client client) {
        client.sendPacket(PacketCreator.removeNPCController(this.getObjectId()));
        client.sendPacket(PacketCreator.removePlayerNPC(this.getObjectId()));
    }

    private static int getAndIncrementRunningWorldJobRanks(int world, int job) {
        AtomicInteger wjr = runningWorldJobRank.computeIfAbsent(new Pair<>(world, job), k -> new AtomicInteger(1));
        return wjr.getAndIncrement();
    }

    public static boolean canSpawnPlayerNpc(String name, int mapid) {
        List<PlayernpcsDO> playerNpcDOs = npcService.getPlayerNpcDOs(PlayernpcsDO.builder().name(name).map(mapid).build());
        return playerNpcDOs.isEmpty();
    }

    public void updatePlayerNPCPosition(MapleMap map, Point newPos) {
        setPosition(newPos);
        RX0 = newPos.x + 50;
        RX1 = newPos.x - 50;
        CY = newPos.y;
        FH = map.getFootholds().findBelow(newPos).getId();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE playernpcs SET x = ?, cy = ?, fh = ?, rx0 = ?, rx1 = ? WHERE id = ?")) {
            ps.setInt(1, newPos.x);
            ps.setInt(2, CY);
            ps.setInt(3, FH);
            ps.setInt(4, RX0);
            ps.setInt(5, RX1);
            ps.setInt(6, getObjectId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void fetchAvailableScriptIdsFromDb(byte branch, List<Integer> list) {
        try {
            int branchLen = (branch < 26) ? 100 : 400;
            int branchSid = NpcId.PLAYER_NPC_BASE + (branch * 100);
            int nextBranchSid = branchSid + branchLen;

            List<Integer> availables = new ArrayList<>(20);
            try (Connection con = DatabaseConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT scriptid FROM playernpcs WHERE scriptid >= ? AND scriptid < ? ORDER BY scriptid")) {
                ps.setInt(1, branchSid);
                ps.setInt(2, nextBranchSid);

                Set<Integer> usedScriptIds = new HashSet<>();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        usedScriptIds.add(rs.getInt(1));
                    }
                }

                int j = 0;
                for (int i = branchSid; i < nextBranchSid; i++) {
                    if (!usedScriptIds.contains(i)) {
                        if (PlayerNPCFactory.isExistentScriptid(i)) {  // thanks Ark, Zein, geno, Ariel, JrCl0wn for noticing client crashes due to use of missing scriptids
                            availables.add(i);
                            j++;

                            if (j == 20) {
                                break;
                            }
                        } else {
                            break;  // after this point no more scriptids expected...
                        }
                    }
                }
            }

            for (int i = availables.size() - 1; i >= 0; i--) {
                list.add(availables.get(i));
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    private static int getNextScriptId(byte branch) {
        List<Integer> availablesBranch = availablePlayerNpcScriptIds.computeIfAbsent(branch, k -> new ArrayList<>(20));

        if (availablesBranch.isEmpty()) {
            fetchAvailableScriptIdsFromDb(branch, availablesBranch);

            if (availablesBranch.isEmpty()) {
                return -1;
            }
        }

        return availablesBranch.removeLast();
    }

    private static PlayerNPC createPlayerNPCInternal(MapleMap map, Point pos, Character chr) {
        int mapId = map.getId();

        if (!canSpawnPlayerNpc(chr.getName(), mapId)) {
            return null;
        }

        byte branch = GameConstants.getHallOfFameBranch(chr.getJob(), mapId);

        int scriptId = getNextScriptId(branch);
        if (scriptId == -1) {
            return null;
        }

        if (pos == null) {
            if (GameConstants.isPodiumHallOfFameMap(map.getId())) {
                pos = PlayerNPCPodium.getNextPlayerNpcPosition(map);
            } else {
                pos = PlayerNPCPositioner.getNextPlayerNpcPosition(map);
            }

            if (pos == null) {
                return null;
            }
        }

        if (GameConfig.getServerBoolean("use_debug")) {
            log.info("GOT SID {}, POS {}", scriptId, pos);
        }

        int worldId = chr.getWorld();
        int jobId = (chr.getJob().getId() / 100) * 100;

        List<PlayernpcsDO> playerNpcDOs = npcService.getPlayerNpcDOs(PlayernpcsDO.builder().scriptid(scriptId).build());
        if (!playerNpcDOs.isEmpty()) {
            return null;
        }
        PlayernpcsDO playerNpcDO = PlayernpcsDO.builder()
                .name(chr.getName())
                .hair(chr.getHair())
                .face(chr.getFace())
                .skin(chr.getSkinColor().getId())
                .gender(chr.getGender())
                .x(pos.x)
                .cy(pos.y)
                .world(worldId)
                .map(mapId)
                .scriptid(scriptId)
                .dir(1)
                .fh(map.getFootholds().findBelow(pos).getId())
                .rx0(pos.x + 50)
                .rx1(pos.x - 50)
                .worldrank(runningWorldRank.get(worldId).getAndIncrement())
                .overallrank(runningOverallRank.getAndIncrement())
                .worldjobrank(getAndIncrementRunningWorldJobRanks(worldId, jobId))
                .job(jobId)
                .build();
        List<PlayernpcsEquipDO> playerNpcEquipDOS = chr.getInventory(InventoryType.EQUIPPED).list().stream()
                .map(equip -> PlayernpcsEquipDO.builder()
                        .equipid(equip.getItemId())
                        .equippos(equip.getPosition())
                        .build())
                .toList();
        return npcService.createPlayerNPC(playerNpcDO, playerNpcEquipDOS);
    }

    private static List<Integer> removePlayerNPCInternal(MapleMap map, Character chr) {
        Set<Integer> updateMapids = new HashSet<>();

        List<Integer> mapids = new LinkedList<>();
        mapids.add(chr.getWorld());

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, map FROM playernpcs WHERE name LIKE ?" + (map != null ? " AND map = ?" : ""))) {
            ps.setString(1, chr.getName());
            if (map != null) {
                ps.setInt(2, map.getId());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    updateMapids.add(rs.getInt("map"));
                    int npcId = rs.getInt("id");

                    try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM playernpcs WHERE id = ?")) {
                        ps2.setInt(1, npcId);
                        ps2.executeUpdate();
                    }

                    try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ?")) {
                        ps2.setInt(1, npcId);
                        ps2.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mapids.addAll(updateMapids);

        return mapids;
    }

    private static synchronized Pair<PlayerNPC, List<Integer>> processPlayerNPCInternal(MapleMap map, Point pos, Character chr, boolean create) {
        if (create) {
            return new Pair<>(createPlayerNPCInternal(map, pos, chr), null);
        } else {
            return new Pair<>(null, removePlayerNPCInternal(map, chr));
        }
    }

    public static boolean spawnPlayerNPC(int mapid, Character chr) {
        return spawnPlayerNPC(mapid, null, chr);
    }

    public static boolean spawnPlayerNPC(int mapid, Point pos, Character chr) {
        if (chr == null) {
            return false;
        }

        PlayerNPC pn = processPlayerNPCInternal(chr.getClient().getChannelServer().getMapFactory().getMap(mapid), pos, chr, true).getLeft();
        if (pn != null) {
            for (Channel channel : Server.getInstance().getChannelsFromWorld(chr.getWorld())) {
                MapleMap m = channel.getMapFactory().getMap(mapid);

                m.addPlayerNPCMapObject(pn);
                m.broadcastMessage(PacketCreator.spawnPlayerNPC(pn));
                m.broadcastMessage(PacketCreator.getPlayerNPC(pn));
            }

            return true;
        } else {
            return false;
        }
    }

    private static PlayerNPC getPlayerNPCFromWorldMap(String name, int world, int map) {
        World wserv = Server.getInstance().getWorld(world);
        for (MapObject pnpcObj : wserv.getChannel(1).getMapFactory().getMap(map).getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapObjectType.PLAYER_NPC))) {
            PlayerNPC pn = (PlayerNPC) pnpcObj;

            if (name.contentEquals(pn.getName()) && pn.getScriptId() < NpcId.CUSTOM_DEV) {
                return pn;
            }
        }

        return null;
    }

    public static void removePlayerNPC(Character chr) {
        if (chr == null) {
            return;
        }

        List<Integer> updateMapids = processPlayerNPCInternal(null, null, chr, false).getRight();
        int worldid = updateMapids.removeFirst();

        for (Integer mapid : updateMapids) {
            PlayerNPC pn = getPlayerNPCFromWorldMap(chr.getName(), worldid, mapid);

            if (pn != null) {
                for (Channel channel : Server.getInstance().getChannelsFromWorld(worldid)) {
                    MapleMap m = channel.getMapFactory().getMap(mapid);
                    m.removeMapObject(pn);

                    m.broadcastMessage(PacketCreator.removeNPCController(pn.getObjectId()));
                    m.broadcastMessage(PacketCreator.removePlayerNPC(pn.getObjectId()));
                }
            }
        }
    }

    public static void multicastSpawnPlayerNPC(int mapid, int world) {
        World wserv = Server.getInstance().getWorld(world);
        if (wserv == null) {
            return;
        }

        Client c = Client.createMock();
        c.setWorld(world);
        c.setChannel(1);

        for (Character mc : wserv.loadAndGetAllCharactersView()) {
            mc.setClient(c);
            spawnPlayerNPC(mapid, mc);
        }
    }

    public static void removeAllPlayerNPC() {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT DISTINCT world, map FROM playernpcs");
             ResultSet rs = ps.executeQuery()) {
            int wsize = Server.getInstance().getWorldsSize();
            while (rs.next()) {
                int world = rs.getInt("world"), map = rs.getInt("map");
                if (world >= wsize) {
                    continue;
                }

                for (Channel channel : Server.getInstance().getChannelsFromWorld(world)) {
                    MapleMap m = channel.getMapFactory().getMap(map);

                    for (MapObject pnpcObj : m.getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapObjectType.PLAYER_NPC))) {
                        PlayerNPC pn = (PlayerNPC) pnpcObj;
                        m.removeMapObject(pnpcObj);
                        m.broadcastMessage(PacketCreator.removeNPCController(pn.getObjectId()));
                        m.broadcastMessage(PacketCreator.removePlayerNPC(pn.getObjectId()));
                    }
                }
            }

            try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM playernpcs")) {
                ps2.executeUpdate();
            }

            try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM playernpcs_equip")) {
                ps2.executeUpdate();
            }

            try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM playernpcs_field")) {
                ps2.executeUpdate();
            }

            for (World w : Server.getInstance().getWorlds()) {
                w.resetPlayerNpcMapData();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addPlayerNPCMapObject(MapleMap map) {
        List<PlayerNPC> playerNPCList = npcService.getPlayerNPC(PlayernpcsDO.builder().map(map.getId()).world(map.getWorld()).build());
        playerNPCList.forEach(map::addPlayerNPCMapObject);
    }
}
