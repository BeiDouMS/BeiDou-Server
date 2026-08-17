package soloMapling.server;

import org.gms.client.Character;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.life.LifeFactory;
import org.gms.server.life.NPC;
import org.gms.server.maps.MapleMap;
import org.gms.util.PacketCreator;

import java.awt.*;

import static soloMapling.server.SoloMaplingUtilities.getMapleMapById;

/**
 * Utility for spawning NPCs at runtime without DB/WZ edits.
 * NPCs persist until the map is unloaded. For permanent spawns, use the plife DB table.
 */
public class NpcSpawner {

    private static final Logger log = LoggerFactory.getLogger(NpcSpawner.class);

    /**
     * Spawn an NPC at a player's current position (interactive use from commands).
     */
    public static void spawnNpcAtPlayer(Character chr, int npcId) {
        spawnNpc(npcId, chr.getMap(), chr.getPosition());
        chr.yellowMessage("NPC (id: " + npcId + ") spawned at your position. Map: " + chr.getMapId());
    }

    /**
     * Spawn an NPC on a map by map ID and coordinates (for environment startup / code-driven spawns).
     */
    public static void spawnNpc(int npcId, int mapId, int x, int y) {
        MapleMap map = getMapleMapById(mapId);
        if (map == null) {
            log.warn("[NpcSpawner] Map {} not found, cannot spawn NPC {}", mapId, npcId);
            return;
        }
        spawnNpc(npcId, map, new Point(x, y));
    }

    /**
     * Spawn an NPC on a map at a specific point.
     */
    public static void spawnNpc(int npcId, MapleMap map, Point pos) {
        NPC npc = LifeFactory.getNPC(npcId);
        if (npc == null) {
            log.warn("[NpcSpawner] Failed to load NPC: {}", npcId);
            return;
        }
        npc.setPosition(pos);
        npc.setCy(pos.y);
        npc.setRx0(pos.x + 50);
        npc.setRx1(pos.x - 50);
        npc.setFh(map.getFootholds().findBelow(pos).getId());
        map.addMapObject(npc);
        map.broadcastMessage(PacketCreator.spawnNPC(npc));
        map.broadcastMessage(PacketCreator.spawnNPCRequestController(npc, true));
        log.info("[NpcSpawner] Spawned NPC {} on map {} at ({}, {})", npcId, map.getId(), pos.x, pos.y);
    }
}
