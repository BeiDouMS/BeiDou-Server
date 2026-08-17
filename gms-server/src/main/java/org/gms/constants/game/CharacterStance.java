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
package org.gms.constants.game;

/**
 * MapleStory v83 character animation stance constants, used by the GCMoveSystem
 * dynamic movement engine to map physics state -> wire stance bytes.
 */
public final class CharacterStance {
    public static final int WALK_RIGHT_STANCE = 2;
    public static final int WALK_LEFT_STANCE = 3;
    public static final int STAND_RIGHT_STANCE = 4;
    public static final int STAND_LEFT_STANCE = 5;
    public static final int JUMP_RIGHT_STANCE = 6;
    public static final int JUMP_LEFT_STANCE = 7;
    public static final int ALERT_RIGHT_STANCE = 8;
    public static final int ALERT_LEFT_STANCE = 9;
    public static final int PRONE_RIGHT_STANCE = 10;
    public static final int PRONE_LEFT_STANCE = 11;
    public static final int PRONE_STANCE = PRONE_RIGHT_STANCE;
    public static final int SWIM_RIGHT_STANCE = 12;
    public static final int SWIM_LEFT_STANCE = 13;
    public static final int LADDER_RIGHT_STANCE = 14;
    public static final int LADDER_LEFT_STANCE = 15;
    public static final int LADDER_STANCE = LADDER_RIGHT_STANCE;
    public static final int ROPE_RIGHT_STANCE = 16;
    public static final int ROPE_LEFT_STANCE = 17;
    public static final int ROPE_STANCE = ROPE_RIGHT_STANCE;
    public static final int DEAD_RIGHT_STANCE = 18;
    public static final int DEAD_LEFT_STANCE = 19;

    private CharacterStance() {
    }

    public static boolean isFacingLeft(int stance) {
        return stance == WALK_LEFT_STANCE
                || stance == STAND_LEFT_STANCE
                || stance == JUMP_LEFT_STANCE
                || stance == PRONE_LEFT_STANCE
                || stance == SWIM_LEFT_STANCE
                || stance == LADDER_LEFT_STANCE
                || stance == ROPE_LEFT_STANCE
                || stance == DEAD_LEFT_STANCE;
    }

    public static boolean isSwimming(int stance) {
        return stance == SWIM_RIGHT_STANCE || stance == SWIM_LEFT_STANCE;
    }

    public static boolean isStanding(int stance) {
        return stance == STAND_RIGHT_STANCE || stance == STAND_LEFT_STANCE;
    }

    public static boolean isWalking(int stance) {
        return stance == WALK_RIGHT_STANCE || stance == WALK_LEFT_STANCE;
    }

    public static boolean isJumping(int stance) {
        return stance == JUMP_RIGHT_STANCE || stance == JUMP_LEFT_STANCE;
    }

    public static boolean isProne(int stance) {
        return stance == PRONE_RIGHT_STANCE || stance == PRONE_LEFT_STANCE;
    }

    public static boolean isClimbing(int stance) {
        return stance == ROPE_RIGHT_STANCE
                || stance == ROPE_LEFT_STANCE
                || stance == LADDER_RIGHT_STANCE
                || stance == LADDER_LEFT_STANCE;
    }

    public static boolean isDead(int stance) {
        return stance == DEAD_RIGHT_STANCE || stance == DEAD_LEFT_STANCE;
    }
}
