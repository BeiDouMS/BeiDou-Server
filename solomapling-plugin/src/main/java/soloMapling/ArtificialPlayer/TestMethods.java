package soloMapling.ArtificialPlayer;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Portal;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementRecording;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.TimeCoordinatePair;


import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.botEnterFMRoom;
import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.botExitFMRoom;
import static soloMapling.ArtificialPlayer.BotGeneration.getConsoleBot;
import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.getMovementRecording;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.BotMoveStreamHelper;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.BotMoveStreamOffset;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.BotMoveStreamUntilStopPoint;
//import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.injectIntoMovementStreamWithEndPoint;
//import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.FMMovementCommands.MoveBetweenTwoDoorsSameRow;
//import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.FMMovementCommands.moveBetweenFMRowsUp;
import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.FMMovementCommands.moveToFMDoor;
//import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.PathFinderbeta.pathFinderTest;
import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.PathFinder.coordPathBuilder;
import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.PathFinder.coordPathSplitter;
import static soloMapling.DebugUtilities.debugprint;
import static soloMapling.ArtificialPlayer.BotCommandsPack.MapleMessengerCommands.addBotToMessenger;

public class TestMethods {

    public static void testBotMoveCSV(Character fakechar, String recName) {
//        BotMoveStreamCSV(fakechar, recName);
    }

    // old version .txt
//    public static void testBotMove(Character fakechar, String recName) {
//        BotMoveStream(getMovementDataMap(fakechar.getMapId(), recName), fakechar);
//    }

    public static void testBotMove(Character fakechar, String recName) {
//        List<MovementPacket> mvp = readPacketsFromFile(fakechar.getMapId(), recName);
        MovementRecording mvr = getMovementRecording(fakechar.getMapId(), recName);
        BotMoveStreamHelper(mvr, fakechar, false, null);
    }

    public static void testBotMoveInjectedIntoStream(Character fakechar, String recName) {
//        List<MovementPacket> mvp = readPacketsFromFile(fakechar.getMapId(), recName);
        MovementRecording mvr = getMovementRecording(fakechar.getMapId(), recName);
//        injectIntoMovementStreamWithEndPoint(mvr, fakechar, new Point(1051, 4));
    }

    public static void pathTest(Character fakechar) {

//        Point endPt = new Point(358, -86); // right ladder
//        Point endPt = new Point(-192,-90); // left ladder // 192,-90
//        Point endPt = new Point(5,-206); // left middle
//        Point endPt = new Point(250,-206); // right middle
//        Point endPt = new Point(318,-416); // top
        Point endPt = new Point(118, -416); // top ladder
//        Point endPt = new Point(812,-146); // right mid
//        Point endPt = new Point(735,-281); // right far ladder
//        Point endPt = new Point(72,34); // bottom row

        List<String> recName = new ArrayList<>(List.of("fmhen1", "fmhen3")); // ,"fmhen2", , "fmhen4"
//        pathFinderTest(fakechar, endPt, recName);
    }

    // todo use this for room walker
    public static void fmRoomPathWithStops(Character fakechar, Point endPt) {
        List<TimeCoordinatePair> tcpSplit = coordPathSplitter(coordPathBuilder(fakechar, endPt), 10);
        for (TimeCoordinatePair cp : tcpSplit) {
            MovementCommands.pathFinderBeta(fakechar, cp.getPoints().getLast());
            BotHelpers.blockingSleep(2500);
        }
    }

    public static void botmovetest(Character fakechar, String recName) {
//        testAllDoorPathCombosSameRow(fakechar, recName);
//        testMovingBetweenFMRows(fakechar, 1, 4);
        testMovingBetweenDoors(fakechar);
//        dropDownRow(fakechar);
//        navigateToNearestDropDoor(fakechar);
//        dropToLowerRow(fakechar);

//        Point pos = new Point(400,4);
//        BotMoveSmallDistanceX(fakechar, pos);
    }

    public static void testMovingBetweenDoors(Character fakechar) {
        for (int s = 1; s <= 6; s++) {
            for (int e = 7; e <= 12; e++) {
//            for (int e = 12; e >= 7; e--) {
                if (s == e) {
                    continue;
                }
                System.out.println(s + " -> " + e);
                moveToFMDoor(fakechar, e);
                BotHelpers.blockingSleep(1000);

                System.out.println(e + " -> " + s);
                moveToFMDoor(fakechar, s);
                BotHelpers.blockingSleep(1000);

            }
            BotHelpers.blockingSleep(1000);
        }
        System.out.println("end");
    }

//    public static void testMovingBetweenFMRows(Character fakechar, int startRow, int endRow) {
//        for (int s = 1; s <= 4; s++) {
//            for (int e = 1; e <= 4; e++) {
//                SocialCommands.BotSpeak(fakechar, s + " -> " + e);
//                moveBetweenFMRowsUp(fakechar, s, e);
//                SocialCommands.BotEmote(fakechar, 2);
//                BotHelpers.blockingSleep(2000);
//            }
//            BotHelpers.blockingSleep(1000);
//        }
//    }
//
//    public static void testAllDoorPathCombosSameRow(Character fakechar, String recName) {
////        MovementRecordingRaw mvraw = getMovementRecordingRaw(fakechar.getMapId(), recName);
//        for (int s = 7; s <= 12; s++) {
//            for (int e = 7; e <= 12; e++) {
//                if (s == e) {
//                    continue;
//                }
//                MoveBetweenTwoDoorsSameRow(fakechar, s, e);
//            }
//            BotHelpers.blockingSleep(1000);
//        }
//        BotHelpers.blockingSleep(2000);
//    }


    public static void testBotMoveMod(Character fakechar, String recName) {
//        List<MovementPacket> mvp = readPacketsFromFile(0, recName);
        MovementRecording mvr = getMovementRecording(0, recName);
        BotMoveStreamOffset(mvr, fakechar);
    }

    public static void testBotMoveStopPoint(Character fakechar, String recName, Point stopPoint) {
//        List<MovementPacket> mvp = readPacketsFromFile(0, recName);
        MovementRecording mvr = getMovementRecording(0, recName);
        BotMoveStreamUntilStopPoint(mvr, fakechar, stopPoint);
    }

    public static void testBotExitAllRooms(Character fakechar) {
        for (int i = 1; i < 23; i++) {
            botExitFMRoom(fakechar, i);
            BotHelpers.blockingSleep(500);
        }
    }

    public static void testBotEnterAllRoms(Character fakechar) {
        for (int i = 1; i < 23; i++) {
            botEnterFMRoom(fakechar, i);
            BotHelpers.blockingSleep(7000);
        }
    }

    public static void testBotEnterExitAllRooms(Character fakechar) {
        for (int i = 1; i < 23; i++) {
            botEnterFMRoom(fakechar, i);
            BotHelpers.blockingSleep(4000);
            botExitFMRoom(fakechar, i);
            BotHelpers.blockingSleep(4000);
        }
    }

    public static void findClosestTPPortal(Client c) {
        MapleMap mymap = c.getPlayer().getMap();
        Point mypos = c.getPlayer().getPosition();
        debugprint("mypos: " + mypos);
        Portal closest_tp_portal = mymap.findClosestTeleportPortal(mypos);
        System.out.println("Closest tp portal: " + closest_tp_portal.getName() + ", "
                + closest_tp_portal.getId() + ", " + closest_tp_portal.getPosition() + ", " + closest_tp_portal.getTargetMapId());
    }

    /*
    Test Dev Commands
     */

    public static void addMMC(Client c) {
        addBotToMessenger(c.getPlayer(), getConsoleBot());
    }


}
