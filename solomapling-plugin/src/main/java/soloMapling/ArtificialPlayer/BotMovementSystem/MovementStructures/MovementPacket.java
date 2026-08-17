package soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures;

import org.gms.net.packet.InPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Packet class to store timestamp and full packet binary data
public class MovementPacket {
    private final long timestamp;
    private final InPacket packet;

    public MovementPacket(long timestamp, InPacket packet) {
        this.timestamp = timestamp;
        this.packet = packet;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public InPacket getPacket() {
        return packet;
    }

    @Override
    public String toString() {
        return "Packet{timestamp=" + timestamp + ", data=" + (Arrays.toString(packet.getBytes())) + "}";
    }

    public static Object[] parseNumCommandMovementPacketRecFromPacket(MovementPacket mp) {
        InPacket p = mp.getPacket();
        p.skip(9);
        List<SingleMoveCommand> list = new ArrayList<>();
        byte numCommands = p.readByte();

        for (byte i = 0; i < numCommands; i++) {
            byte command = p.readByte();
            System.out.println("debug: " + p.toString());
            switch (command) {
                case 0: // normal move
                case 5:
                case 17: { // Float
                    short xpos = p.readShort(); //is signed fine here?
                    short ypos = p.readShort();
                    short xwobble = p.readShort();
                    short ywobble = p.readShort();
                    short fh = p.readShort();
//                    p.skip(6);
                    byte newstate = p.readByte();
                    short duration = p.readShort(); //duration
                    SingleMoveCommand packRec = new SingleMoveCommand(command, xpos, ypos, xwobble, ywobble, fh, newstate, duration);
                    list.add(packRec);

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
                    short duration = p.readShort(); //duration

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
                    break;
                }
                case 14:
                    p.skip(9); // jump down (?)
                    break;
                case 10: // Change Equip
                    //ignored server-side
                    p.readByte();
                    break;
                /*case 11: { // Chair
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short fh = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    ChairMovement cm = new ChairMovement(command, new Point(xpos, ypos), duration, newstate);
                    cm.setFh(fh);
                    res.add(cm);
                    break;
                }*/
                case 15: {
                    //Jump down movement - stance only
                    p.skip(12); //short xpos = lea.readShort(); ypos = lea.readShort(); xwobble = lea.readShort(); ywobble = lea.readShort(); fh = lea.readShort(); ofh = lea.readShort();
                    byte newstate = p.readByte();
                    short duration = p.readShort(); // duration
                    break;
                }
                case 21: {//Causes aran to do weird stuff when attacking o.o
                    p.skip(3);
                    break;
                }
            }
        }
        return new Object[]{numCommands, list};
    }


}