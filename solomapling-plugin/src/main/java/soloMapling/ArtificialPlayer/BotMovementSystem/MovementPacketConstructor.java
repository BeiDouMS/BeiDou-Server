package soloMapling.ArtificialPlayer.BotMovementSystem;

import org.gms.client.Character;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.gms.net.packet.ByteBufInPacket;
import org.gms.net.packet.InPacket;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.SingleMoveCommand;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.findFootHoldId;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.MovementPacketValues.FLYING_BLOCK;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.MovementPacketValues.HANG_ON_BACK;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.MovementPacketValues.NORMAL;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.IDLE_LEFT;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.IDLE_RIGHT;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.JUMP_LEFT;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.JUMP_RIGHT;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.SIT_LEFT;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.SIT_RIGHT;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.SingleMoveCommand.getConvertedStanceDirection;
import static soloMapling.DebugUtilities.debugprint;

public class MovementPacketConstructor {

    /*
    Note: Deconstructing and reconstructing packets is only good for very minor movement, NOT entire streams
     */
    public static List<SingleMoveCommand> deconstructMovementInPacket(InPacket p) {
        p.skip(9);
        List<SingleMoveCommand> singleMoveCommandList = new ArrayList<>();
        try {
            int movementDataStart = p.getPosition();
            byte numCommands = p.readByte();
            for (byte i = 0; i < numCommands; i++) {
                byte command = p.readByte();
                switch (command) {
                    case NORMAL: // normal move
                    case HANG_ON_BACK:
                    case FLYING_BLOCK: { // Float
                        short xpos = p.readShort(); //is signed fine here?
                        short ypos = p.readShort();
                        short xwobble = p.readShort();
                        short ywobble = p.readShort();
                        short fh = p.readShort();
                        byte newstate = p.readByte();
                        short duration = p.readShort(); //duration

                        singleMoveCommandList.add(new SingleMoveCommand(command, xpos, ypos, xwobble, ywobble, fh, newstate, duration));
//                        debugprint(movementPacketRecList.get(i).getFieldValues());
                        break;
                    }
                    case 1:
                    case 2:
                    case 6: // fj
                    case 12:
                    case 13: // Shot-jump-back thing
                    case 16: // Float
                    case 18:
                    case 19: // Springs on maps
                    case 20: // Aran Combat Step
                    case 22: {
                        //Relative movement - server only cares about stance
                        p.skip(4); //xpos = lea.readShort(); ypos = lea.readShort();
                        byte newstate = p.readByte();
//                        target.setStance(newstate);
                        short duration = p.readShort(); //duration

                        singleMoveCommandList.add(new SingleMoveCommand(command, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, newstate, duration));
//                        debugprint(movementPacketRecList.get(i).getFieldValues());

                        break;
                    }
                    case 3:
                    case 4: // tele... -.-
                    case 7: // assaulter
                    case 8: // assassinate
                    case 9: // rush
                    case 11: //chair
                    {
//                case 14: {
                        //Teleport movement - same as above
                        p.skip(8); //xpos = lea.readShort(); ypos = lea.readShort(); xwobble = lea.readShort(); ywobble = lea.readShort();
                        byte newstate = p.readByte();

                        singleMoveCommandList.add(new SingleMoveCommand(command, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, newstate, (short) 0));
//                        debugprint(movementPacketRecList.get(i).getFieldValues());

                        break;
                    }
                    case 14:
                        p.skip(9); // jump down (?)

                        singleMoveCommandList.add(new SingleMoveCommand(command, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, (byte) 0, (short) 0));
//                        debugprint(movementPacketRecList.get(i).getFieldValues());

                        break;
                    case 10: // Change Equip
                        //ignored server-side
                        p.readByte();
                        break;
//                /*case 11: { // Chair
//                    short xpos = lea.readShort();
//                    short ypos = lea.readShort();
//                    short fh = lea.readShort();
//                    byte newstate = lea.readByte();
//                    short duration = lea.readShort();
//                    ChairMovement cm = new ChairMovement(command, new Point(xpos, ypos), duration, newstate);
//                    cm.setFh(fh);
//                    res.add(cm);
//                    break;
//                }*/
                    case 15: {
                        //Jump down movement - stance only
                        p.skip(12); //short xpos = lea.readShort(); ypos = lea.readShort(); xwobble = lea.readShort(); ywobble = lea.readShort(); fh = lea.readShort(); ofh = lea.readShort();
                        byte newstate = p.readByte();
                        p.readShort(); // duration

                        singleMoveCommandList.add(new SingleMoveCommand(command, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, newstate, (short) 0));
//                        debugprint(movementPacketRecList.get(i).getFieldValues());

                        break;
                    }
                    case 21: {//Causes aran to do weird stuff when attacking o.o
                    /*byte newstate = lea.readByte();
                     short unk = lea.readShort();
                     AranMovement am = new AranMovement(command, null, unk, newstate);
                     res.add(am);*/
                        p.skip(3);

                        singleMoveCommandList.add(new SingleMoveCommand(command, (short) 0, (short) 0, (short) 0, (short) 0, (short) 0, (byte) 0, (short) 0));
//                        debugprint(movementPacketRecList.get(i).getFieldValues());

                        break;
                    }
                    default:
                        debugprint(("Unhandled Case: {}" + command));
//                        throw new EmptyMovementException(p);
                }
            }
            return singleMoveCommandList;
        } catch (Exception e) {

        }
        return null;
    }

    public static InPacket reconstructMovementInPacket(List<SingleMoveCommand> movementList) {
        byte numCommands = (byte) movementList.size();

        ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            byteBuf.writeByte(0); // first 9 bytes that will be skipped
        }

        byteBuf.writeByte(numCommands);
        for (SingleMoveCommand packet : movementList) {
            writeMovePackDataToByteBuf(byteBuf, packet);
        }
        InPacket p = (new ByteBufInPacket(byteBuf));
        return p;
    }

    /*
    public static InPacket constructInPacketFromCSVMoveData(MovementData movementData) {
        byte numCommands = (byte) movementData.getNumCommands();
        ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            byteBuf.writeByte(0); // first 9 bytes that will be skipped
        }
        byteBuf.writeByte(numCommands);
        for (MovementPacketRec packet : movementData.getPackets()) {
            writeMovePackDataToByteBuf(byteBuf, packet);
        }
        InPacket p = (new ByteBufInPacket(byteBuf));
        return p;
    }*/

    public static void writeMovePackDataToByteBuf(ByteBuf byteBuf, SingleMoveCommand packRec) {
        byteBuf.writeByte(packRec.getCommand());
        byteBuf.writeShortLE(packRec.getXpos());
        byteBuf.writeShortLE(packRec.getYpos());
        byteBuf.writeShortLE(packRec.getXwobble());
        byteBuf.writeShortLE(packRec.getYwobble());
        byteBuf.writeShortLE(packRec.getFh());
        byteBuf.writeByte(packRec.getNewstate());
        byteBuf.writeShortLE(packRec.getDuration());
    }

    public static InPacket modifyMovementPacketWithOffset(InPacket movePacket, Character fakechar) {
        List<SingleMoveCommand> deconstructedMovementList = deconstructMovementInPacket(movePacket);
        List<SingleMoveCommand> modifiedMovementList = modifyMovementPacketWithOffset(deconstructedMovementList, fakechar);
        return reconstructMovementInPacket(modifiedMovementList);
    }

    public static List<SingleMoveCommand> modifyMovementPacketWithOffset(List<SingleMoveCommand> movementList, Character fakechar) {
        int xpos = fakechar.getPosition().x;
        int ypos = fakechar.getPosition().y;
        int fh = findFootHoldId(fakechar);
        return modifyMovementPacketWithOffset(movementList, xpos, ypos, fh);
    }

    public static List<SingleMoveCommand> modifyMovementPacketWithOffset(List<SingleMoveCommand> movementList, int currentXPos, int currentYPos, int currentFh) {
        int offsetX = (movementList.getFirst().getXpos() - currentXPos);
        int offsetY = (movementList.getFirst().getYpos() - currentYPos);

        for (SingleMoveCommand packet : movementList) {
            short newXpos = (short) (packet.getXpos() - offsetX);
            packet.setXpos(newXpos);
            short newYpos = (short) (packet.getYpos() - offsetY);
            packet.setYpos(newYpos);
            packet.setFh((short) currentFh);
        }
        return movementList;
    }

    public static InPacket createArtificialStopPacket(Character fakechar) {
        short xpos = (short) fakechar.getPosition().getX();
        short ypos = (short) fakechar.getPosition().getY();
        byte stance = (byte) fakechar.getStance();
        return createArtificialStopPacket(
                xpos, ypos, stance);
    }

    public static InPacket createArtificialStopPacket(short xpos, short ypos, byte facingDirection) {
//        debugprint("CreateArtificialStopPacket: ", xpos, ypos, facingDirection);
        byte numCommands = 2;
        byte newstate;
        if (facingDirection % 2 == 0) { // Example logic: even numbers = right
            newstate = IDLE_RIGHT;
        } else { // odd numbers = left
            newstate = IDLE_LEFT;
        }
        short duration = 250;

        ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            byteBuf.writeByte(0); // first 9 bytes that will be skipped
        }
        byteBuf.writeByte(numCommands);

        SingleMoveCommand packRec = new SingleMoveCommand(NORMAL,
                xpos, ypos, (short) 0, (short) 0, (short) 0, newstate, duration);
        writeMovePackDataToByteBuf(byteBuf, packRec);
        writeMovePackDataToByteBuf(byteBuf, packRec);
//        byteBuf.writeByte(MovementPacketEnums.MovementPacket.NORMAL);
//        byteBuf.writeShortLE(xpos);
//        byteBuf.writeShortLE(ypos);
//        byteBuf.writeShortLE(0); // x wobble
//        byteBuf.writeShortLE(0); // y wobble
//        byteBuf.writeShortLE(0); // fh
//        byteBuf.writeByte(newstate);
//        byteBuf.writeShortLE(duration);

//        byteBuf.writeByte(MovementPacketEnums.MovementPacket.NORMAL);
//        byteBuf.writeShortLE(xpos);
//        byteBuf.writeShortLE(ypos);
//        byteBuf.writeShortLE(0); // x wobble
//        byteBuf.writeShortLE(0); // y wobble
//        byteBuf.writeShortLE(0); // fh
//        byteBuf.writeByte(newstate);
//        byteBuf.writeShortLE(duration);
        InPacket p = (new ByteBufInPacket(byteBuf));
        return p;
    }

    public static InPacket createIdleStandlingPacket(Character fakechar) {
        short xpos = (short) fakechar.getPosition().getX();
        short ypos = (short) fakechar.getPosition().getY();
        short fh = (short) findFootHoldId(fakechar);
        byte stance = (byte) fakechar.getStance();
        return createIdleStandlingPacket(xpos, ypos, fh, stance);
    }

    public static InPacket createIdleStandlingPacket(short xpos, short ypos, short fh, byte stance) {
        ByteBuf byteBuf = Unpooled.buffer();
        if (stance % 2 == 0) { // Example logic: even numbers = right
            stance = IDLE_RIGHT;
        } else { // odd numbers = left
            stance = IDLE_LEFT;
        }

        for (int i = 0; i < 9; i++) {
            byteBuf.writeByte(0); // first 9 bytes that will be skipped
        }
        byteBuf.writeByte(1); // numCommands

        SingleMoveCommand packRec = new SingleMoveCommand(NORMAL,
                xpos, ypos, (short) 0, (short) 0, fh, stance, (short) 200);
        writeMovePackDataToByteBuf(byteBuf, packRec);
        InPacket p = (new ByteBufInPacket(byteBuf));
        return p;
    }

    // This works okay. not the best.
    public static InPacket createFallDownPacket(Character fakechar) {
        short xpos = (short) fakechar.getPosition().getX();
        short ypos = (short) fakechar.getPosition().getY();
        short fh = (short) findFootHoldId(fakechar);
        byte stance = (byte) fakechar.getStance();
        short finalypos = (short) MovementCommands.getFootHoldObject(fakechar).getY1();
        return createFallDownPacket(xpos, ypos, fh, stance, finalypos);
    }

    public static InPacket createFallDownPacket(short xpos, short ypos, short fh, byte stance, short finalypos) {
        ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            byteBuf.writeByte(0); // first 9 bytes that will be skipped
        }
        int numCommands = 2;
        byteBuf.writeByte(numCommands + 2); // numCommands

        int ywobble;
        for (int x = 0; x < numCommands; x++) {
            byteBuf.writeByte(NORMAL);
            byteBuf.writeShortLE(xpos);

            if (x == 0) {
                byteBuf.writeShortLE(ypos + 80);
            }
            if (x == 1) {
                byteBuf.writeShortLE(finalypos - 15);
            }

            byteBuf.writeShortLE(0); // x wobble
            ywobble = 670;
            byteBuf.writeShortLE(ywobble); // y wobble
            byteBuf.writeShortLE(fh); // fh

            if (stance % 2 == 0) { // Example logic: even numbers = right
                stance = JUMP_RIGHT;
            } else { // odd numbers = left
                stance = JUMP_LEFT;
            }

            if (x == numCommands) {
                if (stance % 2 == 0) { // Example logic: even numbers = right
                    stance = IDLE_RIGHT;
                } else { // odd numbers = left
                    stance = IDLE_LEFT;
                }
            }
            byteBuf.writeByte(stance);
            byteBuf.writeShortLE(500);

        }

        byteBuf.writeByte(NORMAL);
        byteBuf.writeShortLE(xpos);
        byteBuf.writeShortLE(finalypos);
        byteBuf.writeShortLE(0); // x wob
        byteBuf.writeShortLE(0); // y wob
        byteBuf.writeShortLE(fh); // fh

        if (stance % 2 == 0) { // Example logic: even numbers = right
            stance = JUMP_RIGHT;
        } else { // odd numbers = left
            stance = JUMP_LEFT;
        }
        byteBuf.writeByte(stance);
        byteBuf.writeShortLE(100); // duration


        byteBuf.writeByte(NORMAL);
        byteBuf.writeShortLE(xpos);
        byteBuf.writeShortLE(finalypos);
        byteBuf.writeShortLE(0); // x wob
        byteBuf.writeShortLE(0); // y wob
        byteBuf.writeShortLE(fh); // fh

        if (stance % 2 == 0) { // Example logic: even numbers = right
            stance = IDLE_RIGHT;
        } else { // odd numbers = left
            stance = IDLE_LEFT;
        }
        byteBuf.writeByte(stance);
        byteBuf.writeShortLE(100); // duration


        InPacket p = (new ByteBufInPacket(byteBuf));
        return p;
    }

    public static InPacket createSitPacket(Character fakechar) {
        short xpos = (short) fakechar.getPosition().getX();
        short ypos = (short) fakechar.getPosition().getY();
        short fh = (short) findFootHoldId(fakechar);
        byte stance = (byte) fakechar.getStance();
        return createSitPacket(xpos, ypos, fh, stance);
    }

    /**
     * Hand made movement packet for setting.
     * Uses command = 11, does not need additional packets. set xpos, ypos to char, and stance
     */
    public static InPacket createSitPacket(short xpos, short ypos, short fh, byte stance) {
        byte sit_stance = getConvertedStanceDirection(stance, SIT_RIGHT, SIT_LEFT);

        ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            byteBuf.writeByte(0); // first 9 bytes that will be skipped
        }
        byteBuf.writeByte(1); // numCommands

        byteBuf.writeByte(11); // Chair move command
        byteBuf.writeShortLE(xpos);
        byteBuf.writeShortLE(ypos);
        byteBuf.writeShortLE(0);
        byteBuf.writeShortLE(sit_stance); // STANCE
        byteBuf.writeShortLE(0);
        byteBuf.writeShortLE(0);
        byteBuf.writeShortLE(0);

        InPacket p = (new ByteBufInPacket(byteBuf));
        return p;

    }

    public static InPacket createUnsitPacket(Character fakechar) {
        short xpos = (short) fakechar.getPosition().getX();
        short ypos = (short) fakechar.getPosition().getY();
        short fh = (short) findFootHoldId(fakechar);
        byte stance = (byte) fakechar.getStance();
        return createUnsitPacket(xpos, ypos, fh, stance);
    }

    public static InPacket createUnsitPacket(short xpos, short ypos, short fh, byte stance) {
        byte stand_stance = getConvertedStanceDirection(stance, IDLE_RIGHT, IDLE_LEFT);

        ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 9; i++) {
            byteBuf.writeByte(0); // first 9 bytes that will be skipped
        }
        byteBuf.writeByte(1); // numCommands

        byteBuf.writeByte(11); // Chair movement command
        byteBuf.writeShortLE(xpos);
        byteBuf.writeShortLE(ypos);
        byteBuf.writeShortLE(0);
        byteBuf.writeShortLE(stand_stance); // STANCE
        byteBuf.writeShortLE(0);
        byteBuf.writeShortLE(0);
        byteBuf.writeShortLE(0);

        InPacket p = (new ByteBufInPacket(byteBuf));
        return p;
    }

    public static Map<Long, InPacket> deepCopy(Map<Long, InPacket> original) {
        Map<Long, InPacket> copy = new LinkedHashMap<>(); // Use the same map type as original

        for (Map.Entry<Long, InPacket> entry : original.entrySet()) {
            Long key = entry.getKey();
            InPacket value = entry.getValue();

            // Deep copy the value (InPacket)
            InPacket copiedValue = value.copy();

            // Put the copied key and value into the new map
            copy.put(key, copiedValue);
        }

        return copy;
    }

}
