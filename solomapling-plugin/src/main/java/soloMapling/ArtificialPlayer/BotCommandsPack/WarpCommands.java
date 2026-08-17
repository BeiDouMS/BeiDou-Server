package soloMapling.ArtificialPlayer.BotCommandsPack;

import org.gms.client.Character;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Portal;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecording;

import java.util.HashMap;
import java.util.Map;

import static soloMapling.ArtificialPlayer.BotClientHandler.getBotClient;
import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.getMovementRecording;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.BotMoveStreamOffset;

public class WarpCommands {


    public static Map<Integer, Integer> FMRoomWarpPortalId = new HashMap<>();

    static {
        FMRoomWarpPortalId.put(1, 11); // henesysDoorPortalId 1
        for (int i = 2; i <= 6; i++) {
            FMRoomWarpPortalId.put(i, 10); // henesysDoorPortalId 2-6
        }
        for (int i = 7; i <= 12; i++) {
            FMRoomWarpPortalId.put(i, 10); // Ludibrium
        }
        for (int i = 13; i <= 17; i++) {
            FMRoomWarpPortalId.put(i, 10); // Perion
        }
        for (int i = 18; i <= 22; i++) {
            FMRoomWarpPortalId.put(i, 9); // El Nath
        }

        // Fixing the bug for FM room 10
        FMRoomWarpPortalId.put(10, 11);
    }

    /*
    Bot Enters the portal they're standing on. Does not require Map id's, portal id's, or coordinates.
     */
    public static void botWarpMapOnPortal(Character fakechar) {
        Portal portal = fakechar.getMap().findClosestPortal(fakechar.getPosition());
        MapleMap to;
        if (fakechar.getEventInstance() == null) {
            to = getBotClient().getChannelServer().getMapFactory().getMap(portal.getTargetMapId());
        } else {
            to = fakechar.getEventInstance().getMapInstance(portal.getTargetMapId());
        }

        Portal pto = to.getPortal(portal.getTarget());
        if (pto == null) {// fallback for missing portals - no real life case anymore - interesting for not implemented areas
            pto = to.getPortal(0);
        }
        try {
            fakechar.changeMap(to, pto);
            botEnterPortalDropDown(fakechar);
        } catch (Exception e) {
        }
    }

    public static void botWarpMapOnPortal(Character fakechar, MapleMap warpMap, int portalId) {
        fakechar.changeMap(warpMap, portalId);
        botEnterPortalDropDown(fakechar);
    }

    public static void botEnterFMRoom(Character fakechar, int roomNumber) {
        int freeMarketRoom = 910000000 + roomNumber;
        MapleMap warpMap = getBotClient().getChannelServer().getMapFactory().getMap(freeMarketRoom);
        botWarpMapOnPortal(fakechar, warpMap, FMRoomWarpPortalId.get(roomNumber));
    }

    public static void botExitFMRoom(Character fakechar, int doorNumber) {
        int freeMarketEntrance = 910000000;
        MapleMap warpMap = getBotClient().getChannelServer().getMapFactory().getMap(freeMarketEntrance);
        botWarpMapOnPortal(fakechar, warpMap, getFMEntrancePortal(doorNumber).getId());
    }

    public static Portal getFMEntrancePortal(int doorNumber) {
        int freeMarketEntrance = 910000000;
        String portalName = String.format("in%02d", doorNumber);
        MapleMap warpMap = getBotClient().getChannelServer().getMapFactory().getMap(freeMarketEntrance);
        return warpMap.getPortal(portalName);
    }

    public static Portal getFMEUpArrows(int upArrow) {
        // upArrow 0 = row 1 -> 2
        // upArrow 1 = row 2 -> 3
        // upArrow 2 = row 3 -> 4
        int freeMarketEntrance = 910000000;
        String arrowName = String.format("up%02d", upArrow);
        MapleMap warpMap = getBotClient().getChannelServer().getMapFactory().getMap(freeMarketEntrance);
        return warpMap.getPortal(arrowName);
    }

    // Deliberate synchronous choreography: fake portal lag, then a blocking
    // recording replay. Part of the spawn/warp arrival scripts that hold their thread.
    public static void botEnterPortalDropDown(Character fakechar, int variablePortalLag) {
        BotHelpers.blockingSleep(variablePortalLag);
        String recName = "portalenterdrop";
//        List<MovementPacket> mvp = readPacketsFromFile(0, recName);
        MovementRecording mvr = getMovementRecording(0, recName);
        BotMoveStreamOffset(mvr, fakechar);
    }

    public static void botEnterPortalDropDown(Character fakechar) {
        botEnterPortalDropDown(fakechar, 1500);
    }

    //test only
    public static void botMoveMap(Character fakechar, int mapId) {
        MapleMap warpMap = getBotClient().getChannelServer().getMapFactory().getMap(mapId);
        fakechar.changeMap(warpMap, warpMap.getPortal(11));

////        MapleMap target = fakechar.getClient().getChannelServer().getMapFactory().getMap(gotomaps.get(params[0]));
//
//        Portal targetPortal = target.findMarketPortal();
////        Portal targetPortal = target.getRandomPlayerSpawnpoint();
//        fakechar.saveLocationOnWarp();
//        fakechar.changeMap(target, targetPortal);
//        // broadcast that player enter
//        // in the air packet, then dropping them
    }
}
