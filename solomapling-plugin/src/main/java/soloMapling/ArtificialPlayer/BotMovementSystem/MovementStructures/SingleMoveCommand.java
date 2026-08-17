package soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures;

import java.awt.*;
import java.util.List;
import java.lang.reflect.Field;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.StanceValues.*;
import static soloMapling.DebugUtilities.debugprint;

/*
    Parsed data from each individual Movement Packet.
 */

public class SingleMoveCommand {

    private byte command;
    private short xpos;
    private short ypos;
    private short xwobble;
    private short ywobble;
    private short fh;
    private byte newstate;
    private short duration;

    public SingleMoveCommand(byte command, short xpos, short ypos,
                             short xwobble, short ywobble, short fh,
                             byte newstate, short duration) {
        this.command = command;
        this.xpos = xpos;
        this.ypos = ypos;
        this.xwobble = xwobble;
        this.ywobble = ywobble;
        this.fh = fh;
        this.newstate = newstate;
        this.duration = duration;
    }

    // Getters
    public byte getCommand() {
        return command;
    }

    public short getXpos() {
        return xpos;
    }

    public short getYpos() {
        return ypos;
    }

    public short getXwobble() {
        return xwobble;
    }

    public short getYwobble() {
        return ywobble;
    }

    public short getFh() {
        return fh;
    }

    public byte getNewstate() {
        return newstate;
    }

    public short getDuration() {
        return duration;
    }

    public Point getPoint() {
        return new Point(getXpos(), getYpos());
    }

    // Setters
    public void setCommand(byte command) {
        this.command = command;
    }

    public void setXpos(short xpos) {
        this.xpos = xpos;
    }

    public void setYpos(short ypos) {
        this.ypos = ypos;
    }

    public void setXwobble(short xwobble) {
        this.xwobble = xwobble;
    }

    public void setYwobble(short ywobble) {
        this.ywobble = ywobble;
    }

    public void setFh(short fh) {
        this.fh = fh;
    }

    public void setNewstate(byte newstate) {
        this.newstate = newstate;
    }

    public void setDuration(short duration) {
        this.duration = duration;
    }


    // Helper method to get all field values
    public Object[] getFieldValues() {
        Field[] fields = this.getClass().getDeclaredFields();
        Object[] values = new Object[fields.length];
        try {
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true); // Allow access to private fields
                values[i] = fields[i].get(this);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return values;
    }

    public boolean facingLeft() {
        switch (this.getNewstate()) {
            case MOVING_LEFT, JUMP_LEFT, IDLE_LEFT -> {
                return true;
            }
        }
        return false;
    }

    public void reverseFacingDirection() {
        switch (this.getNewstate()) {
            case MOVING_RIGHT -> this.setNewstate(MOVING_LEFT);
            case MOVING_LEFT -> this.setNewstate(MOVING_RIGHT);
            case IDLE_RIGHT -> this.setNewstate(IDLE_LEFT);
            case IDLE_LEFT -> this.setNewstate(IDLE_RIGHT);
            case JUMP_RIGHT -> this.setNewstate(JUMP_LEFT);
            case JUMP_LEFT -> this.setNewstate(JUMP_RIGHT);
//            default -> throw new IllegalStateException("Invalid state: " + this.getNewstate());
        }
    }

    public static byte getConvertedStanceDirection(byte stance, byte rightStance, byte leftStance) {
        if (stance % 2 == 0) {
            return rightStance;
        } else {
            return leftStance;
        }
    }

    public void reverseWobble() {
        this.setXwobble((short) -this.getXwobble());
        this.setYwobble((short) -this.getYwobble());
    }

    /**
     * Checks if the last stance in the list matches the given stance
     */
    public static boolean endsInStance(List<Byte> stances, byte checkStance) {
        if (stances == null || stances.isEmpty()) {
            return false;
        }
        boolean endStance = stances.getLast() == checkStance;
//        debugprint("Checking end Stance", stances.getLast(), "checkStance: ", checkStance, "endStance bool:", endStance);
        return endStance;
    }

    // Convenience methods for common stances (optional)
    public static boolean endsInStandingStance(List<Byte> stances) {
        debugprint("Checking stances: ", stances);
        return endsInStance(stances, IDLE_LEFT) || endsInStance(stances, IDLE_RIGHT); // or whatever your constant is
    }

    public static boolean endsInJumpingStance(List<Byte> stances) {
        return endsInStance(stances, JUMP_LEFT) || endsInStance(stances, JUMP_RIGHT);
    }

}
