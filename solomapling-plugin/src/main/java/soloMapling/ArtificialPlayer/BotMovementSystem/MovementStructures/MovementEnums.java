package soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures;

public class MovementEnums {

    private MovementEnums() {

    }

    public static class MovementPacketValues {
        public static final byte NONE = -1;
        public static final byte NORMAL = 0;
        public static final byte JUMP = 1;
        //    public static final byte IMPACT = 2;
//    public static final byte IMMEDIATE = 3;
//    public static final byte TELEPORT = 4;
    public static final byte HANG_ON_BACK = 5;
//    public static final byte ASSAULTER = 6;
//    public static final byte ASSASSINATION = 7;
//    public static final byte RUSH = 8;
//    public static final byte STAT_CHANGE = 9;
//    public static final byte SIT_DOWN = 10;
//    public static final byte START_FALL_DOWN = 11;
//    public static final byte FALL_DOWN = 12;
//    public static final byte START_WINGS = 13;
//    public static final byte WINGS = 14;
//    public static final byte ARAN_ADJUST = 15;
//    public static final byte MOB_TOSS = 16;
    public static final byte FLYING_BLOCK = 17;
//    public static final byte DASH_SLIDE = 18;
//    public static final byte BATTLE_MAGE_ADJUST = 19;
//    public static final byte FLASH_JUMP = 20;
//    public static final byte ROCKET_BOOSTER = 21;
//    public static final byte BACK_STEP_SHOT = 22;
//    public static final byte MOB_POWER_KNOCK_BACK = 23;
//    public static final byte VERTICAL_JUMP = 24;
//    public static final byte CUSTOM_IMPACT = 25;
//    public static final byte COMBAT_STEP = 26;
//    public static final byte HIT = 27;
//    public static final byte TIME_BOMB_ATK = 28;
//    public static final byte SNOW_BALL_TOUCH = 29;
//    public static final byte BUFF_ZONE_EFFECT = 30;
//    public static final byte MOB_LADDER = 31;
//    public static final byte MOB_RIGHT_ANGLE = 32;
//    public static final byte MOB_STOP_NODE_START = 33;
//    public static final byte MOB_BEFORE_NODE = 34;
//    public static final byte MOB_ATK_RUSH = 35;
//    public static final byte MOB_ATK_RUSH_STOP = 36;
    }

    public static class StanceValues {
        public static final byte MOVING_RIGHT = 2;
        public static final byte MOVING_LEFT = 3;
        public static final byte IDLE_RIGHT = 4;
        public static final byte IDLE_LEFT = 5;
        public static final byte JUMP_RIGHT = 6;
        public static final byte JUMP_LEFT = 7;

        public static final byte DUCK_RIGHT = 10;
        public static final byte DUCK_LEFT = 11;

        public static final byte SIT_RIGHT = 20;
        public static final byte SIT_LEFT = 21;
    }

    public static class FreeMarketValues {
        public static final int FM_ENTRANCE = 910000000;
        public static final int FM_ROOM_1 = 910000001;
        public static final int FM_ROOM_22 = 910000022;
    }

}
