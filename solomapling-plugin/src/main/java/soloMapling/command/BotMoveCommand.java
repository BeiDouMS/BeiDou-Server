package soloMapling.command;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands;
import soloMapling.ArtificialPlayer.TestMethods;
import soloMapling.server.ExecutorServiceManager;
import soloMapling.server.SoloMaplingUtilities;

import java.awt.*;

import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botCancelChair;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botSitChair;
import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.botMoveMap;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.BotIdleStandingUpdate;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botFallDownPacket;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.findFootHoldId;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.interruptBotMovement;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.testInterruptMovement;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.testInterruptPathfinder;
import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.setMoveDataRecording;
import static soloMapling.ArtificialPlayer.BotMovementSystem.InPacketReader.setMovementDataRecordingMapId;
import static soloMapling.ArtificialPlayer.TestMethods.botmovetest;
import static soloMapling.DebugUtilities.debugprint;

public class BotMoveCommand extends Command {
    {
        setDescription("Bot Movement Commands Test.");
    }

    private static Character player;

    @Override
    public void execute(Client c, String[] params) {
        player = c.getPlayer();
        if (params.length == 0) {
            ExecutorServiceManager.getExecutorService().execute(() -> {
                player.yellowMessage("Please input an integer for cid. Try !move help");
            });
            return;
        }
        if (params.length == 1) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                handleDirectCommand(params[0], c);
            });
            return;
        }
        if (params.length == 2) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                if (SoloMaplingUtilities.isInteger(params[1])) {
                    int commandNum = Integer.parseInt(params[1]);
                    handleNumberedCommand(params[0], commandNum, c);
                } else if (!SoloMaplingUtilities.isInteger(params[0]) && !SoloMaplingUtilities.isInteger(params[1])) {
                    String commandString1 = (params[0]);
                    String commandString2 = params[1];
                    handleTwoStringCommand(commandString1, commandString2, c);
                } else {
                    player.yellowMessage("Second input not an integer");
                }
            });
            return;
        }
        if (params.length == 3) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                if (SoloMaplingUtilities.isInteger(params[1])) {
                    int commandNum = Integer.parseInt(params[1]);
                    handleBotStringCommand(params[0], commandNum, params[2], c);
                } else {
                    player.yellowMessage("Second input not an integer");
                }
            });
            return;
        }
        if (params.length == 5) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                if (SoloMaplingUtilities.isInteger(params[1])) {
                    int commandNum = Integer.parseInt(params[1]);
                    int xpos = Integer.parseInt(params[3]);
                    int ypos = Integer.parseInt(params[4]);
                    Point point = new Point(xpos, ypos);
                    handleBotPointCommand(params[0], commandNum, params[2], point, c);
                } else {
                    player.yellowMessage("Second input not an integer");
                }
            });
            return;
        }
    }


    public static void handleDirectCommand(String input, Client c) {
        switch (input.toLowerCase()) {
            case "help":
                printHelp();
                break;
            case "stoprecording":
                setMoveDataRecording(false);
                setMovementDataRecordingMapId(0);
                break;
            case "getfhy":
                int fh = c.getPlayer().getFh();
                debugprint(fh);
                break;
            case "whereami":
            case "getpos":
            case "getposition":
            case "getmypos":
            case "findfh":
                int mfh = findFootHoldId(c.getPlayer());
                Point pt = c.getPlayer().getPosition();
                MapleMap myMap = c.getPlayer().getMap();
                player.yellowMessage("Foothold: " + mfh + ", Point: " + pt.x + ", " + pt.y);
                debugprint(mfh, pt);
                break;
            case "testdc":
                //  disconnectFirstClient(c);
                break;
            case "closesttpportal":
                TestMethods.findClosestTPPortal(c);
                break;
            default:
                player.yellowMessage("Invalid command - Direct Command");
                break;
        }
    }

    public static void handleBotPointCommand(String input, int input2, String input3, Point point, Client c) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null");
            return;
        }

        player.yellowMessage("Command: " + input2 + ", arg: " + input3 + ", Point: " + point);
        switch (input.toLowerCase()) {
            case "botstop":
            case "botmovestop":
                Point stopPoint = point;
                TestMethods.testBotMoveStopPoint(fakechar, input3, stopPoint);
                break;
            case "pathfinderaerial":
            case "aerialpathfinder":
                // !move pathfinderaerial 186 test 744 -595 // pathfinderaerial <bot id> <test string> <x coord> <y coord>
                MovementCommands.testAerialPathFinder(fakechar, point);
                break;
            default:
                player.yellowMessage("Invalid command - Two Number");
                break;
        }
    }

    public static void handleBotStringCommand(String input, int input2, String input3, Client c) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null");
            return;
        }

        player.yellowMessage("Command: " + input2 + ", arg: " + input3);
        switch (input.toLowerCase()) {
            case "bot":
            case "botmove":
                TestMethods.testBotMove(fakechar, input3);
                break;
            case "botmod":
            case "botmovemod":
                TestMethods.testBotMoveMod(fakechar, input3);
                break;
            case "botinject":
            case "botmoveinject":
//                TestMethods.testBotMoveInjectedIntoStream(fakechar, input3);
                break;
            case "botmovecsv":
                TestMethods.testBotMoveCSV(fakechar, input3);
                break;
            case "botpath":
                TestMethods.pathTest(fakechar);
                break;
            case "botmovetest":
                botmovetest(fakechar, input3);
                break;
            case "movetoportal":
                MovementCommands.moveToPortal(fakechar, Integer.parseInt(input3));
                break;
            case "movetoportalenter":
                MovementCommands.moveToPortalAndEnter(fakechar, Integer.parseInt(input3));
                break;
            default:
                player.yellowMessage("Invalid command - Two Number");
                break;
        }
    }

    public static void handleNumberedCommand(String input, int input2, Client c) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null");
            return;
        }

        switch (input.toLowerCase()) {
            case "idle":
                BotIdleStandingUpdate(fakechar);
                break;
            case "fall":
                botFallDownPacket(fakechar);
                break;
            case "map":
                int mapId = 910000010;
                botMoveMap(fakechar, mapId);
                break;
            case "testallexitdoors":
                TestMethods.testBotExitAllRooms(fakechar);
                break;
            case "testalldoors":
                TestMethods.testBotEnterExitAllRooms(fakechar);
                break;
            case "pathfinder":
                MovementCommands.pathFinderBeta(fakechar, c.getPlayer().getPosition());
                break;
            case "pathaware":
                //  // Left
                // new Point(2845, 334) // Right
                MovementCommands.pathFinderAware(fakechar, new Point(1937, 334)); // c.getPlayer().getPosition()
                player.yellowMessage("Aware pathfinder started for: " + fakechar.getName());
                break;
            case "pathaware2":
                MovementCommands.pathFinderAware(fakechar, new Point(3889, 454)); // c.getPlayer().getPosition()
                player.yellowMessage("Aware pathfinder ended for: " + fakechar.getName());
                break;
            case "pathfinderaerial":
            case "aerialpathfinder":
                MovementCommands.testAerialPathFinder(fakechar, c.getPlayer().getPosition());
                break;
            case "pathpoints":
                TestMethods.fmRoomPathWithStops(fakechar, c.getPlayer().getPosition());
                break;
            case "sitchair":
                Integer chairId2 = 3010071;
                botSitChair(fakechar, chairId2);
                break;
            case "unsitchair":
            case "cancelchair":
                botCancelChair(fakechar);
                break;
            case "getbotpos":
                Point pos = fakechar.getPosition();
                int botFh = findFootHoldId(fakechar);
                int botMapId = fakechar.getMapId();
                player.yellowMessage("Bot " + fakechar.getId() + " — Pos: (" + pos.x + ", " + pos.y + "), FH: " + botFh + ", Map: " + botMapId);
                debugprint("Bot " + fakechar.getId() + " — Pos: " + pos + ", FH: " + botFh + ", Map: " + botMapId);
                break;
            case "interrupt":
            case "stop":
                interruptBotMovement(fakechar);
                player.yellowMessage("Interrupted movement for: " + fakechar.getName());
                break;
            case "testinterrupt":
                // starts pathfinder to player's position, interrupts after 2s
                testInterruptPathfinder(fakechar, c.getPlayer().getPosition(), 2000);
                player.yellowMessage("Test interrupt pathfinder done for: " + fakechar.getName());
                break;
            case "faceme":
                MovementCommands.botFaceTowardsPoint(fakechar, c.getPlayer().getPosition());
                player.yellowMessage("Bot " + fakechar.getId() + " facing towards you");
                break;
            case "wander":
                soloMapling.ArtificialPlayer.BotWanderSystem.BotWanderSystem.start(fakechar);
                player.yellowMessage("Bot " + fakechar.getId() + " wandering this map");
                break;
            case "wandernear":
                // loiter near your X (radius 150) - the "near an NPC/object" mode
                soloMapling.ArtificialPlayer.BotWanderSystem.BotWanderSystem.start(fakechar, c.getPlayer().getPosition().x, 150);
                player.yellowMessage("Bot " + fakechar.getId() + " wandering near you (r=150)");
                break;
            case "wanderstop":
                soloMapling.ArtificialPlayer.BotWanderSystem.BotWanderSystem.stop(fakechar);
                player.yellowMessage("Bot " + fakechar.getId() + " stopped wandering");
                break;
            default:
                player.yellowMessage("Invalid command - NumberedCommand");
                break;
        }
    }

    private static void printHelp() {
        player.yellowMessage("---- Bot Movement Commands (!move) ----");
        player.yellowMessage("-- Player Position --");
        player.yellowMessage("!move whereami                   - get your position & foothold");
        player.yellowMessage("!move closesttpportal            - find closest TP portal");
        player.yellowMessage("-- Recording --");
        player.yellowMessage("!move startrecording <name>      - start recording movement data");
        player.yellowMessage("!move stoprecording              - stop movement recording");
        player.yellowMessage("-- Bot Info --");
        player.yellowMessage("!move getbotpos <cid>            - get bot position, foothold, map");
        player.yellowMessage("!move faceme <cid>               - bot faces towards you");
        player.yellowMessage("-- Flavor Wander --");
        player.yellowMessage("!move wander <cid>               - stroll random legal spots (whole map)");
        player.yellowMessage("!move wandernear <cid>           - loiter near you (radius 150)");
        player.yellowMessage("!move wanderstop <cid>           - stop wandering");
        player.yellowMessage("-- Basic Movement --");
        player.yellowMessage("!move idle <cid>                 - make bot idle standing");
        player.yellowMessage("!move fall <cid>                 - make bot fall down");
        player.yellowMessage("!move map <cid>                  - move bot to map 910000010");
        player.yellowMessage("!move interrupt/stop <cid>       - stop bot movement");
        player.yellowMessage("!move testinterrupt <cid>        - interrupt after 2s delay");
        player.yellowMessage("-- Pathfinding --");
        player.yellowMessage("!move pathfinder <cid>           - pathfind to your position");
        player.yellowMessage("!move pathaware <cid>            - aware pathfind (hardcoded point)");
        player.yellowMessage("!move pathfinderaerial <cid>     - aerial pathfind to you");
        player.yellowMessage("!move pathfinderaerial <cid> <s> <x> <y> - aerial to point");
        player.yellowMessage("-- Replay --");
        player.yellowMessage("!move botmove <cid> <name>       - play movement recording");
        player.yellowMessage("!move botmovecsv <cid> <name>    - play CSV recording");
        player.yellowMessage("!move botmovetest <cid> <name>   - movement test");
        player.yellowMessage("-- Portal Navigation --");
        player.yellowMessage("!move movetoportal <cid> <id>    - move bot to portal");
        player.yellowMessage("!move movetoportalenter <cid> <id> - move to portal and enter");
        player.yellowMessage("-- Chair --");
        player.yellowMessage("!move sitchair <cid>             - bot sits on chair");
        player.yellowMessage("!move cancelchair <cid>          - bot cancels sit");
        player.yellowMessage("-- FM Room Testing --");
        player.yellowMessage("!move testallexitdoors <cid>     - test all room exits");
        player.yellowMessage("!move testalldoors <cid>         - test all room entries/exits");
        player.yellowMessage("!move pathpoints <cid>           - FM room path with stops");
    }

    public static void handleTwoStringCommand(String input, String input2, Client c) {
        switch (input.toLowerCase()) {
            case "startrecording":
                String recMovementDataName = (input2);
                player.yellowMessage("Start movement Data recording: " + recMovementDataName);
                InPacketReader.setMovementDataRecordingName(recMovementDataName);
                setMovementDataRecordingMapId(c.getPlayer().getMapId());
                setMoveDataRecording(true);
                break;

            default:
                player.yellowMessage("Invalid command Two Object");
                break;
        }
    }

}
